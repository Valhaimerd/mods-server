# Mod Fixing Workspace

Read this file first, then read the note in any task-specific folder. For optional Farmer's Delight add-ons, read [`delight extras (optional)/README.md`](delight%20extras%20(optional)/README.md) before working there. The user's latest specific request defines which mod to fix and the expected behavior; use these notes as workflow context.

## Folders

| Folder | Purpose |
| --- | --- |
| `1 pending packs` | Input JARs awaiting work. Work only on the mod or issue named by the user. |
| `1 fixed packs` | Final production JARs for completed fixes. Keep test builds and intermediate compilations out. |
| `retired packs` | Superseded or removed JARs kept locally for recovery; not part of the active pack. |
| `delight extras (optional)` | Non-urgent add-ons for Farmer's Delight that may need a port to Minecraft 26.2. See its README. |
| `source`, `patches`, and mod-specific port folders | Local source and working files used to make fixes; these are not final mod-pack outputs. |

Treat JARs already in `1 fixed packs` as completed. Reopen one only when the user asks for another change or reports a follow-up issue.

## Punchy and Better Combat first-person rendering

The current client pack contains a same-name local repair of `punchy-2.8f-neoforge-26.2.jar` and `modded-swords-x-punchy-compat-26.2-1.3.2.jar` in `mod store/`. Punchy 2.8f (`3Kktuo5t`) remains the newest upstream NeoForge 26.2 version; the compatibility archive was the outdated part. Both final files are also in `1 fixed packs`. The exact upstream JAR is retained in `retired packs/punchy-2.8f-neoforge-26.2-upstream-3Kktuo5t.jar` and in the repair source's hash-pinned `upstream/` folder.

The missing held weapons came from a concrete renderer conflict: PAL's `PlayerModel.translateToHand` wrapper skipped the `ModelPart.translateAndRotate` call where Punchy installs its explicit hand matrix. The repair consumes that same matrix directly only in Punchy's private item-render path. An exact crossbow A/B changed from invisible to visible; the claymore attack and Arsenal bow idle/draw were also observed with the item visible. The resource now maps all 139 current RPG weapon-attribute definitions, removes overlapping group aliases, repairs the invalid claymore critical clip, and supplies explicit melee combos and ranged mappings.

Punchy temporarily yields both hands when Spell Engine's original casting, release, or misc animation layers are active, including fade-out, and resumes afterward. The user explicitly chose original skill motions. Native arm visibility is enabled, but original animation clips, timing, positions and third-person poses are retained. The experimental body offsets that produced distant, detached-looking arms were removed. A focused hammer-channel check and actual Paladin barrier cast exercised handoff and recovery in the isolated client. Native motions may naturally carry a weapon outside the first-person field of view; do not replace them with speculative offsets.

New configs default `bettercombatCompat` to `true` without installing namespace blacklists. Client installs and repairs now merge `bettercombatCompat: true` into existing configs, with a backup and other settings preserved. The merge is independent of recommended keybinds and also runs when JARs are already current. Server installs and `--check` do not change Punchy config. Existing users should remove old RPG blacklist rules to use the mappings. The user's profile and separate desktop server were not changed in this pass. Local source, rebuild instructions and static verification are in `source/punchy-rpg-26.2/`. The source archive and all trial/probe artifacts remain local; do not publish the adapted resource without checking its redistribution terms.

Testing used Minecraft 26.2, NeoForge 26.2.0.90, Java 25 bytecode, Punchy 2.8f, Better Combat 3.2.2, Spell Engine 1.10.5, Player Animation Library 1.2.6, Arsenal 1.5.1 and the four RPG class modules 3.1.2. Better End/Nether and their libraries were added only to register material-dependent variants. There are 124 idle weapon captures; the remaining individual weapon passes were stopped at the user's request. Static validation passes for 139/139 definitions. This does not claim that every weapon combo, skill, shader or multiplayer presentation was individually tested. See the current-pass section in `docs/superpowers/plans/2026-10-16-punchy-modded-swords-26-2.md` for evidence and limits.

## Nameless Trinkets balance and dedicated-server crash

The same-name `mods/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar` now applies the owner's balance policy, retains 42 available trinkets and removes 12 relics from acquisition, crafting, loot injection, the creative tab and JEI. Removed IDs remain inert for save compatibility and can be unequipped. Specified settings override stale config assignments without rewriting personal files. Titan's Mark retains size, removes attack damage and refreshes Slowness I (60s), Health Boost V (+20 maximum HP / +10 hearts, 5s) and Resistance I (5s). The removed Lunar Crest's damage listener and shared client-class reference are also repaired; simply hiding a relic would not fix the dedicated-server crash path. A minimal server reproduced the original crash, then the repaired build passed 108 checks; a separate client confirmed removed items are absent from JEI. Original and final archives are in the ignored retired/fixed folders. Details, policy and rebuild sources: [Nameless balance repair](../docs/patches/nameless-balance/README.md). The working owner reference is `Nameless Trinkets - effects.md` in this folder. Personal `.minecraft`, worlds and the separate desktop server were not changed.

## Emotecraft wheel and list previews

Alex's Caves Continued 1.0.11 caused the blank Emotecraft previews: its fluid helper dereferenced the preview mannequin's dummy level's null chunk source. The repair carried forward to `mods/alexscaves-1.1.2-neoforge+26.2.jar` on 2026-10-20 returns zero/false for levels without chunks in the height and eye checks, retaining the original real-world paths. On the original repaired 1.0.11, all eight wheel models and the left list preview were observed working in an isolated NeoForge 26.2.0.90 client with Vectors at Maximum Performance, with no preview exceptions. The 1.1.2 upstream still lacked the guard; the new repack passes ten focused JVM fixture checks, with only ACFluids changed. The obsolete 1.0.11 and upstream 1.1.2 are retained in ignored retired packs; the new final JAR is also in fixed packs. No new client visual test was performed. See [the source patch, rebuild instructions and verification limits](../docs/patches/README.md).

