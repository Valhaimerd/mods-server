using System.IO.Compression;
using System.Net;
using System.Security.Cryptography;
using System.Text;

namespace ModsServerInstaller;

internal static class EmotePackTests
{
    internal static async Task RunAsync()
    {
        var root = Path.Combine(Path.GetTempPath(), $"emotes-installer-{Guid.NewGuid():N}");
        var mods = Path.Combine(root, "mods");
        var pack = Path.Combine(root, "emotes", "Mods Server Pack");
        Directory.CreateDirectory(Path.Combine(root, "emotes"));
        var personal = Path.Combine(root, "emotes", "personal.emotecraft");
        await File.WriteAllTextAsync(personal, "personal");
        var handler = new ArchiveHandler();
        using var http = new HttpClient(handler);
        try
        {
            var first = handler.Set(("Dance (Танец).emotecraft", "dance"), ("old.emotecraft", "old"), ("edited.emotecraft", "original"));
            var checkRoot = Path.Combine(root, "check-only");
            Require(!await EmotePack.IsCurrentAsync(Path.Combine(checkRoot, "mods"), first) &&
                !Directory.Exists(checkRoot) && handler.Requests == 0, "read-only status check without downloads or folders");
            var result = await EmotePack.ApplyAsync(http, mods, first);
            Require(result.Changed && result.Count == 3 && File.Exists(Path.Combine(pack, "Dance (Танец).emotecraft")), "fresh Unicode extraction");
            Require(await File.ReadAllTextAsync(personal) == "personal", "personal emote preservation");
            Require(!(await EmotePack.ApplyAsync(http, mods, first)).Changed && handler.Requests == 1, "skip unchanged archive download");
            File.Delete(Path.Combine(pack, "Dance (Танец).emotecraft"));
            Require((await EmotePack.ApplyAsync(http, mods, first)).Changed && handler.Requests == 2, "repair missing emote");
            await File.WriteAllTextAsync(Path.Combine(pack, "edited.emotecraft"), "user edit");
            await File.WriteAllTextAsync(Path.Combine(pack, "custom.emotecraft"), "custom");
            var second = handler.Set(("Dance (Танец).emotecraft", "updated"), ("new.emotecraft", "new"));
            result = await EmotePack.ApplyAsync(http, mods, second);
            Require(result.Changed && result.BackupPath is not null &&
                await File.ReadAllTextAsync(Path.Combine(result.BackupPath!, "Dance (Танец).emotecraft")) == "dance" &&
                await File.ReadAllTextAsync(Path.Combine(result.BackupPath!, "old.emotecraft")) == "old", "update backups");
            Require(!File.Exists(Path.Combine(pack, "old.emotecraft")) &&
                await File.ReadAllTextAsync(Path.Combine(pack, "edited.emotecraft")) == "user edit" &&
                await File.ReadAllTextAsync(Path.Combine(pack, "custom.emotecraft")) == "custom", "remove only unchanged retired pack files");
            foreach (var invalid in new[] {
                new[] { ("../escape.emotecraft", "bad") },
                new[] { ("A.emotecraft", "a"), ("a.emotecraft", "b") },
                new[] { ("not-an-emote.txt", "bad") }
            })
            {
                var rejected = false;
                try { await EmotePack.ApplyAsync(http, mods, handler.Set(invalid)); }
                catch (InvalidDataException) { rejected = true; }
                Require(rejected && !File.Exists(Path.Combine(root, "escape.emotecraft")) &&
                    await File.ReadAllTextAsync(Path.Combine(pack, "Dance (Танец).emotecraft")) == "updated", "reject invalid ZIP before modifying emotes");
            }
            var mismatch = handler.Set(("new.emotecraft", "bad bytes")) with { Sha = new string('0', 40) };
            var hashRejected = false;
            try { await EmotePack.ApplyAsync(http, mods, mismatch); }
            catch (InvalidDataException) { hashRejected = true; }
            Require(hashRejected && await File.ReadAllTextAsync(Path.Combine(pack, "new.emotecraft")) == "new", "archive checksum rejection");

            // Fail the final tracking-file replacement after the new emotes have been installed.
            var lockedUpdate = handler.Set(("Dance (Танец).emotecraft", "third"), ("new.emotecraft", "third"), ("added.emotecraft", "added"));
            using (var locked = new FileStream(Path.Combine(root, "modpack-state", "emotes.json"), FileMode.Open, FileAccess.Read, FileShare.Read))
            {
                var failed = false;
                try { await EmotePack.ApplyAsync(http, mods, lockedUpdate); }
                catch (Exception ex) when (ex is IOException or UnauthorizedAccessException) { failed = true; }
                Require(failed && await File.ReadAllTextAsync(Path.Combine(pack, "Dance (Танец).emotecraft")) == "updated" &&
                    !File.Exists(Path.Combine(pack, "added.emotecraft")), "rollback after partial replacement");
            }
            handler.Set(("Dance (Танец).emotecraft", "updated"), ("new.emotecraft", "new"));
            Require(!(await EmotePack.ApplyAsync(http, mods, second)).Changed, "rollback preserves tracking state");

            var scoped = handler.Set(("new.emotecraft", "separate pack")) with { Scope = "Second Pack" };
            await EmotePack.ApplyAsync(http, mods, scoped);
            Require(await File.ReadAllTextAsync(Path.Combine(pack, "new.emotecraft")) == "new" &&
                await File.ReadAllTextAsync(Path.Combine(pack, "Second Pack", "new.emotecraft")) == "separate pack" &&
                await EmotePack.IsCurrentAsync(mods, second) && await EmotePack.IsCurrentAsync(mods, scoped),
                "separate packs preserve colliding filenames and independent tracking");
            var requests = handler.Requests;
            Require(!(await EmotePack.ApplyAsync(http, mods, scoped)).Changed && handler.Requests == requests,
                "skip current scoped pack download");
            var scopedUpdate = handler.Set(("replacement.emotecraft", "replacement")) with { Scope = "Second Pack" };
            await EmotePack.ApplyAsync(http, mods, scopedUpdate);
            Require(!File.Exists(Path.Combine(pack, "Second Pack", "new.emotecraft")) &&
                await EmotePack.IsCurrentAsync(mods, second), "scoped retirement leaves original pack intact");
            var invalidScope = false;
            try { await EmotePack.ApplyAsync(http, mods, scopedUpdate with { Scope = ".." }); }
            catch (InvalidDataException) { invalidScope = true; }
            Require(invalidScope, "reject escaping pack scope");

            var migrationGame = Path.Combine(root, "migration");
            var migrationMods = Path.Combine(migrationGame, "mods");
            var legacyPack = Path.Combine(migrationGame, "emotes", "Mods Server Pack");
            var legacy = handler.Set(("dance.emotecraft", "dance"), ("edited.emotecraft", "original"))
                with { Name = "SPEmotes Dances 5.zip" };
            await EmotePack.ApplyAsync(http, migrationMods, legacy);
            await File.WriteAllTextAsync(Path.Combine(legacyPack, "edited.emotecraft"), "user edit");
            await File.WriteAllTextAsync(Path.Combine(legacyPack, "personal.emotecraft"), "personal");
            var dances = legacy with { Scope = "SPEmotes Dances 5" };
            Require(!await EmotePack.IsCurrentAsync(migrationMods, dances), "legacy Dances needs migration");
            using (var locked = new FileStream(Path.Combine(migrationGame, "modpack-state", "emotes.json"),
                FileMode.Open, FileAccess.Read, FileShare.Read))
            {
                var failed = false;
                try { await EmotePack.ApplyAsync(http, migrationMods, dances); }
                catch (Exception ex) when (ex is IOException or UnauthorizedAccessException) { failed = true; }
                Require(failed && File.Exists(Path.Combine(legacyPack, "dance.emotecraft")) &&
                    await File.ReadAllTextAsync(Path.Combine(legacyPack, "edited.emotecraft")) == "user edit" &&
                    !File.Exists(Path.Combine(legacyPack, dances.Scope, "dance.emotecraft")),
                    "failed Dances migration restores legacy files");
            }
            var migrated = await EmotePack.ApplyAsync(http, migrationMods, dances);
            Require(migrated.BackupPath is not null &&
                await File.ReadAllTextAsync(Path.Combine(migrated.BackupPath!, "legacy", "edited.emotecraft")) == "user edit" &&
                !File.Exists(Path.Combine(legacyPack, "dance.emotecraft")) &&
                !File.Exists(Path.Combine(legacyPack, "edited.emotecraft")) &&
                File.Exists(Path.Combine(legacyPack, "personal.emotecraft")) &&
                await EmotePack.IsCurrentAsync(migrationMods, dances), "migrate only tracked legacy Dances with backups");
            Require(!(await EmotePack.ApplyAsync(http, migrationMods, dances)).Changed, "Dances migration is idempotent");

            var collisionGame = Path.Combine(root, "collision");
            var collisionPack = Path.Combine(collisionGame, "emotes", "Mods Server Pack");
            Directory.CreateDirectory(collisionPack);
            await File.WriteAllTextAsync(Path.Combine(collisionPack, "same.emotecraft"), "personal collision");
            var collision = await EmotePack.ApplyAsync(http, Path.Combine(collisionGame, "mods"),
                handler.Set(("same.emotecraft", "pack version")));
            Require(collision.BackupPath is not null &&
                await File.ReadAllTextAsync(Path.Combine(collision.BackupPath!, "same.emotecraft")) == "personal collision", "back up incoming filename collisions");
        }
        finally
        {
            if (Path.GetDirectoryName(root) != Path.GetTempPath().TrimEnd(Path.DirectorySeparatorChar))
                throw new InvalidOperationException("Unexpected emote test directory.");
            Directory.Delete(root, recursive: true);
        }
    }

