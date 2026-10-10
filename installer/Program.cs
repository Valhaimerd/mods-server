using System.ComponentModel;
using System.Diagnostics;
using System.Net.Http.Json;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json.Serialization;

namespace ModsServerInstaller;

internal static class Program
{
    private const string Repository = "Valhaimerd/mods-server";
    private const string Branch = "main";
    private const string HandbookFileName = "modpack-player-handbook.pdf";
    private static string HandbookUrl => $"https://github.com/{Repository}/blob/{Branch}/{HandbookFileName}";
    private static readonly string[] SourceFolders = ["mods", "mod store"];

    public static async Task<int> Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;

        try
        {
            var options = Options.Parse(args);
            if (options.ShowHelp)
            {
                PrintHelp();
                return 0;
            }

            if (options.SelfTest)
            {
                await RunSelfTestAsync();
                return 0;
            }

            var target = Path.GetFullPath(options.Target ?? DefaultModsFolder());
            if (!Path.GetFileName(target.TrimEnd(Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar))
                    .Equals("mods", StringComparison.OrdinalIgnoreCase))
            {
                throw new ArgumentException("The target folder must be named 'mods'.");
            }

            Console.WriteLine("Minecraft Mod Pack Installer");
            Console.WriteLine($"Target: {target}");
            Console.WriteLine("Reading the current pack from GitHub...");

            using var http = new HttpClient { Timeout = TimeSpan.FromMinutes(30) };
            http.DefaultRequestHeaders.UserAgent.ParseAdd("ModsServerInstaller/1.0");

            var package = await LoadPackageAsync(http);
            Directory.CreateDirectory(target);
            var plan = await BuildPlanAsync(target, package);

            PrintPlan(plan);
            if (!plan.HasChanges)
            {
                Console.WriteLine("Everything is already up to date.");
                OpenHandbookAfterRun(options.CheckOnly, cancelled: false, updateSucceeded: true);
                return 0;
            }

            if (options.CheckOnly)
            {
                Console.WriteLine("Check only: no files were changed.");
                return 0;
            }

            if (!options.Yes && !Confirm())
            {
                Console.WriteLine("Cancelled. No files were changed.");
                return 0;
            }

            var backup = await ApplyPlanAsync(http, target, plan);
            Console.WriteLine("Install/repair completed successfully.");
            if (backup is not null)
            {
                Console.WriteLine($"Backup: {backup}");
            }

            OpenHandbookAfterRun(options.CheckOnly, cancelled: false, updateSucceeded: true);
            return 0;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Error: {ex.Message}");
            return 1;
        }
        finally
        {
            if (args.Length == 0 && !Console.IsInputRedirected)
            {
                Console.WriteLine();
                Console.Write("Press Enter to close...");
                Console.ReadLine();
            }
        }
    }

    private static string DefaultModsFolder() =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft", "mods");

    private static async Task<IReadOnlyList<PackageFile>> LoadPackageAsync(HttpClient http)
    {
        var package = new Dictionary<string, PackageFile>(StringComparer.OrdinalIgnoreCase);

        foreach (var folder in SourceFolders)
        {
            var escaped = Uri.EscapeDataString(folder);
            var url = $"https://api.github.com/repos/{Repository}/contents/{escaped}?ref={Branch}";
            var entries = await http.GetFromJsonAsync<List<GitHubEntry>>(url)
                ?? throw new InvalidOperationException($"GitHub returned no files for '{folder}'.");

            foreach (var entry in entries.Where(x => x.Type == "file" && x.Name.EndsWith(".jar", StringComparison.OrdinalIgnoreCase)))
            {
                if (entry.DownloadUrl is null)
                {
                    throw new InvalidOperationException($"GitHub did not provide a download URL for '{entry.Name}'.");
                }

                if (!package.TryAdd(entry.Name, new PackageFile(entry.Name, entry.Sha, entry.Size, entry.DownloadUrl)))
                {
                    throw new InvalidOperationException($"The pack contains two files named '{entry.Name}'.");
                }
            }
        }

        return package.Values.OrderBy(x => x.Name, StringComparer.OrdinalIgnoreCase).ToArray();
    }

    private static async Task<InstallPlan> BuildPlanAsync(string target, IReadOnlyList<PackageFile> package)
    {
        var local = Directory.EnumerateFiles(target, "*.jar", SearchOption.TopDirectoryOnly)
            .ToDictionary(path => Path.GetFileName(path)!, StringComparer.OrdinalIgnoreCase);
        var wanted = package.ToDictionary(x => x.Name, StringComparer.OrdinalIgnoreCase);
        var downloads = new List<PackageFile>();
        var unchanged = 0;

        foreach (var file in package)
        {
            if (!local.TryGetValue(file.Name, out var path) ||
                !string.Equals(await GitBlobShaAsync(path), file.Sha, StringComparison.OrdinalIgnoreCase))
            {
                downloads.Add(file);
            }
            else
            {
                unchanged++;
            }
        }

        var extras = local
            .Where(x => !wanted.ContainsKey(x.Key))
            .Select(x => x.Value)
            .OrderBy(Path.GetFileName, StringComparer.OrdinalIgnoreCase)
            .ToArray();

        return new InstallPlan(downloads, extras, unchanged);
    }

    private static void PrintPlan(InstallPlan plan)
    {
        var bytes = plan.Downloads.Sum(x => x.Size);
        Console.WriteLine();
        Console.WriteLine($"Already correct: {plan.Unchanged}");
        Console.WriteLine($"Missing or changed: {plan.Downloads.Count} ({FormatBytes(bytes)})");
        Console.WriteLine($"Extra mods to move into backup: {plan.Extras.Count}");
        Console.WriteLine();
    }

    private static bool Confirm()
    {
        Console.Write("Close Minecraft, then type YES to continue: ");
        return string.Equals(Console.ReadLine(), "YES", StringComparison.OrdinalIgnoreCase);
    }

    private static async Task<string?> ApplyPlanAsync(HttpClient http, string target, InstallPlan plan)
    {
        var parent = Directory.GetParent(target)?.FullName
            ?? throw new InvalidOperationException("The target folder has no parent directory.");
        var staging = Path.Combine(parent, $".mods-installer-staging-{Guid.NewGuid():N}");
        var backup = Path.Combine(parent, "modpack-backups", $"{DateTime.Now:yyyyMMdd-HHmmss}-{Guid.NewGuid():N}"[..22]);
        var backedUp = new List<(string Original, string Backup)>();
        var installed = new List<string>();

        Directory.CreateDirectory(staging);
        try
        {
            for (var i = 0; i < plan.Downloads.Count; i++)
            {
                var file = plan.Downloads[i];
                var stagedPath = Path.Combine(staging, file.Name);
                Console.WriteLine($"Downloading {i + 1}/{plan.Downloads.Count}: {file.Name}");

                using var response = await http.GetAsync(file.DownloadUrl, HttpCompletionOption.ResponseHeadersRead);
                response.EnsureSuccessStatusCode();
                await using (var source = await response.Content.ReadAsStreamAsync())
                await using (var destination = new FileStream(stagedPath, FileMode.CreateNew, FileAccess.Write, FileShare.None, 81920, true))
                {
                    await source.CopyToAsync(destination);
                }

                var actualSha = await GitBlobShaAsync(stagedPath);
                if (!string.Equals(actualSha, file.Sha, StringComparison.OrdinalIgnoreCase))
                {
                    throw new InvalidDataException($"Hash verification failed for '{file.Name}'.");
                }
            }

            var pathsToBackup = plan.Extras
                .Concat(plan.Downloads.Select(x => Path.Combine(target, x.Name)).Where(File.Exists))
                .Distinct(StringComparer.OrdinalIgnoreCase)
                .ToArray();

            if (pathsToBackup.Length > 0)
            {
                Directory.CreateDirectory(backup);
                foreach (var original in pathsToBackup)
                {
                    var backupPath = Path.Combine(backup, Path.GetFileName(original));
                    File.Move(original, backupPath);
                    backedUp.Add((original, backupPath));
                }
            }

            foreach (var file in plan.Downloads)
            {
                var destination = Path.Combine(target, file.Name);
                File.Move(Path.Combine(staging, file.Name), destination);
                installed.Add(destination);
            }

            return pathsToBackup.Length > 0 ? backup : null;
        }
        catch
        {
            foreach (var path in installed.Where(File.Exists))
            {
                File.Delete(path);
            }

            foreach (var item in backedUp.AsEnumerable().Reverse())
            {
                if (File.Exists(item.Backup))
                {
                    File.Move(item.Backup, item.Original, true);
                }
            }

            throw;
        }
        finally
        {
            if (Directory.Exists(staging))
            {
                try
                {
                    Directory.Delete(staging, true);
                }
                catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
                {
                    Console.Error.WriteLine($"Warning: temporary files remain at '{staging}'.");
                }
            }
        }
    }

    private static async Task<string> GitBlobShaAsync(string path)
    {
        var length = new FileInfo(path).Length;
        using var hash = IncrementalHash.CreateHash(HashAlgorithmName.SHA1);
        hash.AppendData(Encoding.UTF8.GetBytes($"blob {length}\0"));

        var buffer = new byte[81920];
        await using var stream = File.OpenRead(path);
        int read;
        while ((read = await stream.ReadAsync(buffer)) > 0)
        {
            hash.AppendData(buffer, 0, read);
        }

        return Convert.ToHexString(hash.GetHashAndReset()).ToLowerInvariant();
    }

    private static string FormatBytes(long bytes)
    {
        string[] units = ["B", "KB", "MB", "GB"];
        var value = (double)bytes;
        var unit = 0;
        while (value >= 1024 && unit < units.Length - 1)
        {
            value /= 1024;
            unit++;
        }

        return $"{value:0.##} {units[unit]}";
    }

    private static bool ShouldOpenHandbook(bool checkOnly, bool cancelled, bool updateSucceeded) =>
        updateSucceeded && !checkOnly && !cancelled;

    private static void OpenHandbookAfterRun(bool checkOnly, bool cancelled, bool updateSucceeded)
    {
        if (!ShouldOpenHandbook(checkOnly, cancelled, updateSucceeded))
        {
            return;
        }

        try
        {
            Process.Start(new ProcessStartInfo(HandbookUrl) { UseShellExecute = true });
            Console.WriteLine("Opened the player handbook in your browser.");
        }
        catch (Exception ex) when (ex is InvalidOperationException or Win32Exception)
        {
            Console.WriteLine($"Could not open a browser automatically. View the handbook at: {HandbookUrl}");
        }
    }

    private static async Task RunSelfTestAsync()
    {
        if (HandbookFileName != "modpack-player-handbook.pdf" ||
            HandbookUrl != "https://github.com/Valhaimerd/mods-server/blob/main/modpack-player-handbook.pdf")
        {
            throw new InvalidOperationException("Self-test failed: handbook viewer URL is not stable.");
        }

        if (!ShouldOpenHandbook(checkOnly: false, cancelled: false, updateSucceeded: true) ||
            ShouldOpenHandbook(checkOnly: true, cancelled: false, updateSucceeded: true) ||
            ShouldOpenHandbook(checkOnly: false, cancelled: true, updateSucceeded: true) ||
            ShouldOpenHandbook(checkOnly: false, cancelled: false, updateSucceeded: false))
        {
            throw new InvalidOperationException("Self-test failed: handbook launch gating is incorrect.");
        }

        var path = Path.GetTempFileName();
        try
        {
            await File.WriteAllTextAsync(path, "test content\n", new UTF8Encoding(false));
            var actual = await GitBlobShaAsync(path);
            const string expected = "d670460b4b4aece5915caf5c68d12f560a9fe3e4";
            if (actual != expected)
            {
                throw new InvalidOperationException($"Self-test failed: expected {expected}, got {actual}.");
            }

            Console.WriteLine("Self-test passed.");
        }
        finally
        {
            File.Delete(path);
        }
    }

    private static void PrintHelp()
    {
        Console.WriteLine("ModsServerInstaller [--check] [--yes] [--target <mods-folder>]");
        Console.WriteLine();
        Console.WriteLine("  --check    Show required changes without modifying files.");
        Console.WriteLine("  --yes      Apply changes without the confirmation prompt.");
        Console.WriteLine("  --target   Use another launcher instance's mods folder.");
    }

    private sealed record PackageFile(string Name, string Sha, long Size, string DownloadUrl);
    private sealed record InstallPlan(IReadOnlyList<PackageFile> Downloads, IReadOnlyList<string> Extras, int Unchanged)
    {
        public bool HasChanges => Downloads.Count > 0 || Extras.Count > 0;
    }

    private sealed record GitHubEntry(
        [property: JsonPropertyName("name")] string Name,
        [property: JsonPropertyName("type")] string Type,
        [property: JsonPropertyName("sha")] string Sha,
        [property: JsonPropertyName("size")] long Size,
        [property: JsonPropertyName("download_url")] string? DownloadUrl);

    private sealed record Options(string? Target, bool CheckOnly, bool Yes, bool ShowHelp, bool SelfTest)
    {
        public static Options Parse(string[] args)
        {
            string? target = null;
            var check = false;
            var yes = false;
            var help = false;
            var selfTest = false;

            for (var i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--target" when i + 1 < args.Length:
                        target = args[++i];
                        break;
                    case "--check":
                        check = true;
                        break;
                    case "--yes":
                        yes = true;
                        break;
                    case "--help" or "-h":
                        help = true;
                        break;
                    case "--self-test":
                        selfTest = true;
                        break;
                    default:
                        throw new ArgumentException($"Unknown or incomplete option: {args[i]}");
                }
            }

            return new Options(target, check, yes, help, selfTest);
        }
    }
}
