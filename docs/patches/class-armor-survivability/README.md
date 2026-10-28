# Class armor survivability

Implemented 28 October 2026 for Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25, following the approved [design](../../superpowers/specs/2026-10-28-class-armor-survivability-design.md). Applies to 33 sets / 132 pieces: 19 native class sets and 14 Armory legendary sets. Paladin plate, Justicar and Lightbringer are excluded.

## Defense progression

All values below are full-set totals. Armor points are distinct from toughness.

| Tier | Wizard / Priest armor | Rogue / Archer armor | Warrior armor | Toughness | Durability |
| --- | ---: | ---: | ---: | ---: | --- |
| Entry |11|12|13|0|Native iron |
| Advanced |15|16|17|0|Native diamond |
| Netherite |16|17|18|4|Native netherite |
| Armory legendary |16|17|18|4|Native netherite |

Helmet/chest/legs/boots armor distributions:11=2/4/3/2;12=2/5/3/2;13=2/5/4/2;15=2/6/5/2;16=3/6/5/2;17=3/6/5/3;18=3/7/5/3. Netherite/legendary pieces each add 1 toughness. Native per-slot durability is iron 165/240/225/195, diamond 363/528/495/429, netherite 407/592/555/481.

policy.json lists the exact 33 sets and their targets. Armory coverage: Astral, Scarlet, Glacier, Tempest, Smouldering, Rimeweave, Avatar and Absolution robes; Deathmantle and Slayer Rogue armor; Strider and Riftstalker Archer armor; Destroyer and Onslaught Warrior armor. Their class/set bonuses remain. All targeted sets remain below diamond in armor/toughness and below corresponding Paladin plate in armor, with toughness never higher than the corresponding plate tier. This does not assert equal total survivability once skills, enchantments and set effects apply.

## Implementation

Each of the five mod JARs changes only its armor-registration class and adds its namespaced ClassArmorBalance helper. Existing Armor.Entry creation calls are routed through a wrapper that copies the native ArmorMaterial with only durability changed. This matters because Armor.CustomItem calls humanoidArmor and overwrites the initial Properties durability. The original attempted integer-only change failed runtime durability checks and was corrected before installation.

A registration hook applies approved armor/toughness to defaults and already loaded equipment configs. Redundant armor/toughness modifiers are removed from the affected piece lists (including conditional lists) so toughness cannot be counted twice. Other modifiers, knockback, repair materials, enchantability, IDs, assets, recipes and set effects remain. The previous Paladin flat Healing Power helper and Priest/Paladin doctrine restrictions are carried forward unchanged.

Existing wear and enchantment components are retained, including nearly broken saved stacks. No item is deleted during loading. Armory's native durability multiplier changes from 40 to 37; existing wear is not repaired or proportionally rescaled, so very worn Armory gear can have fewer remaining uses after the intended durability reduction. Explicit administrator-authored component overrides remain native overrides.

## Rebuild / verification

`python docs/patches/class-armor-survivability/build.py` uses immutable ignored input snapshots under tmp/class-armor-survivability/before and writes built candidates. artifacts.json records hashes and exact entry differences. Same active filenames are retained; ignored retired packs contain pre-task bytes and fixed packs contain final copies.

`python docs/patches/class-armor-survivability/run_server.py --baseline` reproduces the original failing target checks (726 checks, 350 failures) and captures native item/stack fixtures. The fresh candidate and `--legacy` runs each pass 1626 checks, covering every piece, equipped totals, native durability, preserved non-defense modifiers, excluded items and old worn/enchanted stacks. legacy-configs contains isolated earlier equipment settings; no personal configs were modified. The native client passes 462 checks, verifying every piece's synchronized equipment, armor tooltip, durability and full-set defenses. Five inventory screenshots capture representative class and Armory robes/light/heavy armor. Client screenshots were visually reviewed.

`python docs/patches/class-armor-survivability/verify.py --installed` verifies exact archive scope, tested hashes and active/fixed copies. 4159 other pre-existing archive entries are byte-identical. No personal client/world or separate desktop server was changed. These focused item/attribute tests do not claim a full combat playtest of every skill/set combination.

Restart matching client and server installations after syncing all five JARs. No commit/push requested.
