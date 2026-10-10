using System.Windows.Forms;

namespace ModsServerInstaller;

internal enum MenuAction
{
    Continue,
    Submit,
    Cancel,
}

internal sealed record MenuKeyResult(int SelectedIndex, MenuAction Action);

internal static class InteractiveMenu
{
    public const string Header = "Minecraft Java RPG Series by Valhaimerd";

    public static MenuKeyResult HandleKey(ConsoleKey key, IReadOnlyList<MenuOption> options, int selectedIndex)
    {
        ArgumentNullException.ThrowIfNull(options);
        if (selectedIndex < 0 || selectedIndex >= options.Count || !options[selectedIndex].Enabled)
        {
            throw new ArgumentOutOfRangeException(nameof(selectedIndex), "Selection must point to an enabled menu option.");
        }

        return key switch
        {
            ConsoleKey.UpArrow => new MenuKeyResult(
                InstallerBehavior.MoveSelection(options, selectedIndex, -1), MenuAction.Continue),
            ConsoleKey.DownArrow => new MenuKeyResult(
                InstallerBehavior.MoveSelection(options, selectedIndex, 1), MenuAction.Continue),
            ConsoleKey.Enter => new MenuKeyResult(selectedIndex, MenuAction.Submit),
            ConsoleKey.Escape => new MenuKeyResult(selectedIndex, MenuAction.Cancel),
            _ => new MenuKeyResult(selectedIndex, MenuAction.Continue),
        };
    }

    public static int Select(string title, IReadOnlyList<MenuOption> options)
    {
        ArgumentNullException.ThrowIfNull(options);
        var selectedIndex = InstallerBehavior.FirstEnabledIndex(options);

        while (true)
        {
            Console.Clear();
            Console.WriteLine(Header);
            Console.WriteLine();
            Console.WriteLine(title);
            Console.WriteLine();

            var originalColor = Console.ForegroundColor;
            try
            {
                for (var i = 0; i < options.Count; i++)
                {
                    var option = options[i];
                    Console.ForegroundColor = !option.Enabled
                        ? ConsoleColor.DarkGray
                        : i == selectedIndex
                            ? ConsoleColor.Yellow
                            : originalColor;
                    Console.WriteLine($"{(i == selectedIndex ? "> " : "  ")}{option.Label}");
                }
            }
            finally
            {
                Console.ForegroundColor = originalColor;
            }

            Console.WriteLine();
            Console.WriteLine("Use ↑/↓ to move, Enter to select, Esc to go back.");

            var result = HandleKey(Console.ReadKey(intercept: true).Key, options, selectedIndex);
            selectedIndex = result.SelectedIndex;
            if (result.Action == MenuAction.Submit)
            {
                return selectedIndex;
            }

            if (result.Action == MenuAction.Cancel)
            {
                return -1;
            }
        }
    }

    public static string? PickFolder(string description, string? initialPath)
    {
        string? selectedPath = null;
        Exception? failure = null;

        var thread = new Thread(() =>
        {
            try
            {
                using var dialog = new FolderBrowserDialog
                {
                    Description = description,
                    ShowNewFolderButton = false,
                };

                if (!string.IsNullOrWhiteSpace(initialPath) && Directory.Exists(initialPath))
                {
                    dialog.SelectedPath = initialPath;
                }

                if (dialog.ShowDialog() == DialogResult.OK)
                {
                    selectedPath = dialog.SelectedPath;
                }
            }
            catch (Exception ex)
            {
                failure = ex;
            }
        });

        thread.SetApartmentState(ApartmentState.STA);
        thread.Start();
        thread.Join();

        if (failure is not null)
        {
            throw new InvalidOperationException("The Windows folder picker could not be opened.", failure);
        }

        return selectedPath;
    }
}
