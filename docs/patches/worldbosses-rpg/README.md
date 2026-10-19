# World Bosses 2.0.0 — RPG raid balance

Local same-filename repack for **Minecraft 26.2 / NeoForge 26.2.0.90**. Keep the active filename `world-bosses-neoforge-26.2-2.0.0.jar`; the installer detects changed content hashes. No new companion mod is required.

## Balance

| Boss | Base HP | Base armor | Base melee input | Base regeneration |
| --- | --- | --- | --- | --- |
| Sylvaron | 7,000 | 16 | 8 HP | 10 HP/s |
| Aureon | 8,000 | 14 | 8 HP | 10 HP/s |
| Nekhraz | 9,000 | 18 | 9 HP | 11 HP/s |
| Varkhul | 10,000 | 20 | 10 HP | 12 HP/s |
| Morvath | 11,000 | 16 | 9 HP | 13 HP/s |
| Elyndra | 12,000 | 18 | 10 HP | 14 HP/s |
| Shadow King | 14,000 | 20 | 10 HP | 16 HP/s |
| Dragon Mother | 15,000 | 20 | 10 HP | 17 HP/s |
| Storm Griffin | 14,500 | 18 | 10 HP | 16 HP/s |
| Astrael | 18,000 | 22 | 12 HP | 19 HP/s |
| Shadow Weaver | 20,000 | 22 | 12 HP | 20 HP/s |

Each additional active participant adds **15% of base HP**, **10% of base regeneration**, and **10% of base armor**, additive, up to eight participants. Four players mean 1.45× HP and 1.30× regeneration/armor. The highest reached count stays until reset. Armor is clamped to Minecraft's existing **30-point limit**; toughness, outgoing damage and attack speed do not scale with the party.

The maximum-health attribute's permitted range is raised to one million so the actual health pools, boss bars, phase fractions, healing and saved health agree. This changes the permissible range on both sides, not other entities' actual health or their base attributes. Regional bosses no longer rely on realm-only effective-health compensation.

The native `config/worldbosses.json` receives a **one-time migration** marked by `rpgBalanceVersion = 1`. Unrelated world generation, rituals and presentation settings are preserved. Added settings are `<boss>.rpgRegenerationPerSecond`, `<boss>.raidScaling.regenerationPerExtraPlayerPercent`, and `<boss>.raidScaling.armorPerExtraPlayerPercent`. First-time migration is applied before saving, including existing installations. Subsequent reloads do not repeat the migration or overwrite later base-stat edits. `/worldbosses reload` remains available.

Combat healing starts after ten seconds, checks every second, and pauses for invulnerability/Shadow Weaver's awakening. It uses the per-boss base times the party regeneration factor, with a 0.2%-of-maximum-HP safety ceiling. Morvath's extra drain heal is 10 HP without phase multiplication, and pressure lifesteal is disabled.

Regional phase damage multipliers are 1/1.05/1.10 and cooldowns 1/0.90/0.80; Astrael's final enrage adds 0.90 cooldown. A shared major-cast gate leaves three seconds between regional major ability starts. Realm windups are at least two seconds, flight dives have a separate two-second warning, and their attack values/veil defenses follow the balance reference. Shadow Weaver's combined clone/veil reduction is 55%, replacing 97%.

## Damage ownership and terrain safety

- Boss-owned direct hits, projectiles, minions and effects share **12 HP per player per rolling 20 ticks**, after difficulty/boss multipliers and before player defenses. Owned lightning keeps its damage type and receives an explicit boss owner.
- Persistent boss hazards are limited to **2 HP per hazard per second**, **4 HP combined per second**, within the overall budget. Normal hurt invulnerability is retained.
- Boss poison/wither ownership exists only during the recorded effect's own tick. Generic ownerless magic, including **CrossArms Frenzy self-damage**, does not become boss damage merely because the player recently fought a boss.
- Boss status applications are shortened, with slowness limited to II. Boss fire duration and ownership are tracked separately. Existing unrelated damage/effects retain their ordinary behavior.
- Source-owned pushes, pulls and vanilla knockback validate a short projected path. Unsafe forced movement into fluids, unsupported edges or outside the arena is canceled. A short ownership interval protects subsequent displacement-related fall/void/lava damage and uses a checked return location. Ordinary unrelated environmental damage is not globally disabled.
- Shadow Step checks a boss-sized destination, support, fluids, arena distance and world border; an invalid destination cancels the teleport.

Ranged targeting reaches 32–40 blocks for selected native skills, without expanding Aureon's impact radius past three blocks. Six continuous seconds of unreachable engaged positioning begins the common response: a fixed-position eruption marker with two seconds' warning. Continuing to hide produces a separate three-second relocation warning, then a checked open point 8–12 blocks from the boss and two seconds' boss-damage protection. Restored reach, leaving eligibility or loading a saved fight clears pending warnings, preventing stale instant relocations. No blocks are destroyed.

Wipes/abandoned encounters reset health, party count, phase/cast state and owned nearby summons/projectiles after the participation grace period. The original arena coordinates are retained exactly, including the altar identity. A nearby active healer or evading participant prevents an abandonment reset.