## Gateway of Doom targeting

The active `gatewayofdoom-neoforge-26.2-2.3.4.jar` keeps upstream 2.3.4's pressure-sharing wave AI, attacker reactions, periodic target review, four bosses, and custom mobs. Its repack changes only target validity: players downed by PlayerRevive or Better Revive are ineligible while other upstream rules remain. The guard covers the shared wave selector, boss focus and entity validators, and custom Rogue, Kobold, Stone Golem, and Cinder Wraith validators. Patch source is in `source/gateway-of-doom-2.3.4-downed-target/`; upstream 2.3.4 and the previous fixed 2.2.1 JAR are in `retired packs/`. The archive and Java 25 class versions were inspected, but a multiplayer encounter has not yet been run for this version. The new JAR is installed only in this project, not the separate desktop server.

## Death and grave system

The active Better Revive JAR is now `Better Revive - 2.2.0 - NeoForge - 26.2-MultiGraveFix-Java25.jar`. It includes the previous grave guard: the grave already had unbreakable hardness, explosion resistance, a no-op explosion callback, an explosion affected-list filter, and canceled player breaking; the guard restores the original block entity and contents on the next tick if another world edit removes an active grave. Normal retrieval and expiry still remove it. The current death-fix repack captures a grave and lets PlayerRevive complete a real death on a second lethal hit or give-up; it restores retained hotbar and armor on respawn and clears remaining downed state. The Grave Compass takes the final hotbar slot, moving its former item into available main inventory. This fixes the earlier build's missing hotbar and 1-health/6-food give-up state, reproduced and corrected in an isolated flat world. A full-hotbar grave-retrieval regression confirmed the displaced item and retained gear remain intact. Lava deaths can create a grave on nearby land or above a lava sea; void deaths remain final without a grave. This repack was installed only in this project, not the separate desktop server or personal Minecraft profile. Patch source and runtime evidence are in `source/better-revive-death-fixes/`; previous JARs are in `retired packs/`.

Better Respawn 2.0.6 was also loaded in the isolated flat-world client and server on 2026-10-16. It placed the player roughly 283–315 blocks from the death grave. Better Revive restored retained gear at that distant spawn, set the compass target to the original grave, and kept the grave inventory available after its chunk unloaded and reloaded. Returning and restoring the grave yielded the stored items without clearing the hotbar, armor, or offhand. The test used a temporary probe to invoke PlayerRevive's give-up and grave-retrieval methods, so the visible compass direction and multiplayer interaction remain unverified.

The 2026-10-16 multiple-grave revision chooses the newest unexpired grave for the single Grave Compass. The subsequent full-pack single-player run exposed a separate creation bug: when an older grave existed, upstream code marked the new grave temporary, then normal respawn converted it into a collection crate. With this pack's zero-second collection phase, that new grave expired immediately and lost its registration and contents; the older grave was not deleted. The active repack now creates ordinary independent graves on both death paths. The earlier isolated checks covered compass order, same-location placement, and a full hotbar, but did not expose this full-pack normal-respawn path. The JAR default remains 300 seconds; an existing world's saved timer overrides that default. The user's single-player world contained a saved 180-second timer when inspected read-only; no personal world was changed. Details are in `source/better-revive-death-fixes/README.md`.

Observed before the grave guard: a grave vanished after nearby destruction, but its Grave Compass still pointed at that location more than five minutes later. The compass followed the registry record even after the block was gone. The new repack checks online players' compasses every second and removes one when its grave is unregistered or its stored protected deadline has passed with no collection phase. A grave created under an older timer keeps that stored deadline, so the five-minute rule is not retroactive. The guard cannot restore a grave that disappeared before the guard was installed. The multiplayer death and lava paths still need an in-game check after installation.

The new Better Revive JAR, Better Revive X PlayerRevive 1.0.1, and `Better Revive X Curios - 1.0.0 - NeoForge - 26.2-KeepGearFix.jar` are installed in the active Minecraft 26.2 pack. The separate desktop server still has the prior Better Revive build. PlayerRevive 2.1.2 and Curios 16.0.0 remain installed. The PlayerRevive integration gives the first rescue attempt to PlayerRevive, then hands a bleed-out or surrender to Better Revive for its grave and longer revival window. The repacked Curios add-on now captures ordinary equipped Curios, including Sophisticated Backpacks, for the grave. It keeps its built-in Grave Compass exception.

The Better Revive repack saves XP from the player's current level and progress in both normal and explosion deaths. The original saved `totalExperience`, which includes XP earned before spending levels and could inflate a level-20 recovery to roughly level 90. Built-in defaults are now `graveDurationSeconds = 300` and `collectionDurationSeconds = 0`: the grave stays owner-protected for 5 minutes, then expires without a public collection phase. The admin menu accepts zero for that setting. The server's `config/better_revive.json` and existing `world_config.dat` were updated; their previous copies and the original JAR are in `modpack-backups/better-revive-xp-timers-2026-10-15/`. The production repack is in `1 fixed packs`; local patch source is in `source/better-revive-xp-timers/`. Existing graves keep the XP amount and deadline already stored in them. The new grave-guard repack keeps the XP and 5-minute timer changes.

The death rule retains hotbar slots 0-8, armor slots 36-39, and offhand slot 40 through revival or normal respawn. Ordinary equipped Curios go to the grave. Tool Belt's separate Shift + Q slot stays equipped by request. Sophisticated Backpacks always go to the grave, including backpacks in those retained slots. The rule applies to immediate and delayed explosion graves. Retained gear is saved to player data so a restart while downed does not discard it. The 2026-10-15 repack fixes normal respawn, which bypasses Minecraft's respawn event, and restores previously stranded retained gear when a living player next logs in. Existing players who already respawned with missing retained gear should reconnect after installing the new JAR; items can be recovered only if their `NeoForgeData.better_revive_keep_gear` record still exists. The prior server JAR is backed up in `modpack-backups/better-revive-normal-respawn-2026-10-15/`; implementation source is in `source/better-revive-keep-gear/`.

