using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;

namespace ModsServerInstaller;

internal static class PunchyCompatibility
{
    private static readonly string[] RpgItemPatterns =
        ["arsenal:*", "paladins:*", "archers:*", "rogues:*", "wizards:*"];

    public static async Task<string?> ApplyRecommendedSettingsAsync(string modsFolder)
    {
        var gameFolder = Directory.GetParent(modsFolder)?.FullName
            ?? throw new InvalidOperationException("The mods folder has no parent directory.");
        var configPath = Path.Combine(gameFolder, "config", "punchy", "punchy_config.json");
        if (!File.Exists(configPath))
        {
            return null; // The bundled Punchy JAR creates the recommended defaults on first launch.
        }

        var config = JsonNode.Parse(await File.ReadAllTextAsync(configPath)) as JsonObject
            ?? throw new InvalidDataException("Punchy configuration is not a JSON object.");
        var changed = false;
        if (config["bettercombatCompat"]?.GetValue<bool>() != true)
        {
            config["bettercombatCompat"] = true;
            changed = true;
        }

        var blacklist = config["itemBlacklist"] as JsonArray;
        if (blacklist is null)
        {
            blacklist = [];
            config["itemBlacklist"] = blacklist;
            changed = true;
        }
        var dualHanded = config["blacklistApplyDualHanded"] as JsonObject;
        if (dualHanded is null)
        {
            dualHanded = [];
            config["blacklistApplyDualHanded"] = dualHanded;
            changed = true;
        }
        foreach (var pattern in RpgItemPatterns)
        {
            if (!blacklist.Any(item => item?.GetValue<string>() == pattern))
            {
                blacklist.Add(pattern);
                changed = true;
            }
            if (dualHanded[pattern]?.GetValue<bool>() != true)
            {
                dualHanded[pattern] = true;
                changed = true;
            }
        }
        if (!changed)
        {
            return null;
        }

        var backupFolder = Path.Combine(gameFolder, "modpack-backups",
            $"punchy-config-{DateTime.Now:yyyyMMdd-HHmmss}-{Guid.NewGuid():N}");
        Directory.CreateDirectory(backupFolder);
        var backupPath = Path.Combine(backupFolder, "punchy_config.json");
        File.Copy(configPath, backupPath);

        var temporaryPath = configPath + $".{Guid.NewGuid():N}.tmp";
        try
        {
            await File.WriteAllTextAsync(temporaryPath,
                config.ToJsonString(new JsonSerializerOptions { WriteIndented = true }) + "\n",
                new UTF8Encoding(false));
            File.Move(temporaryPath, configPath, overwrite: true);
        }
        finally
        {
            if (File.Exists(temporaryPath))
            {
                File.Delete(temporaryPath);
            }
        }

        return backupPath;
    }
}
