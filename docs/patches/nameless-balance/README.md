# Nameless Trinkets: pack balance and server repair

This same-version repair targets `mods/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar` (Curios variant, Java 25 bytecode). `balance.json` records the requested settings, 12 removed relics and Titan's effect durations/amplifiers. It retains all other upstream classes and resources except the explicitly changed classes, affected item tags, 36 removed recipes/advancements and the English tooltip updates.

## Character-scaling follow-up - 30 October 2026

[Character scaling](../accessory-rpg-balance/character-scaling.json) now supersedes earlier flat HP/attack values for Titan, Ghast Eye, Crystal Heart, Power Glove and Woundbearer. Chorus Totem healing, Thorn retaliation and Tick use the revised capped behavior in the [complete inventory](../accessory-rpg-balance/inventory.md). Legacy flat config fields remain for component/save compatibility; native item callbacks/tickers compute the effective scaled values. No extra dependencies are added.

## Current capped RPG revision - 30 October 2026

The [complete inventory](../accessory-rpg-balance/inventory.md) supersedes older numeric descriptions in the historical sections below. Moon Stone uses -0.02625 gravity (3/8 of the previous reduction). Offensive procs scale modestly from the strongest offensive attribute with fixed damage caps and wearer cooldowns. Missing Page and Vampire Blood no longer scale from victim maximum HP. Existing modifiers refresh to current values; legacy Woundbearer and Sigil storage are clamped. Fractured Nullstone recognizes the common magic damage tag, including RPG school damage.

All 42 retained trinkets and 12 disabled relics remain registered with their existing acquisition/equip guards. The Vampire lock remains 48,000 equipped ticks; size drawbacks stay. [New verification and current hashes](../accessory-rpg-balance/README.md) are separate from historical results below. Current effective values are in `effects.md` and `balance.json`. The build additionally includes `resources/`, `tooltip-overrides.json` and `nameless-pack-balance.json`; no personal config file is rewritten.

## Historical resulting pack behavior (superseded where listed above)


- 42 trinkets remain available. Broken Magnet, Cracked Crown, Dying Star, Electric Paddle, Ethereal Wings, God's Crown, Lunar Crest, Pocket Lightning Rod, Reforger, Speed Force, Super Magnet and What...? Magnet are disabled. Registry IDs remain for existing saves; old stacks are inert and removable, refuse equipping, and are excluded from crafting, random rewards, loot injection, the creative tab and JEI.
- `PackBalance` applies at `TrinketItem.setTrinketConfig`, both during construction and when old disk/network configuration is assigned. The changed fields cannot silently revert through stale generated JSON. Unlisted configuration fields retain their original behavior. Constructor defaults also match the policy. The existing JSON files themselves are not rewritten by this patch.
- The percentages are interpreted according to each handler: Rage Mind is **+15% / 1.15x** total damage, Scarab is **1.5x**, Shrinking Veil is **1.25x**, Vampire Blood is **+20% / 1.2x damage** and **+10% / 1.1x movement speed**, and XP is **+50% / 1.5x**. Reverse Card's random check is corrected from `<=` to a strict threshold for exactly 25%, rather than 26%.
- Ghast Eye gives **+6 maximum HP / +3 hearts**, Regeneration I initially for 5 seconds, and 2.5 seconds per extension. It does not give a flat six-HP heal on every kill.
- Titan's Mark grants +25% scale and +10% attack damage. Existing scale/attack modifiers refresh to these values without accumulating duplicates. While worn it refreshes **Slowness I for 1,200 ticks**, **Health Boost V for 100 ticks (+20 maximum HP / +10 hearts, confirmed by the user)**, and **Resistance I for 100 ticks**. Health Boost adds capacity without filling the added hearts. After removal, effects expire at their remaining durations; other sources of those effects are not forcibly cleared.
- Legacy crown, Dying Star and Speed Force attribute modifiers are removed when the corresponding old stack ticks or is unequipped. Ethereal Wings' old flight ability is cleared for survival/adventure wearers on those same paths. Personal worlds are not edited.

## Additional balance revision ? 18 October 2026