The previous, broader retention rule was exercised in an isolated NeoForge 26.2.0.90 client and dedicated flat-world server. The five core mods loaded; a scripted death returned the sword and chestplate on normal respawn while placing Sophisticated Backpacks and ordinary inventory in the grave. A second test used Artifacts only as a Curios accessory fixture and confirmed that an equipped Flame Pendant was retained under the old rule. A saved retained-gear record was also recovered on login. A separate test invoked the grave's `restoreInventory` and `restoreXp` methods and returned `complete=true`. Those runtime checks predate the current Curios-to-grave change.

The Java 25 repack recompiles only the three `KeepGear` helper classes with `javac --release 25`. The previous repack accidentally used Java 26 class version 70, while Minecraft 26.2 and the rest of Better Revive use Java 25 class version 69. The updated archive contains 187 version-69 classes, and only those three helper entries differ from the previous repack. The previous server copy is backed up in `modpack-backups/better-revive-java25-2026-10-15/`.

The 5-minute timer revision changes the protected grave default from 1,200 to 300 seconds and keeps the public collection phase at zero. The existing server config and saved world config are set to 300 seconds; existing graves retain their stored deadlines. The prior server JAR and settings are backed up in `modpack-backups/better-revive-five-minute-2026-10-15/`.

The hotbar, armor, and offhand revision keeps Java 25 class version 69 and changes only the same three `KeepGear` classes. The Curios bridge's built-in Grave Compass exception is restored; the `captureCurios` compatibility hook no longer removes other Curios after the bridge commits them to the grave. Saved Curios from a death under the earlier rule can still be restored from the player's existing `better_revive_keep_gear` data. The prior server JAR for this revision is in `modpack-backups/better-revive-hotbar-armor-2026-10-15/`.

Tool Belt's `Shift + Q` belt slot is a separate NeoForge attachment, outside both vanilla inventory and Curios. Its original death handler dropped the equipped belt and cleared that attachment. `ToolBelt-26.2-2.10.0-CuriosBetterReviveFix.jar` retains the dedicated slot on player deaths when Better Revive is loaded, allowing Tool Belt's clone handler to copy it on respawn. Its ordinary death drop behavior remains for packs without Better Revive, and Curse of Vanishing still removes a cursed belt. The existing Curios compatibility fix is retained. In the isolated six-mod flat-world client/server, a scripted final death emptied the slot with the old JAR; the repack kept `toolbelt:belt` in the attachment after final death and immediate respawn. The test-only belt equip hook was removed from the production JAR. The prior server JAR is backed up in `modpack-backups/toolbelt-better-revive-slot-2026-10-15/`, and the local source is in `source/toolbelt/`.

Graveless 1.3.2 was removed from the pack and server after the client crashed while rendering its 3D grave preview. Its JAR is kept in `retired packs`. Existing Graveless death records may not be accessible through Better Revive; recover any items from an old save before relying on this replacement.

## End and Nether world generation

Better End 26.2.6 replaces Stellarity 5.5.5 in the pack and server. `lootintegrations_stellarity-1.2.jar` was removed with Stellarity; no other active JAR referenced it when inspected. Better Nether 26.2.3 replaces 26.2.2, leaving one active version. The removed pack JARs are in `retired packs`; the server copies are in `modpack-backups/better-end-switch-2026-10-15/`. Existing End chunks retain their generated terrain, so explore new chunks for Better End's generation.

## Penchant progression

`penchant-neoforge-26.2.0-1.6-2.5x-progress.jar` replaces the previous 26.2 fixed repack in the pack and server. It changes only `EnchantmentProgress.getMaxProgress`: the target for each eligible enchantment is `ceil(original target × 0.4)`. This keeps the normal use triggers, level caps, and existing saved progress while making leveling approximately 2.5 times faster. The prior JAR is in `retired packs` and the server backup is in `modpack-backups/penchant-2.5x-2026-10-15/`. Repack source is in `source/penchant-fast-progression/`.

## Display Delight food interactions

`mods/displaydelight-26.2-1.7.0-fix2.jar` now includes the food-interaction repair, retaining the active filename and version. The previous JAR is preserved in `retired packs`; the production replacement is also in `1 fixed packs`. The port had no empty-hand pickup path for bowl/bottle displays, compared placement against `CONSUME` even though Minecraft 26.2 returns `SUCCESS`, and canceled successful interactions with `PASS`. The repair retrieves the associated edible meal from ordinary food displays and reports completed interactions as successful. Displays without an installed source food remain decorative.

Isolated runtime regression checks used Minecraft 26.2, NeoForge 26.2.0.67, and Farmer's Delight 1.3.3-fix5. Bowl retrieval and handled-result checks failed before the change; plate take-one/take-all already passed. After repair, checks cover bowl retrieval with and without Sneak, eating the returned stew and receiving its bowl, single-meal placement only while sneaking, take-one/take-all, small plates, empty-plate preservation, and successful action results. This is a scripted interaction test, not a full-pack mouse-input or multiplayer verification. No left-click placement handler exists in this mod; the user's profile was inspected read-only and has Attack on left-click, Use on right-click, and Sneak on C. Test sources and rebuild notes are under `displaydelight-26.2-port/`; personal installations and worlds were not modified.

## 17 October 2026 pack additions and ALI update

