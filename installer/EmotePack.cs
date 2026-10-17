using System.IO.Compression;
using System.Security.Cryptography;
using System.Text.Json;

namespace ModsServerInstaller;

internal sealed record EmoteArchive(string Name, string Sha, string DownloadUrl);
internal sealed record EmoteApplyResult(bool Changed, string? BackupPath, int Count);

internal static class EmotePack
{
    private sealed record State(string ArchiveSha, Dictionary<string, string> Files);
    private static string GameFolder(string mods) => Directory.GetParent(Path.GetFullPath(mods))!.FullName;
    private static string PackFolder(string game) => Path.Combine(game, "emotes", "Mods Server Pack");
    private static string StatePath(string game) => Path.Combine(game, "modpack-state", "emotes.json");

    internal static async Task<bool> IsCurrentAsync(string mods, EmoteArchive archive)
    {
        var game = GameFolder(mods);
        ValidateFolders(game);
        var state = await ReadStateAsync(game);
        if (state is null || state.ArchiveSha != archive.Sha) return false;
        foreach (var (name, hash) in state.Files)
        {
            var path = Path.Combine(PackFolder(game), name);
            RejectLink(path);
            if (!File.Exists(path) || await HashAsync(path) != hash) return false;
        }
        return state.Files.Count > 0;
    }

    internal static async Task<EmoteApplyResult> ApplyAsync(HttpClient http, string mods, EmoteArchive archive)
    {
        var game = GameFolder(mods);
        if (await IsCurrentAsync(mods, archive))
            return new EmoteApplyResult(false, null, (await ReadStateAsync(game))!.Files.Count);
        var oldState = await ReadStateAsync(game);
        var staging = Path.Combine(game, $".emotes-installer-staging-{Guid.NewGuid():N}");
        var backup = Path.Combine(game, "modpack-backups", $"emotes-{Guid.NewGuid():N}");
        var moved = new List<(string Original, string Backup)>();
        var installed = new List<string>();
        Directory.CreateDirectory(staging);
        try
        {
            var zipPath = Path.Combine(staging, "pack.zip");
            Console.WriteLine($"Downloading emote pack: {archive.Name}");
            using (var response = await http.GetAsync(archive.DownloadUrl, HttpCompletionOption.ResponseHeadersRead))
            {
                response.EnsureSuccessStatusCode();
                await using var source = await response.Content.ReadAsStreamAsync();
                await using var destination = File.Create(zipPath);
                await source.CopyToAsync(destination);
            }
            if (!string.Equals(await Program.GitBlobShaAsync(zipPath), archive.Sha, StringComparison.OrdinalIgnoreCase))
                throw new InvalidDataException("Emote ZIP hash verification failed.");

            var files = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
            using (var zip = ZipFile.OpenRead(zipPath))
                foreach (var entry in zip.Entries)
                {
                    ValidateName(entry.FullName);
                    if (((entry.ExternalAttributes >> 16) & 0xF000) == 0xA000 || !files.TryAdd(entry.FullName, ""))
                        throw new InvalidDataException("Emote ZIP contains a link or duplicate filename.");
                    var path = Path.Combine(staging, entry.FullName);
                    entry.ExtractToFile(path);
                    files[entry.FullName] = await HashAsync(path);
                }
            if (files.Count == 0) throw new InvalidDataException("Emote ZIP contains no emotes.");

            // Only names from the current archive or our previous tracking file can be changed.
            var pack = PackFolder(game);
            var replace = new List<string>();
            foreach (var (name, hash) in files)
            {
                var path = Path.Combine(pack, name);
                RejectLink(path);
                if (Directory.Exists(path)) throw new IOException($"An emote filename is occupied by a folder: {name}");
                if (!File.Exists(path) || await HashAsync(path) != hash) replace.Add(name);
            }
            var retire = new List<string>();
            if (oldState is not null)
                foreach (var (name, hash) in oldState.Files.Where(file => !files.ContainsKey(file.Key)))
                {
                    var path = Path.Combine(pack, name);
                    RejectLink(path);
                    // A user-edited retired emote becomes personal and stays in place.
                    if (File.Exists(path) && await HashAsync(path) == hash) retire.Add(name);
                }

            var newState = Path.Combine(staging, "state.json");
            await File.WriteAllTextAsync(newState, JsonSerializer.Serialize(new State(archive.Sha, files)) + "\n");
            Directory.CreateDirectory(pack);
            Directory.CreateDirectory(Path.GetDirectoryName(StatePath(game))!);
            if (File.Exists(StatePath(game)))
            {
                Directory.CreateDirectory(backup);
                File.Copy(StatePath(game), Path.Combine(backup, "emotes-state.json"));
            }
            foreach (var name in replace.Concat(retire))
            {
                var original = Path.Combine(pack, name);
                if (!File.Exists(original)) continue;
                Directory.CreateDirectory(backup);
                var saved = Path.Combine(backup, name);
                File.Move(original, saved);
                moved.Add((original, saved));
            }
            foreach (var name in replace)
            {
                var destination = Path.Combine(pack, name);
                File.Move(Path.Combine(staging, name), destination);
                installed.Add(destination);
            }
            File.Move(newState, StatePath(game), overwrite: true);
            return new EmoteApplyResult(true, Directory.Exists(backup) ? backup : null, files.Count);
        }
        catch
        {
            foreach (var path in installed) File.Delete(path);
            foreach (var (original, saved) in moved.AsEnumerable().Reverse()) File.Move(saved, original);
            throw;
        }
        finally
        {
            if (Path.GetDirectoryName(Path.GetFullPath(staging)) != game)
                throw new InvalidOperationException("Unexpected emote staging directory.");
            try { Directory.Delete(staging, recursive: true); }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
            { Console.Error.WriteLine($"Warning: temporary emote files remain at '{staging}'."); }
        }
    }

    private static async Task<State?> ReadStateAsync(string game)
    {
        var path = StatePath(game);
        RejectLink(path);
        if (!File.Exists(path)) return null;
        var state = JsonSerializer.Deserialize<State>(await File.ReadAllTextAsync(path))
            ?? throw new InvalidDataException("Emote tracking data must be an object.");
        if (state.Files is null || state.Files.Keys.Distinct(StringComparer.OrdinalIgnoreCase).Count() != state.Files.Count)
            throw new InvalidDataException("Invalid emote tracking filenames.");
        foreach (var name in state.Files.Keys) ValidateName(name);
        return state;
    }

    private static void ValidateName(string name)
    {
        if (string.IsNullOrEmpty(name) || Path.GetFileName(name) != name ||
            name.IndexOfAny(Path.GetInvalidFileNameChars()) >= 0 ||
            !name.EndsWith(".emotecraft", StringComparison.OrdinalIgnoreCase))
            throw new InvalidDataException($"Expected a flat .emotecraft filename in the pack: {name}");
    }

    private static void ValidateFolders(string game)
    {
        foreach (var path in new[] { Path.Combine(game, "emotes"), PackFolder(game),
            Path.Combine(game, "modpack-state"), Path.Combine(game, "modpack-backups") }) RejectLink(path);
    }

    private static void RejectLink(string path)
    {
        if (Path.Exists(path) && (File.GetAttributes(path) & FileAttributes.ReparsePoint) != 0)
            throw new IOException($"Cannot update emotes through a link: {path}");
    }

    private static async Task<string> HashAsync(string path)
    {
        await using var stream = File.OpenRead(path);
        return Convert.ToHexString(await SHA256.HashDataAsync(stream));
    }
}