- Vampire Blood uses a persistent per-stack removal countdown of 48,000 equipped server ticks (two full Minecraft days, nominally 40 minutes). Restoring equipped stacks preserves the countdown; normal removal clears it and a new equip starts a new lock. Offline/unequipped time and sleeping do not advance it. Creative players can remove it immediately. This follows the existing timed-removal concept without sharing the timer through global Stats.
- Shrinking Veil refreshes Weakness II to 1,200 ticks/60 seconds while equipped. It can linger for its remaining duration after removal; other sources of Weakness are not forcibly cleared.
- Missing Page's radius setting is five blocks, with a distance check excluding the corners of the original cubic search. It retains 100% activation and 5% target maximum-health damage on qualifying incoming entity attacks.
- Titan's Mark now uses +25% size and +10% attack damage, retaining its existing refreshed health, Slowness and Resistance effects.

The subsequent balance adjustment sets Fractured Nullstone to 25% magic-damage reduction and Rage Mind to 1.15x marked-type damage. Vampire Blood retains 1.2x attack damage and adds 1.1x movement speed using a stable transient modifier refreshed while worn and removed on unequip. Its removal timer, kill healing and sunlight drawback remain unchanged.

## Why removing Lunar Crest alone was insufficient

The original `LunarCrestEvents` listener executes on damage events, and the shared `LunarCrestHandler` contains a client-only Minecraft helper. The dedicated server resolves `ClientLevel` when that shared class is reached, causing the supplied `NoClassDefFoundError`. This is independent of whether new Lunar Crests can still be obtained. The repair removes the listener entirely for the removed relic and replaces the obsolete client-bonus method with a zero-returning compatibility method, leaving no client-class references in the shared handler.

## One active copy per trinket type

The shared Curios equip rule blocks a second Nameless trinket of the same item type, reading live slots rather than Curios' same-tick cached `findCurios` lookup. Different types remain usable together. Existing/forced duplicates remain intact in their slots, with only the first copy receiving equip/tick callbacks. Removing one copy cannot clear modifiers while another remains. The last removal cleans up normally. Inactive duplicates can be removed despite ordinary removal restrictions; the active Vampire Blood retains its timer, and removed copies have their timer cleared so later equips cannot reuse an expired timer.

`PackUniqueness` supplies the shared policy. Build-time ASM guards cover 69 native Curios callback implementations (tick, equip, unequip and restricted removal). Vampire Blood handles its unequip guard in source so per-item timer cleanup always runs. The existing event handlers already select one equipped stack per item type. No duplicate stacks are deleted or moved.

## Rebuild

1. Preserve the original JAR as `tmp/nameless-balance/upstream.jar`. `repack.py` verifies its pinned SHA-256 before changing anything. The ignored retired-packs backup contains the same original bytes.
2. Provide a compilation classpath containing the **NeoForge-patched** Minecraft 26.2 client/common JAR, NeoForge/FML and their dependencies, the original Nameless Trinkets JAR, Curios 16.0.0+26.2, JEI 30.32.0.215, and ASM 9.10.1. Put the NeoForge-patched Minecraft JAR first; a vanilla-only JAR lacks the added creative/loot methods. This pass used the cached 26.2.0.88 development artifact and exercised the result on 26.2.0.90.
3. Run `build.ps1 -ClassPath '<semicolon-separated dependency paths>'` from the repository root with Java 25+ and Python available. It compiles production `src/` plus the separate build-only `tools/GuardDuplicates.java`, prepares the guard-tool environment and repacks the original archive to `tmp/nameless-balance/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar`. Test probes under `tests/` and the guard tool are never included in the production archive.
4. Inspect `repack.json`, run archive/static checks, and repeat the isolated tests before replacing the active same-filename JAR. Keep upstream license notices. Neither this patch nor its build script publishes artifacts.

## Verification — 17 October 2026