Advanced Loot Info 2.4.0 and ALICompat 1.3.0 replace their 2.3.0 and 1.1.0 copies. ALICompat requires Advanced Core Info 1.5.0, so that official NeoForge 26.2 dependency replaces 1.3.0 too. The three superseded JARs are preserved in `retired packs/pack-update-2026-10-17/`. Advanced Worldgen Info 1.2.0 is restored to `mod store`, consistent with the earlier server-stall isolation; ALI, ALICompat, and ACI remain in `mods` for server data support. The prior audit's server-stall finding is not a successful test of the new versions.

Emotecraft 3.4.0-b.build.170 is in `mods`; its required Player Animation Library 1.2.6 is already present. Borrow Their Emote 1.0.1, its missing MossyLib 1.5.0 dependency, Universal Enchantment Info 1.5.0, and the supplied Vectors 1.5.0-alpha.4 NeoForge port are client-only entries in `mod store`. ACI and MossyLib downloads match official Modrinth SHA-512 hashes. Vectors' public page lists Fabric releases; the supplied JAR's metadata identifies this as a local NeoForge alpha, not an official listed NeoForge release. Vectors is omitted from the gameplay catalog as a performance internal, while its visible control is listed. The existing keybind profile has no assignments for the new controls, so the handbook leaves their recommendations blank. No installer source or personal installation is changed by this update. Armor Hider 1.0.1 was added late in the pass and placed in `mod store` after its official page and metadata confirmed client-only armor/elytra visibility controls. Sky Village-Islands 01.02.00 and TradeView 1.0.5 were also added to `mods`; both official file lists include NeoForge 26.2, and the required Lithostitched 1.8.0+beta6 is already installed. The handbook describes TradeView's installed preview interface rather than assuming newer upstream controls. Archive/dependency inspection is not a runtime emote, rendering, world-generation, or server compatibility test.

## AWI 1.3.2 and World Map follow-up

AWI 1.3.2 replaces 1.2.0 and is installed in `mods` alongside ACI 1.5.0, ALI 2.4.0, and ALICompat 1.3.0 at the user's explicit request. The user accepts lengthy collection and reports ALI completing after about eight minutes. This supersedes the earlier client-extra isolation decision. The extra ACI 1.5.0 `(1)` download was byte-identical to the installed JAR, so only one copy remains. Xaero's World Map 1.47.0 replaces 1.46.0. Superseded/duplicate files are preserved in `retired packs/awi-update-2026-10-17/`.

A bounded isolated test loaded the 310 main-pack JARs on Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 with an 8 GiB heap and a fresh flat world. It reached `Done` about 84.5 seconds after launch. AWI's base-layout scan took 16.6 seconds, but queued `list` commands did not answer during the subsequent four-minute observation. A thread sample showed `collectCompositeFeatureConfigurationBlocks`; this establishes lengthy blocking collection, not that it can never finish. The test's four-minute budget was shorter than the user's reported eight-minute ALI completion, so scan completion remains unverified. The no-AWI comparison was not launched after the user's instruction to retain the full information suite. Evidence is in `tmp/awi-1.3.2-test/`. Its longer watchdog allowance belongs only to the isolated test; personal/server configurations were not changed. Server owners may need to allow longer collection before the watchdog intervenes.

## Fix workflow

1. Read the current user request and identify its exact target and requested behavior. Inspect the pending JAR's embedded mod metadata, Minecraft/NeoForge compatibility, and relevant source before changing it. Do not update unrelated pending mods just because they are present.
2. Make and package the requested fix. A test compilation, development build, or intermediate JAR is not a finished fix.
3. Once the final production JAR is built and its archive and metadata have been checked, put that JAR in `1 fixed packs`.
4. For every completed fix, replace the active JAR in `mods/` with the final compiled JAR. Keep the existing filename when repacking the same mod/version; the installer detects changed contents by Git hash, so a new name is unnecessary. Use a new filename for an actual mod/version change or when the user explicitly asks. Do this only with the final production artifact, never with a test or intermediate compilation. Check that `mods/` contains the intended replacement and not a duplicate old entry.
5. Remove the completed input from `1 pending packs` so that folder continues to represent unresolved work.

Keep a fix in the existing mod entry when that is what the user requested. Do not create an extra compatibility mod or pack unless the user asks for one. If the final build or compatibility check cannot be completed, leave the mod pending and report what remains unresolved.

## Skill tree and Orb of Oblivion compatibility

Adventurer Skills is the pack's only general stat tree. RPG Series class and weapon trees remain available. Default Skill Trees and the older Stronger Skill Tree are retired; the removed Default JAR is in `retired packs` for recovery. The server's previous copies are in `modpack-backups/adventurer-skills-only-2026-10-15/`. `orb_of_oblivion_puffish_compat-1.0.2.jar` replaces 1.0.1 in `1 fixed packs`, `mods/`, and the server's `mods/` folder.

The add-on registers `adventurer_skills:adventurer_skills` with the RPG Series Orb's reset categories. The Orb now refunds spent Adventurer points even when no RPG class or weapon points were spent, and still resets the RPG categories under its original use and durability rules. Earned category experience is preserved. The local source and build notes are in `source/orb-oblivion-puffish-compat/`.

## Withered Lands Slime Rain removal

`Withered-Lands-26.2-1.6.9-no-slime-rain.jar` replaces the original in the pack, client, and server `mods/` folders. Its only changed archive entry is `SlimeRainEvent.class`: natural and command starts are blocked, and a previously saved active event ends on its next tick. Other Withered Lands content remains in the JAR. The original is in `retired packs`; the finished repack is in `1 fixed packs`. The installed client and server `withered_lands.toml` files also set `"Slime Rain event enable" = false`. Rebuild instructions are in `source/withered-lands-no-slime-rain/README.md`.

## Building replacement - 2026-10-20

Effortless Building 4.3 is retired from the active pack, replaced by the supplied Sophisticated Building 5.1.0 (`sophisticatedbuilding`). Its metadata targets Minecraft 26.2 / NeoForge 26.2.0.88+, and the installed Backpacks 3.26.2 / Core 1.5.0 meet its optional integration requirements. No other installed mod declares an Effortless Building dependency. The old JAR is retained in retired packs. The installer source profile now binds the new radial-menu key to Alt, and leaves the other six actions unbound. Existing old-mod items/settings are not claimed to migrate automatically across the changed namespace.

