using System.Text;
using System.Text.Json.Nodes;

namespace ModsServerInstaller;

internal sealed record VectorsApplyResult(bool Changed, string? BackupPath);

internal static class VectorsProfile
{
    public static async Task<VectorsApplyResult> ApplyAsync(string modsFolder)
    {
        var gameFolder = Directory.GetParent(modsFolder)?.FullName
            ?? throw new InvalidOperationException("The mods folder has no parent directory.");
        var configPath = Path.Combine(gameFolder, "config", "vectors.json");
        var exists = File.Exists(configPath);
        var config = exists
            ? JsonNode.Parse(await File.ReadAllTextAsync(configPath)) as JsonObject
                ?? throw new InvalidDataException("Vectors config must be a JSON object.")
            : new JsonObject();
        using var stream = typeof(VectorsProfile).Assembly.GetManifestResourceStream("ModsServerInstaller.VectorsPerformance")
            ?? throw new InvalidOperationException("The Vectors performance preset is missing from the installer.");
        var preset = JsonNode.Parse(stream)!.AsObject();
        if (preset.All(entry => JsonNode.DeepEquals(config[entry.Key], entry.Value)))
            return new VectorsApplyResult(false, null);
        foreach (var entry in preset) config[entry.Key] = entry.Value?.DeepClone();
        string? backupPath = null;
        if (exists)
        {
            var backupFolder = Path.Combine(gameFolder, "modpack-backups", $"vectors-{Guid.NewGuid():N}");
            Directory.CreateDirectory(backupFolder);
            backupPath = Path.Combine(backupFolder, "vectors.json");
            File.Copy(configPath, backupPath);
        }
        var configFolder = Path.GetDirectoryName(configPath)!;
        Directory.CreateDirectory(configFolder);
        var temporaryPath = Path.Combine(configFolder, $".vectors-{Guid.NewGuid():N}.tmp");
        try
        {
            await File.WriteAllTextAsync(temporaryPath,
                config.ToJsonString(new System.Text.Json.JsonSerializerOptions { WriteIndented = true }) + "\n",
                new UTF8Encoding(false));
            File.Move(temporaryPath, configPath, overwrite: true);
        }
        finally { if (File.Exists(temporaryPath)) File.Delete(temporaryPath); }
        return new VectorsApplyResult(true, backupPath);
    }

    internal static async Task RunSelfTestAsync()
    {
        var root = Path.Combine(Path.GetTempPath(), $"vectors-installer-{Guid.NewGuid():N}");
        var mods = Path.Combine(root, "mods");
        var config = Path.Combine(root, "config", "vectors.json");
        Directory.CreateDirectory(Path.GetDirectoryName(config)!);
        const string original = """
            {"profile":"balanced","passiveDistance":192,"theme":"light","overlay":true,"custom":{"keep":true}}
            """;
        try
        {
            await File.WriteAllTextAsync(config, original);
            var result = await ApplyAsync(mods);
            var json = JsonNode.Parse(await File.ReadAllTextAsync(config))!;
            if (!result.Changed || json["profile"]!.GetValue<string>() != "performance" ||
                json["passiveDistance"]!.GetValue<int>() != 64 ||
                json["theme"]!.GetValue<string>() != "light" ||
                json["overlay"]!.GetValue<bool>() != true ||
                json["custom"]!["keep"]!.GetValue<bool>() != true ||
                result.BackupPath is null || await File.ReadAllTextAsync(result.BackupPath) != original)
                throw new InvalidOperationException("Self-test failed: Vectors performance merge/backup.");
            var saved = await File.ReadAllTextAsync(config);
            var again = await ApplyAsync(mods);
            if (again.Changed || again.BackupPath is not null || await File.ReadAllTextAsync(config) != saved)
                throw new InvalidOperationException("Self-test failed: Vectors merge must be idempotent.");
            File.Delete(config);
            var fresh = await ApplyAsync(mods);
            if (!fresh.Changed || fresh.BackupPath is not null ||
                JsonNode.Parse(await File.ReadAllTextAsync(config))!["profile"]!.GetValue<string>() != "performance")
                throw new InvalidOperationException("Self-test failed: fresh Vectors config.");
            await File.WriteAllTextAsync(config, "not JSON");
            try
            {
                await ApplyAsync(mods);
                throw new InvalidOperationException("Self-test failed: malformed Vectors config was accepted.");
            }
            catch (System.Text.Json.JsonException) { }
            if (await File.ReadAllTextAsync(config) != "not JSON")
                throw new InvalidOperationException("Self-test failed: malformed Vectors config was changed.");
        }
        finally { Directory.Delete(root, recursive: true); }
    }
}