- A minimal dedicated server with Minecraft 26.2, NeoForge 26.2.0.90, Java 25, Curios 16.0.0+26.2, Nameless Trinkets and a test-only probe reproduced the original `ClientLevel` crash on player damage with a legacy Lunar Crest equipped.
- The repaired JAR passed **108 dedicated-server checks**, including that exact damage path, all requested numeric overrides after deliberately stale config assignment, all 12 removed relics' disabled/equip/tag/recipe paths, Titan's health/effect levels/durations/refresh and attack cleanup, guaranteed Poison I for three seconds on a susceptible creature, XP and magic-damage calculations, Vampire Blood's actual 1.2x attribute, Scarab/Veil movement ratios and legacy modifier cleanup.
- A separate isolated client with Minecraft 26.2, NeoForge 26.2.0.90, Java 26.0.2, Curios and JEI confirmed the 12 removed relics are absent from JEI while Titan's Mark remains available. The client and dedicated server both stopped normally.
- Static checks verify 42 entries remain in the acquisition tag, no removed relic survives the relevant item tags/recipes, and the archive passes CRC validation. Unrelated ZIP entries are preserved.

These are focused tests, not a full multiplayer playthrough of every surviving trinket and interaction. Test worlds, logs, probe JARs and intermediate classes remain under ignored `tmp/nameless-balance/`. The active pack and ignored fixed-packs folder hold the final JAR; the personal `.minecraft` installation and separate desktop server were not changed. The owner's detailed working reference remains `mod fixing/Nameless Trinkets - effects.md`.

## Revision verification ? 18 October 2026

The preceding build fails the new Titan attack-bonus assertion. The revised same-filename JAR passes **126 dedicated-server checks**, including the prior balance/removal/crash coverage plus the new stale-config overrides, Titan 1.25x scale/1.1x damage, Vampire Blood initial lock and expiry boundary, independent item timers, restored-equipment persistence and fresh-equip reset, Veil Weakness I duration/refresh, and Missing Page actual damage inside the radius with no damage outside or in cube corners. The probe forces its test chunk to load so the targets are searchable; it removes that ticket at shutdown.

The isolated client loads the revised JAR and confirms removed relics remain absent from JEI and Titan remains available. Both processes stop normally. That revision?s active/server/client/fixed-pack archives matched SHA-256 `8cba1f8f9a30304237d3bde84780425edc82bc600f81ffe20a46c65ce3419950`; the archive passes CRC checks and retains 42 acquisition-tag entries. The effects reference, README and handbook describe the revision. No personal installation was modified.

Latest follow-up verification: **131 dedicated-server checks passed**, including 25% actual magic reduction, 1.15x marked-type damage (unmarked types unchanged), and Vampire Blood?s 1.2x attack/1.1x movement, non-stacking and removal cleanup. The isolated client/JEI check also passed. Active/server/client/fixed copies match SHA-256 `215d97e344cb83436aaea78d626fd4c4a1d27b8d4a5f4e695db15282b33a4c5f`.

Duplicate-prevention verification: **226 dedicated-server checks passed**. Every retained trinket rejects a second copy and permits its first copy. Four forced Vampire Blood copies receive only one damage/speed modifier and one tick callback; inactive removal, primary handover, last-copy cleanup, preserved stacks and timer restrictions pass. Live-slot checks also cover repeated same-tick equipment changes. The revised archive loads in the isolated client and retains the JEI exclusions. Active/server/client/fixed copies match SHA-256 `a9d7800221a968f04c596761eb7a1ab49c2eef7ede2950a7d9ef3a352aa60569`.

## Shrinking revision - 21 October

Shrinking Veil retains **75% original size** (25% reduction), refreshes **Weakness II for 60 seconds**, and keeps its 1.25x movement bonus. Defaults and stale-config enforcement set 25% shrink. Equipped legacy half-size modifiers refresh to the new amount without accumulating. Existing duplicate-trinket guards remain.

Latest isolated server verification: **228 checks passed**, including 75% actual scale, repairing an already-equipped legacy half-size modifier, Weakness II refresh, stale-config enforcement, duplicate-copy restrictions and prior balance/removal coverage. No new client visual test was performed. Current hashes are recorded in `../artifacts-balance/shrinking-slots-verification.json`.
