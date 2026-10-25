using System.Text;
using System.Text.RegularExpressions;

namespace ModsServerInstaller;

internal sealed record KeybindApplyResult(bool Changed, string? BackupPath);

internal static class RecommendedKeybinds
{
    private const string ProfileUrl = "https://raw.githubusercontent.com/Valhaimerd/mods-server/main/installer/recommended-keybinds.txt";

    public static async Task<KeybindApplyResult> ApplyAsync(string modsFolder, HttpClient http)
    {
        var gameFolder = Directory.GetParent(modsFolder)?.FullName
            ?? throw new InvalidOperationException("The mods folder has no parent directory.");
        var optionsPath = Path.Combine(gameFolder, "options.txt");
        using var timeout = new CancellationTokenSource(TimeSpan.FromSeconds(30));
        var profile = LoadProfile(await http.GetStringAsync(ProfileUrl, timeout.Token));
        var existing = File.Exists(optionsPath)
            ? await File.ReadAllTextAsync(optionsPath)
            : string.Empty;
        var seen = new HashSet<string>(StringComparer.Ordinal);
        var updated = Regex.Replace(existing, @"^key_[^\r\n]*", match =>
        {
            var separator = match.Value.IndexOf(':');
            if (separator < 0)
            {
                return match.Value;
            }

            var key = match.Value[..separator];
            if (!profile.TryGetValue(key, out var binding))
            {
                return match.Value;
            }

            seen.Add(key);
            return binding;
        }, RegexOptions.Multiline);

        var newline = existing.Contains("\r\n", StringComparison.Ordinal) ? "\r\n" : "\n";
        var missing = profile.Where(entry => !seen.Contains(entry.Key)).Select(entry => entry.Value).ToArray();
        if (missing.Length > 0)
        {
            if (updated.Length > 0 && !updated.EndsWith('\n'))
            {
                updated += newline;
            }

            updated += string.Join(newline, missing) + newline;
        }

        if (updated == existing)
        {
            return new KeybindApplyResult(false, null);
        }

        Directory.CreateDirectory(gameFolder);
        string? backupPath = null;
        if (File.Exists(optionsPath))
        {
            var backupFolder = Path.Combine(gameFolder, "modpack-backups",
                $"keybinds-{DateTime.Now:yyyyMMdd-HHmmss}-{Guid.NewGuid():N}");
            Directory.CreateDirectory(backupFolder);
            backupPath = Path.Combine(backupFolder, "options.txt");
            File.Copy(optionsPath, backupPath);
        }

        var temporaryPath = Path.Combine(gameFolder, $".options-{Guid.NewGuid():N}.tmp");
        try
        {
            await File.WriteAllTextAsync(temporaryPath, updated, new UTF8Encoding(false));
            if (File.Exists(optionsPath))
            {
                File.Move(temporaryPath, optionsPath, overwrite: true);
            }
            else
            {
                File.Move(temporaryPath, optionsPath);
            }
        }
        finally
        {
            if (File.Exists(temporaryPath))
            {
                File.Delete(temporaryPath);
            }
        }

        return new KeybindApplyResult(true, backupPath);
    }

