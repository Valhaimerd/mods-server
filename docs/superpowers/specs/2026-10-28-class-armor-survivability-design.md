# Class armor survivability balance

Date:2026-10-28. Approved by the user: ?Implement them now?. Implemented values and evidence: ../../patches/class-armor-survivability/README.md.

## Intent

User chose small defensive differences between Wizard/Priest robes, Rogue/Archer armor and Warrior armor, progressing beneath the corresponding Paladin-series plate tier. Include Armory legendary sets. Improve caster survivability without changing class damage/healing bonuses or adding hidden damage reduction.

## Approaches

Equal defense for all five classes was considered, but removes the small class distinctions the user prefers. Retaining current protection (7 for robes,12 for legendary Rogue/Archer,20 for legendary Warrior) leaves casters too vulnerable. Chosen direction: modest class differences, equivalent protection within a class at a given tier, with shared durability progression.

## Proposed concrete values (full four-piece sets)

| Tier | Wizard / Priest armor | Rogue / Archer armor | Warrior armor | Total toughness | Durability standard |
| --- | ---: | ---: | ---: | ---: | --- |
| Entry |11|12|13|0|Iron equivalent |
| Advanced / diamond progression |15|16|17|0|Diamond equivalent |
| Netherite |16|17|18|4|Netherite equivalent |
| Armory legendary |16|17|18|4|Netherite equivalent |

Armor point distributions in helmet/chest/legs/boots order:11=(2,4,3,2);12=(2,5,3,2);13=(2,5,4,2);15=(2,6,5,2);16=(3,6,5,2);17=(3,6,5,3);18=(3,7,5,3). Toughness is per-piece0/0/1, yielding totals0/0/4. Durability means each corresponding slot matches the native iron/diamond/netherite item maximum durability, checked against installed Minecraft rather than guessed constants. No class durability penalty.

These implementation details were approved. Diamond baseline20 armor/8 toughness remains stronger in raw armor/toughness than every targeted set. Paladin plate is preserved:15/20/20 armor and0/0/4 toughness by tier. Advanced class toughness stays0 so it does not gain a per-stat advantage over Crusader plate. Netherite/legendary class gear matches plate toughness but has fewer armor points.

## Exact coverage

Wizard entry: wizard_robe. Advanced: arcane_robe, fire_robe, frost_robe. Netherite: their netherite variants.
Priest entry: priest_robe. Advanced: prior_robe. Netherite: netherite_prior_robe.
Rogue entry: rogue_armor. Advanced: assassin_armor. Netherite: netherite_assassin_armor.
Archer entry: archer_armor. Advanced: ranger_armor. Netherite: netherite_ranger_armor.
Warrior entry: warrior_armor. Advanced: berserker_armor. Netherite: netherite_berserker_armor.
Armory robes: astral, scarlet, glacier, tempest, smouldering, rimeweave, avatar, absolution. Rogue: deathmantle, slayer. Archer: strider, riftstalker. Warrior: destroyer, onslaught. All use legendary row. Justicar and Lightbringer stay unchanged.

## Preservation and delivery

Preserve all existing offensive/healing/haste/evasion/knockback modifiers, set bonus definitions, recipes, item IDs, doctrine restrictions and recent Paladin flat Healing Power values. This equalizes basic equipment defense, not total class survival after skills and set bonuses. Existing enchantments and damage remain; test component max-damage changes do not repair or destroy existing worn items unexpectedly. Existing generated equipment settings must receive pack defense targets, with unrelated settings preserved. Do not edit personal profiles or desktop server.

Use existing registration/configuration hooks where possible. Repack same active filenames for Wizards, Rogues, Archers, Paladins and Armory; preserve prior unrelated JAR patches, keep ignored backups and final fixed-pack copies. Verify native item defaults and equipped attributes, fresh and old configs, all targeted items, protected Paladin plate and non-defense modifiers, then isolated client tooltips. Update working class references, README/MODLIST and rebuild/review handbook PDF. No push unless requested. No workers for this design; prefer inline implementation.

## Approved decision

User approved implementation of the tier table with toughness totals0/0/4, all14 Armory sets and preserved Paladin plate. Installation, runtime verification and handbook synchronization are recorded in the patch README.
