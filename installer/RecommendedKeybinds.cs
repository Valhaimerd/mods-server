using System.Text;
using System.Text.RegularExpressions;

namespace ModsServerInstaller;

internal sealed record KeybindApplyResult(bool Changed, string? BackupPath);

internal static class RecommendedKeybinds
{
    private const string ResourceName = "ModsServerInstaller.RecommendedKeybinds";

    public static async Task<KeybindApplyResult> ApplyAsync(string modsFolder)
    {
        var gameFolder = Directory.GetParent(modsFolder)?.FullName
            ?? throw new InvalidOperationException("The mods folder has no parent directory.");
        var optionsPath = Path.Combine(gameFolder, "options.txt");
        var profile = LoadProfile();
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
        const string original = "music:0.3\nkey_key.emotecraft.fastchoose:key.keyboard.b\nkey_custom.mod:key.keyboard.p\n";
        try
        {
            await File.WriteAllTextAsync(options, original);
            var result = await ApplyAsync(Path.Combine(root, "mods"));
            var saved = await File.ReadAllTextAsync(options);
            var profile = LoadProfile();
            if (!result.Changed || result.BackupPath is null ||
                await File.ReadAllTextAsync(result.BackupPath) != original ||
                !saved.Contains("music:0.3\n") || !saved.Contains("key_custom.mod:key.keyboard.p\n") ||
                profile.Values.Any(line => !saved.Split('\n').Contains(line)) ||
                profile["key_key.emotecraft.fastchoose"] != "key_key.emotecraft.fastchoose:key.keyboard.g" ||
                profile["key_key.punchy.inspect"] != "key_key.punchy.inspect:key.keyboard.i" ||
                profile["key_key.armorhider.open_menu"] != "key_key.armorhider.open_menu:key.keyboard.k" ||
                profile["key_key.special_ability"] != "key_key.special_ability:key.keyboard.unknown")
                throw new InvalidOperationException("Self-test failed: current controls merge/backup.");
            if ((await ApplyAsync(Path.Combine(root, "mods"))).Changed || await File.ReadAllTextAsync(options) != saved)
                throw new InvalidOperationException("Self-test failed: controls merge must be idempotent.");
        }
        finally { Directory.Delete(root, recursive: true); }
    }

    private static Dictionary<string, string> LoadProfile()
    {
        using var stream = typeof(RecommendedKeybinds).Assembly.GetManifestResourceStream(ResourceName)
            ?? throw new InvalidOperationException("The recommended keybind profile is missing from the installer.");
        using var reader = new StreamReader(stream, Encoding.UTF8);
        var profile = new Dictionary<string, string>(StringComparer.Ordinal);
        while (reader.ReadLine() is { } line)
        {
            var separator = line.IndexOf(':');
            if (separator < 0 || !line.StartsWith("key_", StringComparison.Ordinal) ||
                !profile.TryAdd(line[..separator], line))
            {
                throw new InvalidDataException("The embedded keybind profile contains an invalid or duplicate entry.");
            }
        }

        return profile;
    }
}