    internal static async Task RunSelfTestAsync()
    {
        var root = Path.Combine(Path.GetTempPath(), $"controls-installer-{Guid.NewGuid():N}");
        var options = Path.Combine(root, "options.txt");
        Directory.CreateDirectory(root);
        const string original = "music:0.3\nlastServer:private.example\nkey_patpat.keybinding.pat:key.keyboard.left.shift\nkey_custom.mod:key.keyboard.p\n";
        using var handler = new ProfileHandler();
        using var http = new HttpClient(handler);
        try
        {
            await File.WriteAllTextAsync(options, original);
            handler.Text = "key_patpat.keybinding.pat:key.mouse.right:CONTROL\nkey_key.penchant.show_progress:key.keyboard.left.shift\nkey_test:key.keyboard.r:SHIFT\n";
            var result = await ApplyAsync(root + "/mods", http);
            var saved = await File.ReadAllTextAsync(options);
            if (!result.Changed || result.BackupPath is null ||
                await File.ReadAllTextAsync(result.BackupPath) != original ||
                !saved.Contains("music:0.3\nlastServer:private.example\n") ||
                !saved.Contains("key_custom.mod:key.keyboard.p\n") ||
                handler.Text.Split('\n', StringSplitOptions.RemoveEmptyEntries).Any(line => !saved.Split('\n').Contains(line)) || handler.Requests != 1)
                throw new InvalidOperationException("Self-test failed: downloaded controls merge/backup/privacy.");
            if ((await ApplyAsync(root + "/mods", http)).Changed)
                throw new InvalidOperationException("Self-test failed: controls merge must be idempotent.");
            handler.Text = "key_patpat.keybinding.pat:key.keyboard.g\n";
            if (!(await ApplyAsync(root + "/mods", http)).Changed ||
                !(await File.ReadAllTextAsync(options)).Contains(handler.Text))
                throw new InvalidOperationException("Self-test failed: controls must refresh from the next download.");
            saved = await File.ReadAllTextAsync(options);
            foreach (var invalid in new[] { "", "lastServer:leaked.example\n", "key_test:bad\n", "key_test:key.keyboard.g\nkey_test:key.keyboard.p\n", new string('x', 65537) })
            {
                handler.Text = invalid;
                try { await ApplyAsync(root + "/mods", http); throw new Exception("Invalid controls accepted."); }
                catch (InvalidDataException) { }
                if (await File.ReadAllTextAsync(options) != saved)
                    throw new InvalidOperationException("Self-test failed: invalid download modified controls.");
            }
            handler.Fail = true;
            try { await ApplyAsync(root + "/mods", http); throw new Exception("Download failure ignored."); }
            catch (HttpRequestException) { }
            if (await File.ReadAllTextAsync(options) != saved)
                throw new InvalidOperationException("Self-test failed: download failure modified controls.");
            var missing = Path.Combine(root, "new-instance");
            try { await ApplyAsync(missing + "/mods", http); throw new Exception("Download failure ignored."); }
            catch (HttpRequestException) { }
            if (Directory.Exists(missing)) throw new Exception("Failed download created instance files.");
        }
        finally { Directory.Delete(root, recursive: true); }
    }

    private sealed class ProfileHandler : HttpMessageHandler
    {
        internal string Text = "";
        internal bool Fail;
        internal int Requests;
        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (request.Method != HttpMethod.Get || request.RequestUri?.AbsoluteUri !=
                "https://raw.githubusercontent.com/Valhaimerd/mods-server/main/installer/recommended-keybinds.txt" || request.Content is not null)
                throw new Exception("Controls must only GET the public keybind profile.");
            Requests++;
            return Task.FromResult(new HttpResponseMessage(Fail ? System.Net.HttpStatusCode.ServiceUnavailable : System.Net.HttpStatusCode.OK)
            { Content = new StringContent(Text) });
        }
    }

    private static Dictionary<string, string> LoadProfile(string text)
    {
        if (text.Length is 0 or > 65536)
            throw new InvalidDataException("Downloaded controls profile is empty or too large; controls were not changed.");
        using var reader = new StringReader(text.TrimStart('\uFEFF'));
        var profile = new Dictionary<string, string>(StringComparer.Ordinal);
        while (reader.ReadLine() is { } line)
        {
            var separator = line.IndexOf(':');
            if (separator < 0 || !Regex.IsMatch(line, @"\Akey_[^:\s]+:key\.(keyboard|mouse)\.[a-z0-9._]+(?::(SHIFT|CONTROL|ALT))?\z") ||
                !profile.TryAdd(line[..separator], line))
            {
                throw new InvalidDataException("Downloaded controls profile contains an invalid or duplicate entry; controls were not changed.");
            }
        }

        if (profile.Count == 0) throw new InvalidDataException("Downloaded controls profile has no bindings.");
        return profile;
    }
}