## Retired equipment and rewards

**Twenty combat items are retired:** the Worldbreaker and Shadowbreaker sword, axe, pickaxe, shovel, hoe, helmet, chestplate, leggings, boots and shield. Their recipes, creative entries, gear upgrade recipe and armor set bonus registration are disabled. The obsolete Shadowbreaker armor advancement is removed; boss-clear and challenge advancements are retained.

Registry IDs remain for save compatibility. At the shared ItemStack constructor, old equipment becomes matching vanilla netherite equipment or a vanilla shield. Names, supported enchantments and proportional durability are retained; World Bosses upgrade components and custom combat attributes are removed. This covers loaded containers, inventories, equipment and newly generated stacks. Build/runtime checks verify all twenty IDs and a legacy stack containing an upgrade, custom damage and enchantments. Do not deploy a world migration without a normal world backup.

Gear-only alloy/template/fragment rewards are excluded. Codices, compasses, ritual items, portal dust/sparks, boss elements, required realm drops and noncombat collectibles remain. `primordial_hammer` is a plain registered item in this version, not one of the twenty combat items.

Each eligible participant receives one cache; opening it delivers a guaranteed package instead of the upstream random reel. Existing caches use the same replacement opening path.

| Tier | Bosses | Golden apples | Netherite ingots | Totems | Diamonds | XP bottles | Raw XP at clear |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I | Sylvaron, Aureon, Nekhraz | 4 | 1 | 1 | 8 | 32 | 2,000 |
| II | Varkhul, Morvath, Elyndra | 6 | 2 | 1 | 12 | 48 | 4,000 |
| III | Shadow King, Dragon Mother, Storm Griffin | 8 | 3 | 1 | 16 | 64 | 6,000 |
| IV | Astrael, Shadow Weaver | 10 | 4 | 2 | 24 | 96 | 10,000 |

These are **regular** golden apples. No enchanted golden apples or retired combat gear is rolled. Existing trophies are granted where registered. Each player's first Astrael clear and first Shadow Weaver clear grants one Nether Star.

Eligibility requires recorded damage, tanking or real Spell Engine healing of an engaged ally in the last 60 seconds. Nearby bystanders alone do not qualify. Deferred grants survive death/offline periods and include boss-clear/challenge advancements. Encounter/player deduplication and first-clear flags are stored in vanilla world command storage under `worldbosses:rpg_rewards`; larger ledgers use bounded NBT string chunks. Inventory overflow uses one give-or-drop path. Successful shrines and realm altars have a 30-minute cooldown.

## Build and verification

Original installed JAR SHA-256: `20b63910decdb07caabf90dc0c0620aa6fdd93982b1a44ec84f140a39b481dc7`.

Place that exact original at `tmp/worldbosses-rpg/upstream.jar`. The classpath file must include it, the NeoForge-patched Minecraft 26.2 development JAR, loader/ASM dependencies, GeckoLib and the installed Spell Engine API. Build with:

```powershell
python docs/patches/worldbosses-rpg/build.py --classpath-file tmp/worldbosses-rpg/classpath.txt
```

The builder compiles Java 25 helpers and asserted ASM hooks, removes 25 equipment/upgrade recipes plus the obsolete gear advancement, preserves the upstream metadata version, and outputs the candidate under `tmp/worldbosses-rpg/`. Added archive entries use fixed timestamps. It does not overwrite the active JAR.

```powershell
javac --release 25 -d tmp/worldbosses-rpg/math-test docs/patches/worldbosses-rpg/src/local/worldbosses/RaidMath.java docs/patches/worldbosses-rpg/tests/RaidMathTest.java
java -cp tmp/worldbosses-rpg/math-test RaidMathTest
python docs/patches/worldbosses-rpg/tests/prepare.py
```

Run the prepared isolated server with NeoForge 26.2.0.90's `win_args.txt` and inspect `tmp/worldbosses-rpg/server/result.txt` plus the complete probe log. The production JAR never contains the test mod. `verification.json` records final archive hashes and observed check results.

Validation covers actual entity health/scaling/healing, saved encounter state, gear conversion, source-owned damage limits, unrelated magic damage, native creative tabs, warning cancellation, checked movement/relocation, progression/advancement delivery, every cache tier, dead contributors, first clears and ledger reloads. Test versions: **World Bosses 2.0.0**, **GeckoLib 5.5.6**, **Spell Engine 1.10.5+26.2**, **Spell Power 1.6.1+26.2**, **Player Animation Library 1.2.8**, **Bendable Cuboids 2.0.4**, **Cloth Config 26.2.155**, **NeoForge 26.2.0.90**, Java 26.0.2.1 running Java-25-targeted code.

**Limits:** these are isolated automated server checks, not a measured full-pack raid playthrough or client visual/animation test. Real late-game kill times, mixed-mod loadouts, economy impact and human-readable arena cues still need playtesting. No personal world/server was modified, and nothing is pushed by this workflow.