## Voltaic Shot removal - 2026-10-20

The existing `DungeonsArise-26.1.2-2.1.70-release-26.2-registryfix.jar` keeps its prior registry repair and all classes/worldgen. Voltaic Shot is removed: its enchantment definition, tag, two functions, translation keys and five special loot outcomes. Those outcomes become equally weighted empty rolls, preserving all other loot chances. Existing items can no longer resolve this enchantment. Rebuild with `python docs/patches/remove-voltaic-shot.py <prior-registry-fixed.jar> <output.jar>`; run with only a JAR argument to verify complete absence. Prior active bytes are retained in ignored retired packs.

79 isolated NeoForge 26.2.0.90 server checks passed with the repacked Dungeons Arise, including registry absence and the restored 25% ordinary/Hunter bow and crossbow projectile bonuses. The original ensnaring entity tag still references removed `minecraft:potion`, producing a tag and dependent-predicate reload warning; these unrelated resources are byte-identical. See `docs/patches/voltaic-removal-verification.json`.

## Warrior tier I and directional shields ? 23 October 2026

The same-filename Rogues, Skill Tree and Spell Engine repacks now include Throw Weapon, pulling/area nets, hit-based Killing Spree, direct-damage Endured Vitality and explicit haste across all Warrior tiers. Shields apply an independent 80% frontal reduction, retaining activation delay and durability while preventing axe/component disable and damage-tag/piercing bypass. The isolated NeoForge 26.2.0.90 server passed 625 scripted checks, including party/pet protection and existing Rogue tier-II regressions. Active and `1 fixed packs` copies match; prior copies are retained in `retired packs/warrior-tier-one-2026-10-23/`. See `docs/patches/warrior-tier-one/README.md` for inputs, rebuild, exact values and limitations. No personal installations or worlds were touched; nothing was committed or pushed.

## Asgardian Bifrost travel alignment

The earlier travel-only same-filename Bifrost 0.4.3-alpha replacement is displayed as Asgardian_Mod and renames Heimdall's Sword to Hofund. That historical layer preserved marker saves, IDs, travel effects and timing; the full expansion below supersedes its mechanics and test scope. Stored Iourus Asgardian identity is required for focus use/attacks, marker placement/use/rename, menus, commands and packets; dead, spectator and downed actors cannot activate travel. Losing eligibility cancels charging. Both sides require matching Bifrost and Asgardian-enabled Iourus builds. Combat additions and passenger transit were pending at that stage; they are implemented by the full expansion below. See [source and isolated client/server evidence](../docs/patches/bifrost-alignment/README.md). The previous JAR is in retired packs and the final is also in fixed packs; personal installations were not changed.

## Warrior tier II and scaling ? 23 October 2026

The current three Warrior repacks include six-second nets (four with Multiple Nets), half-point melee-power scaling for Vitality armor and Warm Up healing, Wild Charge and Shout purification/control protection, revised second-tier upgrades and the guaranteed Momentum roll/next-hit buff. Wild Charge retains a two-second base and Improved adds one second per the final clarification. The isolated server passed 762 measurements, including previous shield/throw/party regressions. Source, exact behavior, client-check scope and inputs are in `docs/patches/warrior-tier-two/README.md`; previous active/fixed copies are in `retired packs/warrior-tier-two-2026-10-23/`. Personal installations and worlds remain untouched; no commit or push was requested.

## Full Asgardian expansion - 24 October 2026

The [marker occlusion repair](../docs/patches/asgardian-expansion/marker-occlusion-fix.md) removes the solid-cube face-culling assumption from Bifrost's inset decorative marker. Adjacent terrain now renders through its model gaps instead of exposing the world below. Collision, marker saves, IDs and travel are retained, including the preceding beam repair. The same-filename replacement is local and not pushed; no personal installation is changed.

The subsequent [Bifrost visibility repair](../docs/patches/asgardian-expansion/visibility-fix.md) restores a visible ground-level effect for the caster and arriving passengers. The previous tall display column rose above the first-person view, and most particle origins were high above the player. A bounded inward-facing rim and nearby particle origins now accompany the original sweep. This does not change transport/capture timing or controls; the handbook remains accurate. The same active filename is retained; the predecessor is archived locally. No personal installation is modified and this follow-up is not pushed.

The full 2026-10-24 Asgardian/Bifrost expansion adds server-authoritative weapon skills, bound hammer/axe ownership and protected final-death recovery, mixed-race passenger transit, original models and four phase sounds. The same active filename is retained. See [the implementation and isolated test scope](../docs/patches/asgardian-expansion/README.md). This extends the earlier travel-only alignment; neither isolated runtime checks nor the initial coefficients establish final class balance.

## Warrior ultimates - 24 October 2026

Installed same-filename Rogues, Skill Tree and Spell Engine overlay: Berserker, revised Last Stand, five-hit Momentum, health-based Enrage and Tenacity. [Build and test record](../docs/patches/warrior-tier-three/README.md). Isolated NeoForge checks: 944 passed. Matching fixed copies retained; prior active/fixed archives are in ignored `retired packs/warrior-tier-three-2026-10-24/`. No personal worlds changed; no GitHub push requested.

## Archer and Wizard descriptions - 24 October 2026

Audited 135 Archer/Arcane/Fire/Frost descriptions and simplified 55. Same-filename language-only Archers, Wizards and Skill Tree overlays preserve all gameplay resources and bytecode. Percentage descriptions, ranges and timings replace template tokens; Nimble Shield wording now matches its half-penalty offset. [Audit and rebuild record](../docs/patches/class-descriptions/README.md). Previous archives are retained in ignored retired packs; fixed copies match.