    private static void Require(bool condition, string message)
    {
        if (!condition) throw new InvalidOperationException($"Self-test failed: emotes {message}.");
    }

    private sealed class ArchiveHandler : HttpMessageHandler
    {
        private byte[] bytes = [];
        internal int Requests { get; private set; }
        internal EmoteArchive Set(params (string Name, string Content)[] entries)
        {
            using var stream = new MemoryStream();
            using (var zip = new ZipArchive(stream, ZipArchiveMode.Create, leaveOpen: true))
                foreach (var (name, content) in entries)
                {
                    var entry = zip.CreateEntry(name);
                    entry.LastWriteTime = new DateTimeOffset(2026, 1, 1, 0, 0, 0, TimeSpan.Zero);
                    using var writer = new StreamWriter(entry.Open(), new UTF8Encoding(false));
                    writer.Write(content);
                }
            bytes = stream.ToArray();
            var header = Encoding.UTF8.GetBytes($"blob {bytes.Length}\0");
            var sha = Convert.ToHexString(SHA1.HashData(header.Concat(bytes).ToArray())).ToLowerInvariant();
            return new EmoteArchive("emotes.zip", sha, "https://example.invalid/emotes.zip");
        }
        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            Requests++;
            return Task.FromResult(new HttpResponseMessage(HttpStatusCode.OK) { Content = new ByteArrayContent(bytes) });
        }
    }
}
