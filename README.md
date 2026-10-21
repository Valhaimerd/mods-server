# Mods Server Pack

This repository contains the current Minecraft mod collection and a Windows installer that keeps a player's mod folder and bundled emote pack in sync with it.

The current pack requires Minecraft 26.2, NeoForge **26.2.0.90 or newer**, and Java 25 or newer. Install the matching NeoForge version on both client and server before syncing mods; the mod installer updates JARs but does not change the loader.

See [the player handbook](docs/MODLIST.md) for setup guidance, changed mechanics, controls, exploration, building, and quality-of-life features in the current pack. Libraries, APIs, and compatibility-only JARs are intentionally omitted from its gameplay catalog.

Wizards' first two active tiers use a [local magic balance rework](docs/patches/magic-revision-five/README.md): Missiles has an 8-16-second channel-based cooldown, Explosion centers on the target, Charged Beam grows during its channel, Shattering Lance separates direct and splash damage, and Frost Shield grants damage immunity followed by a freeze burst. Wizards, Skill Tree and Spell Engine retain their active filenames.

## Installer

Download [ModsServerInstaller.exe](https://github.com/Valhaimerd/mods-server/releases/latest/download/ModsServerInstaller.exe), close Minecraft, and run it.

With no command-line options, use the arrow keys and Enter to choose Client or Server; Esc goes back. The installer does not ask for a typed confirmation or path.

For Client, select TLauncher (the only currently enabled launcher), then choose whether to apply Recommended Keybinds. Yes merges the pack's keybind profile into `%APPDATA%\.minecraft\options.txt`, backing up an existing file under `modpack-backups`; No leaves controls untouched. Other settings such as graphics and audio are preserved. Either choice continues the usual install to `%APPDATA%\.minecraft\mods` with JARs from both `mods` and `mod store`. Legacy Launcher, Prism Launcher, and CurseForge App are shown as future options but are not selectable yet.

For Server, choose `Select file location…` and select the server's existing `mods` directory. The folder name must be `mods` (case-insensitive). Server installs use only the repository's `mods` folder; UI/client JARs from `mod store` are excluded.

Client installs and repairs also download the ZIPs from `emotes/`, verify their Git content hashes, and automatically extract 352 emotes across thirteen bundled sets to `%APPDATA%\.minecraft\emotes\Mods Server Pack`. No manual extraction is needed. This runs independently of the keybind choice, including when the mods are already current. For `--target`, the emotes go beside that instance's `mods` folder instead. Server installs do not install the emote pack; `--check` reports its status without downloading or extracting the ZIPs or creating folders.

Installer v1.4.5 displays its version on startup and in the menu. Replace older downloaded copies with the latest executable; v1.4.3 still extracts Dances 5 into the parent folder. Versions 1.4.4 and newer put every emote pack in its own named folder and migrate tracked legacy Dances files into backups. It tracks Dances in `modpack-state/emotes.json` and each additional ZIP in its own `modpack-state/emotes-<pack>.json`, skips downloading intact current packs, and repairs missing or changed pack files. Replaced files and unchanged retired pack files move to `modpack-backups/emotes-*`; failures during replacement restore the previous files. Personal emotes outside the managed subfolder and untracked files with other names inside it are preserved. An existing file that shares an incoming pack filename is backed up before replacement. A user-edited retired pack file is also kept. Each ZIP must contain flat `.emotecraft` files; all sets use separate subfolders beneath `Mods Server Pack`; see [emote pack maintenance](emotes/README.md).

Advanced Loot Info 2.4.0, Advanced Worldgen Info 1.3.2, ALICompat 1.3.0, and Advanced Core Info 1.5.0 are in `mods` for client/server data sharing. Their initial information collection can take several minutes; the pack owner reports waiting about eight minutes for ALI to finish. Allow collection to complete before using the reference data. A dedicated server may need a longer `max-tick-time` watchdog allowance for this startup work.

The local PVP Flagging replacement only retains 50% of attributed player-to-player melee, ranged and magic damage. Everyone can fight by default; Better Party controls party friendly fire, while vanilla combat permissions still apply. Flagging, cooldowns, zones, commands and indicators are removed. Other damage is not globally scaled. The same update sets ordinary school power to 3%, Magistrate to 10% power plus 5% magic resistance without a specialization critical-chance bonus, ordinary melee/ranged damage to 3%/2%, and revises Aegis and Hunter. RPG melee/ranged rewards and their Warrior/Archer tooltips are 3%/2%, and Assassin/Rogue attack speed is 2%; Martial Prowess now grants 8% melee damage, 5% attack speed and 10% each physical critical chance/damage without penetration. See [the PvP port](docs/patches/pvp-flagging-26.2/README.md) and [skill balance](docs/patches/skill-overhaul/README.md).

World Bosses 2.0.0 has a same-filename late-game raid update: 7,000–20,000 base HP, 10–20 base HP/s regeneration, and additive +15% HP / +10% regeneration / +10% armor per extra active participant (maximum eight; armor capped at 30). Its twenty Worldbreaker/Shadowbreaker combat items and gear upgrades are retired; existing gear converts to corresponding vanilla equipment. Personal participation-based caches give materials and regular golden apples, with no enchanted golden apples or World Bosses combat gear. See [the raid changes and verification](docs/patches/worldbosses-rpg/README.md).

Alex's Caves Continued is updated to 1.1.2 with the existing Emotecraft preview guard carried forward; the old 1.0.11 is retired. Effortless Building 4.3 is replaced by Effortless Building: Sophisticated 5.1.0, with its radial menu assigned to Alt and its other six controls unbound. The recommended keybind profile was checked against the owner's Minecraft options on 20 October 2026; it contains the Sophisticated Building bindings and no retired Effortless Building bindings. See [the Caves patch and update checks](docs/patches/README.md).

Advancements Reloaded 0.14.0 is a client-only addition in `mod store`, expanding the advancements screen with search and stable ordering. Open it with the existing B binding.

Bosslike Ender Dragon has been removed. Player Animation Library is updated to 1.2.8, with the older 1.2.6 copy retired; Bendable Cuboids 2.0.4 is included as a separate library.

Voltaic Shot, Double Hit, Arrow+ and True Shot Enchantment have been removed from the pack. RPG bow damage rewards are 2%, with their conditional bow tooltip corrected; Adventurer Hunter now grants 6% ranged damage and 5% draw speed, with Luck and Jump Strength removed. Adventurer ordinary and Hunter projectile-speed bonuses grant 25%. Ordinary draw-speed nodes grant 5%; RPG Archer nodes receive no additional draw-speed bonus.

Too Many Bows has been removed from the pack, alongside Arrow+ and True Shot. CrossArms and Expanded Crossbow Enchantings remain, including the Frenzy firing repair for modded crossbows. Capacity is removed. Frenzy retains 30/40/50/60% damage by heat tier, uses 115/125/140/160/185/220% normal spread at zero through five bars and deals 1/2/4 magic self-damage every 20 ticks at its third/fourth/fifth heat bars. Each shot, including the first, applies Slowness II (30% movement reduction) for one second, refreshed while firing. First shots use the same heat damage multiplier as later shots. The Empty - Reload notice disappears after three seconds. Shot 26 empties the burst and requires a full normal crossbow crank; three idle seconds reset only partial counters. The counter is shared across Frenzy crossbows and both hands. Infinity preserves arrows during Frenzy auto-feeding; one normal arrow is still required and free projectiles cannot be collected. Forced overheating durability loss is removed. See [the firing repair](docs/patches/crossarms-firing/README.md).

Artifacts 15.1.3+26.2.1 has a same-filename balance update: reduced melee damage, attack speed, life steal, sprint speed and knockback bonuses; longer defensive cooldowns; and four extra hearts from Crystal Heart. Charm of Shrinking retains half size and adds 1.2x movement speed with refreshing Weakness I. See [the effective values and verification](docs/patches/artifacts-balance/README.md).

Naturally Trimmed 3.5.0 has a same-filename local repair for TradeView previews and short trade pools: only newly generated offers are trimmed, preventing the negative-list-index server crash. TradeView has since been removed from the pack; the trade-range repair remains. See [the patch and isolated runtime results](docs/patches/naturally-trimmed-trades/README.md).

Nameless Trinkets 1.2.4+26.2.1 has a same-filename local balance/server repair: 42 trinkets remain obtainable, duplicate copies cannot stack their effects, 12 removed relics stay registered as inert legacy items, and the requested damage/mining/movement/proc settings override older generated configs. Titan's Mark grants +25% size/+10% attack damage, refreshing +10 maximum hearts and Resistance I for five seconds plus Slowness I for one minute. Fractured Nullstone reduces magic damage by 25%, Rage Mind deals 1.15x marked-type damage, and Vampire Blood gives 1.2x damage/1.1x movement while locking removal for two Minecraft days of equipped time; Shrinking Veil refreshes Weakness I for a minute, and Missing Page has a five-block radius. The removed Lunar Crest's shared damage crash is also repaired. See [the balance policy, rebuild instructions and focused test results](docs/patches/nameless-balance/README.md).

Emotecraft is installed on clients and servers. The client-only SPEmotes Translations resource JAR loads automatically on 26.2, with local English additions covering Dances 5; select English in Minecraft for English labels. See [translation packaging and rebuild notes](docs/patches/spemotes-translations/README.md). Borrow Their Emote and its MossyLib dependency, Universal Enchantment Info, Armor Hider, and the local NeoForge Vectors alpha are client-only extras in `mod store`. The recommended profile, refreshed from the owner's current controls, uses G for the emote wheel, I for Punchy inspect, and K for Armor Hider. The race menu, Alex's Caves ability, and Spell Engine hotbar bypass are unbound. See the handbook for all controls. Client installs and repairs also apply Vectors' Maximum Performance preset to `config/vectors.json`, backing up an existing file and preserving unrelated interface settings. This runs independently of the keybind choice; server installs and `--check` leave it untouched.

Punchy and the revised Modded Swords x Punchy! resource JAR are client-only extras in `mod store`. The local Punchy 2.8f repair keeps RPG weapons attached to its animated hands and yields to the original Spell Engine skill motions while they play, then resumes Punchy. The resource supplies mappings for the 139 RPG Series weapon definitions. Client installs and repairs set `bettercombatCompat` to `true` in `config/punchy/punchy_config.json`, backing up an existing file under `modpack-backups` and preserving other settings. This also runs when the JARs are already current, independently of the keybind choice. Server installs and `--check` do not change Punchy settings. Remove any old RPG namespace blacklist entries to use these mappings. See the [compatibility implementation and test notes](docs/superpowers/plans/2026-10-16-punchy-modded-swords-26-2.md).

For either mode, the installer compares Git hashes and downloads only missing or changed JARs. Extra and replaced JARs are moved to a timestamped `modpack-backups` folder beside the target `mods` folder. JAR downloads are verified before the mod folder is changed, and a failed mod replacement is rolled back. Emote replacement has its own verification and rollback; an error in a later step does not undo a completed mod update.

After a successful install or repair, the installer opens the [player handbook PDF](https://github.com/Valhaimerd/mods-server/blob/main/modpack-player-handbook.pdf) in the default browser. It also opens the guide when the installation is already current; `--check`, cancellation, and failed updates do not open it.

The executable is not code-signed, so Windows SmartScreen may show an unknown-publisher warning. The source code is available in the `installer` folder.

Useful command-line options:

```text
ModsServerInstaller.exe --check
ModsServerInstaller.exe --target "D:\Launcher\Instance\mods"
ModsServerInstaller.exe --yes
```

`--check` reports what would change without changing files and does not create a missing target folder. `--target` installs the client mod set to another folder named `mods`. `--yes` is retained for compatibility and has no effect; no typed confirmation is used.

The keybind choice appears only in the interactive TLauncher flow. Command-line installs and server installs leave `options.txt` untouched. The embedded profile is curated from the pack's controls; the copied personal `options.txt` is excluded from Git.

## Build the installer

With the .NET 10 SDK installed:

```powershell
dotnet publish .\installer\ModsServerInstaller.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:EnableCompressionInSingleFile=true -o .\dist
```

The resulting self-contained `dist\ModsServerInstaller.exe` runs on 64-bit Windows without requiring a separate .NET installation.

For GitHub releases, create a draft release and push its `installer-v*` tag. The Windows installer workflow builds that tag, runs `--self-test`, uploads `ModsServerInstaller.exe`, and publishes the release as latest. It can also be run manually for an existing release tag.

## Skill progression

The pack's skill overhaul adds Magistrate casting, Aegis/Colossus/Bulwark defenses and Companion pet paths. Offensive, Defensive and Companion choices lock at their entrances; Utilities is unrestricted. Adventurer and both RPG skill categories gain XP at 1.35 times their original rate. RPG Priest healing nodes grant +3%; Paladin retains its defaults. Existing Adventurer allocations are refunded once on login, with earned skill XP preserved. All three same-filename replacement JARs must be installed together on client and server. See the [implementation, rebuild and focused test results](docs/patches/skill-overhaul/README.md) and the [player guide](docs/MODLIST.md#adventurer-skills).

## Maintaining the handbook

When the pack changes, follow the [player handbook maintenance checklist](docs/HANDBOOK_MAINTENANCE.md) to audit player-facing mods, controls, screenshots, indexes, and distribution counts without adding libraries or APIs to the gameplay catalog.