Confirmed Warrior follow-up: Tenacity now triggers at 1-30% HP with twelve-second absorption. Enrage size stacking and the five ultimate cooldown steps are confirmed. [Rebuild and isolated proof](../docs/patches/tenacity-followup/README.md).

## Skill names and tooltip presentation - 24 October 2026

Synchronized nineteen tree-title translations with scroll/spell names across both tree categories. Supercharge text correctly says 125%; native multiplier remains 1.25. Shortened class descriptions and moved exact passive activation labels into the yellow statistics section, retaining native dynamic cooldowns. Five matching same-filename Archers/Wizards/Rogues/Skill Tree/Spell Engine replacements are installed. [Build, cause and checks](../docs/patches/skill-tooltip-polish/README.md): 43 native client tooltip checks and 946 dedicated Warrior/shield/party checks passed. Combat data is unchanged. Previous archives are in ignored retired packs.

## Bifrost claim, recipe discovery and shield poses - 24 October 2026

The [claim and recipe repair](../docs/patches/asgardian-expansion/claim-and-recipes.md) corrects close ground-weapon selection and adds native recipe-book unlocks for all four existing Bifrost recipes, verified in JEI. Prior beam and marker repairs remain included. The client-only [Punchy shield repair](../docs/patches/shield-pose/README.md) recognises Paladins component shields and connects the existing first- and third-person blocking poses. Same-filename active/fixed copies are installed after isolated checks; predecessors are retained locally. No personal installation or GitHub push.

## Iourus race balance - 24 October 2026

Installed same-filename Iourus update: Elf no longer gains projectile damage, has +8% movement and connected-client slow falling; Goblin scale 0.75; Asgardian scale 1.10 with mining bonus removed; Dwarf +2 armor. Orc uses a -10% total attack-speed modifier and removable melee/ranged bonuses at 50/40/30/20% HP. Old Orc raw-base inflation receives a one-time backed-up repair. [Implementation, migration details and tests](../docs/patches/race-balance/README.md). Native Better Combat/RPG integration and persistence passed 166 measurements; the real Elf drop now slows correctly with no health loss. Fixed copy matches; prior active/fixed archives are ignored under retired packs. The owner reference is `Iourus Races - attributes and benefits.md`.

Orc follow-up: starting +2 melee/+1 ranged applies at full health, with higher tiers at 40/30/20% HP. Adds a 10% bow/crossbow draw-speed penalty through ranged haste. [Implementation and 170 native checks](../docs/patches/orc-followup/README.md). Same-filename Iourus/fixed copies updated; backups retained.

## RPG entity lifecycle cleanup - 24 October 2026

The same-filename Spell Engine repair removes invalid spell clouds that previously bypassed expiry and preserves summon/projectile elapsed age through native save/load. Projectile range and traveled distance also survive reloads. Original damage, targeting, cooldowns, party rules and configured lifetimes are unchanged. [Audit, limitations and rebuild](../docs/patches/rpg-performance/README.md): 17 lifecycle checks, 946 combat/shield/party regressions and 43 client loading/tooltip checks passed. Matching active/fixed copies are installed; predecessor retained locally. This does not establish the cause of all reported server delay. No personal installation or GitHub push.

## PvP retention and Improved Bear Trap - 24 October 2026

PvP now retains 30% of attributed player damage. Improved Bear Trap deals 60% dual-melee power; the base trap remains 40%, with its confirmed four-second haste-affected base cooldown. [Exact overlay and verification](../docs/patches/pvp-trap-followup/README.md): 29 PvP and 946 combat/shield/Rogue/party checks passed. Same-filename active/fixed replacements installed, previous archives retained locally. No personal installation or push.

## Spread Spawnpoints Spring mode - 24 October 2026

Owner clarified Spring rather than pool. Same-filename Spread Spawnpoints 1.2.0 repack sets Spring as the new-world default while preserving saved settings/assignments. [Isolated test and existing-world command](../docs/patches/spawn-audit/README.md): 65 server checks plus actual client first arrival passed with only the mod and bundled dependency. Separate desktop server/world untouched; existing worlds require the documented operator command to change their saved mode. Previous archives retained in ignored retired packs; fixed copy matches.

## Iourus race-state cleanup - 24 October 2026

Installed atomic race setter/attribute cleanup, direct admin state reconciliation and client logout reset. Melfork water/land attack modifiers clear immediately; its underwater vision/breathing/swim effects are retained. Elf night vision is removed; Goblin vision stays. Elf has solid leaf collision while other races pass through tagged leaves or LeavesBlock subclasses. [Build and evidence](../docs/patches/race-state/README.md): 931 server checks plus 22 connected identity/vision/water checks passed. Matching fixed copy installed; backups retained. Personal worlds and external server untouched.

## Rogue tier I/II follow-up - 24 October 2026

[Current balance and isolated proof](../docs/patches/rogue-rebalance/README.md): Shock 14s/4-block radius, Explosive 50%/4-block splash, Slice 16s (14 Improved), Blades +4 hits, four-second Shadowstep kill-reset window, 0.5s Cloak, 40% Ambush, 2.5s Serrated Bleed and no Opportunist critical-damage bonus. Fleet Footed remains 3% per stack as confirmed. 1261 dedicated combat/party/Rogue and 43 client checks passed. Three same-filename active/fixed replacements installed; predecessors archived locally. No personal installation or push.

## Mutilate and Prepared Counter - 24 October 2026

Mutilate now deals 100% combined dual-melee power. Prepared Counter no longer retaliates: evasion readies one basic melee hit for +25% melee power, with a two-second haste-affected cooldown. Native attack verification confirms 20 to 25 damage, followed by 20 on the next hit; spells and protected pets cannot consume the charge. [Build and proof](../docs/patches/rogue-counter/README.md): 1311 combat/party/Rogue checks and 43 client checks passed. Three active/fixed JARs and the reference/handbook/PDF are updated; predecessors archived locally. No personal installation or push.

