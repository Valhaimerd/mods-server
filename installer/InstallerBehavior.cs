namespace ModsServerInstaller;

internal enum InstallMode
{
    Client,
    Server,
}

internal sealed record MenuOption(string Label, bool Enabled = true);

internal static class InstallerBehavior
{
    private static readonly IReadOnlyList<string> ClientSourceFolders =
        Array.AsReadOnly(new[] { "mods", "mod store" });
    private static readonly IReadOnlyList<string> ServerSourceFolders =
        Array.AsReadOnly(new[] { "mods" });

    public static IReadOnlyList<string> SourceFoldersFor(InstallMode mode) => mode switch
    {
        InstallMode.Client => ClientSourceFolders,
        InstallMode.Server => ServerSourceFolders,
        _ => throw new ArgumentOutOfRangeException(nameof(mode), mode, "Unknown install mode."),
    };

    public static bool IsModsFolder(string path)
    {
        if (string.IsNullOrWhiteSpace(path))
        {
            return false;
        }

        var normalized = Path.TrimEndingDirectorySeparator(path);
        return Path.GetFileName(normalized).Equals("mods", StringComparison.OrdinalIgnoreCase);
    }

    public static int FirstEnabledIndex(IReadOnlyList<MenuOption> options)
    {
        ArgumentNullException.ThrowIfNull(options);

        for (var i = 0; i < options.Count; i++)
        {
            if (options[i].Enabled)
            {
                return i;
            }
        }

        throw new InvalidOperationException("The menu has no enabled options.");
    }

    public static int MoveSelection(IReadOnlyList<MenuOption> options, int currentIndex, int direction)
    {
        ArgumentNullException.ThrowIfNull(options);
        if (options.Count == 0)
        {
            throw new InvalidOperationException("The menu has no enabled options.");
        }

        if (direction is not (-1 or 1))
        {
            throw new ArgumentOutOfRangeException(nameof(direction), "Direction must be -1 or 1.");
        }

        if (currentIndex < 0 || currentIndex >= options.Count)
        {
            throw new ArgumentOutOfRangeException(nameof(currentIndex));
        }

        for (var distance = 1; distance <= options.Count; distance++)
        {
            var index = (currentIndex + (direction * distance) + options.Count) % options.Count;
            if (options[index].Enabled)
            {
                return index;
            }
        }

        throw new InvalidOperationException("The menu has no enabled options.");
    }

    public static bool ShouldUseInteractiveWizard(int argumentCount, bool inputRedirected, bool outputRedirected) =>
        argumentCount == 0 && !inputRedirected && !outputRedirected;
}
