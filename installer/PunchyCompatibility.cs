using System.Text;
using System.Text.Json.Nodes;

namespace ModsServerInstaller;

internal sealed record PunchyApplyResult(bool Changed, string? BackupPath);

internal static class PunchyCompatibility
{
    public static async Task<PunchyApplyResult> ApplyAsync(string modsFolder)
    {
        var gameFolder = Directory.GetParent(modsFolder)?.FullName
            ?? throw new InvalidOperationException("The mods folder has no parent directory.");
        var configPath = Path.Combine(gameFolder, "config", "punchy", "punchy_config.json");
        var exists = File.Exists(configPath);
        var config = exists
            ? JsonNode.Parse(await File.ReadAllTextAsync(configPath)) as JsonObject
                ?? throw new InvalidDataException("Punchy config must be a JSON object.")
            : new JsonObject();
        if (config["bettercombatCompat"]?.ToJsonString() == "true")
            return new PunchyApplyResult(false, null);

        config["bettercombatCompat"] = true;
        string? backupPath = null;
        if (exists)
        {
            var backupFolder = Path.Combine(gameFolder, "modpack-backups", $"punchy-{Guid.NewGuid():N}");
            Directory.CreateDirectory(backupFolder);
            backupPath = Path.Combine(backupFolder, "punchy_config.json");
            File.Copy(configPath, backupPath);
        }
        var configFolder = Path.GetDirectoryName(configPath)!;
        Directory.CreateDirectory(configFolder);
        var temporaryPath = Path.Combine(configFolder, $".punchy-{Guid.NewGuid():N}.tmp");
        try
        {
            await File.WriteAllTextAsync(temporaryPath,
                config.ToJsonString(new System.Text.Json.JsonSerializerOptions { WriteIndented = true }) + "\n",
                new UTF8Encoding(false));
            File.Move(temporaryPath, configPath, overwrite: true);
        }
        finally { if (File.Exists(temporaryPath)) File.Delete(temporaryPath); }
        return new PunchyApplyResult(true, backupPath);
    }

    internal static async Task RunSelfTestAsync()
    {
        var root = Path.Combine(Path.GetTempPath(), $"punchy-installer-{Guid.NewGuid():N}");
        var mods = Path.Combine(root, "mods");
        var config = Path.Combine(root, "config", "punchy", "punchy_config.json");
        Directory.CreateDirectory(Path.GetDirectoryName(config)!);
        const string original = """
            {"bettercombatCompat":false,"animationSpeed":5.5,"itemBlacklist":["custom:weapon"],"custom":{"keep":true}}
            """;
        try
        {
            await File.WriteAllTextAsync(config, original);
            var result = await ApplyAsync(mods);
            var json = JsonNode.Parse(await File.ReadAllTextAsync(config))!;
            if (!result.Changed || json["bettercombatCompat"]!.GetValue<bool>() != true ||
                json["animationSpeed"]!.GetValue<double>() != 5.5 ||
                json["itemBlacklist"]![0]!.GetValue<string>() != "custom:weapon" ||
                json["custom"]!["keep"]!.GetValue<bool>() != true ||
                result.BackupPath is null || await File.ReadAllTextAsync(result.BackupPath) != original)
                throw new InvalidOperationException("Self-test failed: Punchy compatibility merge/backup.");
            var saved = await File.ReadAllTextAsync(config);
            var again = await ApplyAsync(mods);
            if (again.Changed || again.BackupPath is not null || await File.ReadAllTextAsync(config) != saved)
                throw new InvalidOperationException("Self-test failed: Punchy merge must be idempotent.");
            File.Delete(config);
            var fresh = await ApplyAsync(mods);
            if (!fresh.Changed || fresh.BackupPath is not null ||
                JsonNode.Parse(await File.ReadAllTextAsync(config))!["bettercombatCompat"]!.GetValue<bool>() != true)
                throw new InvalidOperationException("Self-test failed: fresh Punchy config.");
            await File.WriteAllTextAsync(config, "not JSON");
            try
            {
                await ApplyAsync(mods);
                throw new InvalidOperationException("Self-test failed: malformed Punchy config was accepted.");
            }
            catch (System.Text.Json.JsonException) { }
            if (await File.ReadAllTextAsync(config) != "not JSON")
                throw new InvalidOperationException("Self-test failed: malformed Punchy config was changed.");
        }
        finally { Directory.Delete(root, recursive: true); }
    }
}