## Barricades and hidden enchantment retirement (2026-10-24)

Barricades, Life Steal, Air Jump, Protection Grace and Tense are removed. Four shared providers and Penchant are repacked to remove 15 modded enchantment registry entries and acquisition references. Vanilla Sweeping Edge stays hidden. Saved equipped/stored enchantments and Penchant progress strip retired IDs while preserving valid enchantments. The isolated server passed 483 checks; originals are in `retired packs/enchantment-retirement-20261024`. See [the tracked retirement report](../docs/patches/enchantment-retirement/README.md). No external worlds were changed.

## Warrior follow-up (2026-10-24)

See [the tracked patch](../docs/patches/warrior-followup/README.md): minimum charge damage, Last Stand +20% maximum health/healing per pulse, ultimate death cleanup, icon reuse and removal of Lethality spike clouds. Three same-name active/fixed archives replaced after 895 isolated server checks. Previous archives remain in `retired packs/warrior-followup-20261024`. Both client and server need the update. No push requested.

## Throw Weapon recast fix (2026-10-24)

[Tracked patch](../docs/patches/throw-weapon-hit/README.md): quick recasts capture power without the skill's own Weakness penalty, while ordinary melee stays weakened. Strong Throw pierces once for two hits total. Installed matching Skill Tree/Spell Engine archives after 909 isolated checks; originals retained under `retired packs/throw-weapon-hit-20261024`.

## Flat Bleed - 24 October 2026

Bleed is now one level with no power/amplifier scaling: 2 raw HP stationary or 6 raw HP moving horizontally, every 1.25 seconds. Serrated remains 2.5s and Bloody Strikes 7.5s. [Build and scoped proof](../docs/patches/flat-bleed/README.md): 49 native Bleed and 43 client checks passed, including old high-level NBT normalization. Native defenses/ownerless magic attribution are unchanged. The overlay was rebased on newer Warrior Throw repairs before installation. Two active/fixed same-filename replacements installed; predecessors retained locally. No personal installation or push.

## Asgardian tooltip wording - 24 October 2026

Replaced the technical Asgardian English tooltip with short, plain prose matching the other race descriptions. [Exact wording and archive proof](../docs/patches/race-description/README.md). Only two language keys change; race mechanics and all other archive entries are unchanged. Matching active/fixed JARs installed; predecessor retained locally. No personal installation or push.

## Plain-English race benefits - 24 October 2026

All eight English Iourus trait lists and descriptions now use short, plain phrases without percentages or formulas. Separate numeric Height/Health/Speed fields and all mechanics remain unchanged. [Exact wording and byte-level validation](../docs/patches/race-plain-language/README.md). Active/fixed same-filename copies match; predecessor archived locally. No personal installation or push.

## RPG Skills wording proposal - 24 October 2026

Created [196 individual before/after descriptions](../docs/patches/skill-wording-review/RPG%20Skills%20-%20description%20rewrite.md) for Arcane, Fire, Frost, Archer, Rogue and Warrior, with a matching local `RPG Skills - description rewrite.md`. Priest/Paladin are excluded. All proposals are no longer than current text; 20 already-brief entries are retained. This is a reviewed wording draft, not an installed overlay. No JAR changes or push in this pass.

## Approved RPG skill wording implementation - 24 October 2026

Installed all 196 approved English descriptions across Arcane, Fire, Frost, Archer, Rogue and Warrior: 176 rewrites, 20 retained. Four same-filename Archers/Rogues/Wizards/Skill Tree overlays change only English language resources; all other keys and archive entries are preserved. Active and fixed copies match, with predecessors archived locally. Priest and Paladin are untouched. [Build, exact archive checks and scope](../docs/patches/skill-wording-review/README.md). No new client visual test, personal installation changes, commit or push. The earlier proposal is now implemented.

## Vanish held-item rendering - 24 October 2026

Rogue Vanish now hides both held items in first-person (including Punchy) and third-person render snapshots. Only client rendering hooks were added to the same-filename Rogues JAR; equipment, combat, armor and ordinary Invisibility are unchanged. The isolated client passed eleven checks, including restoration when Stealth ends. [Root cause, rebuild and runtime scope](../docs/patches/vanish-held-items/README.md). Active/fixed copies match, predecessors are archived locally; personal installations and external servers were untouched. No push requested.

## Bifrost RPG weapon alignment - 24 October 2026

Stormbreaker now matches the Netherite Great Hammer (0.8 attack speed, two-handed, native Ground Slam); Mjolnir matches Netherite Mace with Smash; Hofund matches Diamond Sword with Swift Strikes. Stormbreaker has a longer handle and corrected held yaw, with Punchy-compatible framing. Twelve isolated connected-client checks passed; active/fixed archives match. Existing custom abilities and inputs are unchanged in this pass. [Build, evidence and planned Bifrost Skills](../docs/patches/bifrost-weapon-alignment/README.md). Handbook weapon guidance/PDF are synchronized, predecessors archived locally. No personal installation changes or push.

## Global player damage and restored PvP retention (2026-10-24)

Player-attributed outgoing damage now retains 80% in PvE; PvP applies a further 50% multiplier (40% combined). The prior 30% PvP policy is superseded. Healing, mob/pet attacks and ownerless damage are unchanged; party/team protections remain. Skill coefficients and power attributes are unchanged to avoid duplicate scaling. The same-name PvP JAR passed 32 isolated checks. [Source and report](../docs/patches/pvp-flagging-26.2/README.md). Originals: `retired packs/player-damage-balance-20261024`. No push or personal installation changes.

## Player attribute balance replaces outgoing multiplier (2026-10-24)

Hidden -20% total modifiers now reduce player melee, ranged and offensive magic attributes. The previous outgoing 0.8 damage-event multiplier is removed. Only the separate 50% PvP event multiplier remains. Gear/skills and summon inheritance read reduced power; Healing Power is unchanged. Applied on join/death clone with idempotent periodic reconciliation. Same-name active/fixed PvP JAR installed after 54 isolated checks. [Source and proof](../docs/patches/pvp-flagging-26.2/README.md). Originals in `retired packs/player-attribute-balance-20261024`. No external world changes or push.

## Player and RPG summon retention replaces attribute penalties (2026-10-24)

Players and player-owned Spirit Wolves, Arcane Barrage emitters, Fire Hydras and Frost Elementals retain 35% damage against players and 80% against other entities. Ordinary entity combat remains 100%. Former attribute penalties are removed, and owners are checked for party/team/pet protection before summon damage. Same-name active/fixed PvP JAR installed after 118 isolated checks. [Source and report](../docs/patches/pvp-flagging-26.2/README.md). Originals: `retired packs/player-summon-balance-20261024`. No push or external installation changes.

## Crimson Strikes / Last Stand (2026-10-24)

Crimson heals 10% dual power once per successful Mutilate cast. Last Stand's 0.6-second immunity pulses are removed; other buffs remain. Three same-name archives installed after 908 isolated combat checks; originals retained in `retired packs/crimson-laststand-20261024`. [Proof and source](../docs/patches/crimson-laststand/README.md). No push.

## Berserker pulse healing

Berserker now fully heals on every pulse; fifth-pulse Speed IV and ending effects remain. Two same-name archives installed after 918 combat/shield/party checks. [Source and proof](../docs/patches/berserker-pulse-heal/README.md). Previous builds retained in `retired packs/berserker-pulse-heal-20261025`; no push.

## Bifrost native skill hotbar - 25 October 2026

Only the appended native Bifrost custom skill is enabled. It opens destinations instantly with zero spell cooldown; the existing teleport charge/travel cooldown remains. It follows class/accessory skills (verified right-click, 1-4, then Bifrost 5) and uses the supplied portal icon. Hofund is now a two-handed Diamond Claymore with Flurry. Old custom combat, throws, healing passives and selector/packet activation are dormant; ownership/death recovery and recipes remain. The same-filename replacement passed 42 connected-client/integrated-server checks, and the icon/handbook page were visually checked. [Build, scope and future slots](../docs/patches/bifrost-skill-hotbar/README.md). Active/fixed copies match; predecessors archived. No personal installation changes or push.

## Bifrost/Punchy weapon visuals - 25 October 2026

Added exact reference Punchy aliases for Stormbreaker, Mjolnir and Hofund; the client now plays the reference downward `attack_mace` clip for Stormbreaker instead of a generic sword clip. Enlarged Stormbreaker held displays by 1.5x, tilted/shrank its inventory thumbnail, and replaced Bifrost?s icon with the supplied Pixel Rainbow Portal Vortex. Seventeen native client/integrated-server assertions pass; first-person/third-person/inventory frames were visually checked. The reported damage issue was confirmed to be unclaimed weapons, and gameplay/ownership code remains byte-identical as requested. [Build, proof and scope](../docs/patches/bifrost-melee-visuals/README.md). Matching active/fixed copies installed; predecessors archived locally. No personal installation changes or push.

## Archer ordinary node balance (2026-10-25)

[Tracked patch](../docs/patches/archer-node-balance/README.md): ordinary ranged damage is 2% in Adventurer Skills and RPG class/weapon trees, including conditional bow rewards. Adventurer ranged penetration becomes 3% movement speed; Hunter loses 10% penetration, retaining its other bonuses. Node IDs and saved unlocks remain. Two same-name active/fixed JARs replaced after 573 archive/node checks; prior copies retired locally. No push requested.

## Bifrost creative and JEI discovery - 25 October 2026

[Repair and evidence](../docs/patches/bifrost-creative-discovery/README.md): missing creative registration hid all four items from creative search and JEI item listing. Marker now appears in Functional Blocks; weapons in Combat. Existing crafting recipes and recipe-book unlocks remain intact. The exact tested one-class replacement passed 26 connected-client/integrated-server checks and was installed under its unchanged filename in active/fixed folders; predecessors retired locally. No push.

## Rogue dual-power and attack-speed balance (2026-10-25)

[Source and tests](../docs/patches/rogue-dual-balance/README.md): all Rogue physical-melee spell schools now use combined dual-wield power. Explosive Powder 40% D; Slice bonus 8% D; Fracture 2 armor + 5% D/stack; Ambush 30% D; Crimson 15% D once/cast; Bloody Strikes 10s; Counter +15% D. Flat Bleed is 1/4 HP stationary/moving per 25 ticks. Adventurer ordinary attack speed is 2%, RPG Rogue ordinary attack speed 1%. Four same-name active/fixed JARs replaced after 971 native regression checks. Originals retired locally. No push or external installation changes.

## Warrior balance ? 25 October 2026

Installed matching same-name Rogues, Skill Tree and Spell Engine replacements: Killing Spree kill healing replaces reduction; Shout weakens power by 12%; Improved Wild Charge protects for 3s; Concussion stuns for 1s. Berserker grants Speed I-V per pulse and lasts 14/17s; Frozen now explicitly interrupts both ultimates. Enrage grants 3% damage/6% size per threshold; Tenacity lasts 14s. The concise description guide is preserved. See [build and 999 isolated checks](../docs/patches/warrior-balance/README.md); originals remain ignored under retired packs. No push requested.

Warrior proc follow-up: Sentinel has a 0.25s base stacking interval; Lethality has a 0.2s base hit-healing interval. Both retain native haste scaling and share the limit across targets. 1,010 isolated checks passed; same-name active/fixed replacements installed and prior copies preserved in `retired packs/warrior-proc-intervals-20261025/`.
