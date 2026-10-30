# Nameless Trinkets: individual effects reference

Reviewed: 30 October 2026 against the implemented pack balance. Installed build: **1.2.4+26.2.1**, Minecraft **26.2**, NeoForge **26.2.0.90**, Curios variant.

This reference covers the **42 retained wearable trinkets** in the installed JAR's acquisition tag and separately records **12 removed legacy relics**, followed by its **six supporting items**. It describes effects from the packaged English text and local implementation source. It is not a claim that every effect has passed runtime testing. The pack overrides the specifically changed values and disables the removed relics. Other server settings can still change unlisted fields; personal configurations were not copied or edited directly.

All short IDs below use the `nameless_trinkets:` namespace. For example, Titan's Mark is `nameless_trinkets:titans_mark`. Effects generally require equipping the item in a valid Curios slot, rather than merely carrying it.

## Pack balance update

The requested pack balance is enforced when item settings are assigned, including old configs read from disk or synchronized settings. The JSON on disk may still contain its previous values; the pack policy wins for the changed fields. Unlisted fields keep their existing configuration. Removed item IDs stay registered only for save compatibility; they are inert, refuse equipping, and are excluded from crafting, random rewards, new loot, the creative tab and JEI.

### Capped RPG balance - current

The complete [87-item inventory](../accessory-rpg-balance/inventory.md) records current effective details. Stat-focused curios now use capped character scaling; health accessories share a baseline excluding each other and Health Boost. They do not heal on equip or refresh. Current policy metadata is embedded in both archives as `accessory-character-scaling.json`.

## Duplicate trinkets - no stacking

Only **one equipped copy of each Nameless Trinkets item type** can be active. Equipping a second copy is blocked across Curios slots, including rapid same-tick attempts. Different trinket types can still be used together.

If an existing save or forced inventory edit contains duplicates, one copy receives equip/tick effects; the extras stay intact but inactive. Removing an inactive copy does not remove the active copy's bonuses. If the active copy is removed, a remaining copy becomes active; bonuses clear when the last copy is removed. Inactive duplicate copies can be unequipped even if their normal removal restriction would lock them. Vampire Blood's active copy still obeys its removal timer.

## Reading the numbers

**Changed values below are pack-enforced settings; unchanged values are bundled defaults, not a dump of your live server configuration.** There are 42 retained trinkets and 12 disabled legacy items. Other mods, attribute caps, armor, event ordering and server overrides can change actual results.

- **+150% damage** means adding 1.5 times the original attribute: 100% + 150% = **250% total (x2.5)** before other modifiers.
- **150% total damage** means **x1.5**, or +50%. The implementation uses both conventions on different items.
- **10% damage reduction** is different from **10 armor**, **10 armor toughness** or **10% knockback resistance**. Knockback resistance affects knockback, not general damage reduction; the crowns are removed in this pack.
- **2 HP = 1 heart**. Times assume **20 ticks per second**; lag can extend real elapsed cooldowns.
- Ranges are the configured block values; several handlers use expanded bounding boxes, so they are not necessarily exact spherical radii.

## Individual trinkets

| Trinket | Item ID suffix | Effect | Pack values / unchanged defaults | Drawbacks, conditions and troubleshooting notes |
| --- | --- | --- | --- | --- |
| **Amphibious Hands** | `amphibious_hands` | Improves underwater mining speed. | Underwater mining x1.25 (+25%). | Does not provide underwater breathing. |
| **Blaze Nucleus** | `blaze_nucleus` | Reduces fire damage by 50%; qualifying enemy hits ignite for 4s and extinguish the wearer. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Blindfold** | `blindfold` | Makes the wearer invisible and reduces how readily mobs detect them. | Enabled; no configurable numeric strength in its Stats class. | Not a guarantee that every hostile mob loses its target or stops attacking. |
| **Broken Ankh** | `broken_ankh` | Provides a reusable Totem-like rescue from lethal damage. | 36,000-tick cooldown = 30 min. | Has a configurable recharge cooldown; it is not continuously available. |
| **Callus** | `callus` | Protects against cactus, falling-anvil, hot-floor and sweet-berry damage, and reduces fall and general incoming damage. | 10% fall-damage reduction; 5% other damage reduction; listed hazard damage is zeroed. These are separate branches, not a combined 15% fall reduction. | Damage reductions are configurable; this is not general invulnerability. |
| **Creeper Sense** | `creeper_sense` | Sneaking triggers a power-3 explosion on a 12s cooldown. No block destruction; the wearer can still be hurt. Party allies and their pets are protected. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Dark Nelumbo** | `dark_nelumbo` | Lets the wearer stand and walk on lava. | Lava-damage cancellation enabled by default. | Its configuration includes a lava-damage cancellation option; do not assume this protects against every fire source. |
| **Dragon's Eye** | `dragons_eye` | Highlights nearby hostile or aggressive mobs with glowing outlines. | 20-block search range; blindness enabled. | The configuration also enables blindness by default. This can explain an unexpected darkened view. |
| **Eclipse Ashes** | `eclipse_ashes` | Taking an enemy hit summons phantom wolves for 5s, spaced 1s apart. Each deals 1 damage plus 5% of your strongest offensive power, capped at 3. New sessions have a 12s interval; hits do not extend the session. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Experience Battery** | `experience_battery` | Increases experience obtained from qualifying entity kills. | +50% qualifying kill XP, approximately x1.5 total before rounding. | Does not promise a bonus to every XP source, such as mining or furnaces. |
| **Experience Magnet** | `experience_magnet` | Pulls nearby experience orbs toward the wearer. | 25-block configured attraction range. | Does not attract dropped items by itself. |
| **Explosion-proof Jacket** | `explosion_proof_jacket` | Reduces explosion damage by 20%; awards at most 1 TNT every 20s from qualifying explosion events. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Fate Emerald** | `fate_emerald` | Trade discount boost 25 (raw value, not a guaranteed 25% discount). Retains 60s removal lock and 0.1 exhaustion per second. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Fertilizer** | `fertilizer` | Periodically stimulates nearby crop and plant growth. | Growth interval 100 ticks = 5 s. | Uses a configurable interval; it is not instantaneous growth for every plant. |
| **Four Leaf Clover** | `four_leaf_clover` | Eligible non-player kills have a 25% chance of one extra loot-table roll. Not guaranteed duplication of original drops. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Fractured Nullstone** | `fractured_nullstone` | Reduces recognized magic damage by 15%, leaving 85% before other defenses. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Fragile Cloud** | `fragile_cloud` | Cushions descent using reduced gravity and fall-distance resets while falling. | Gravity modifier -0.07 while qualifying descent is active; resets fall distance. No configurable numeric Stats fields. | Its handler yields when Moon Stone is equipped; elytra flight, grounded movement and water have separate handling. |
| **Ghast Eye** | `ghast_eye` | Adds 7.5% of maximum HP before the three scaling health accessories and Health Boost, capped at +6 HP. Kills refresh Regeneration I for 5s without adding duration; stronger unrelated regeneration remains. | Capped character scaling. | Existing slot/equip restrictions remain. |
| **Gills** | `gills` | Allows breathing underwater by replenishing air while submerged. | Air decreases by 7 per tick outside suitable water; choking hit 2 HP (1 heart); choking blindness enabled; underwater air resets to 300. | Air drains outside water and can cause choking and blindness. Normal removal requires the wearer's eyes to be underwater. |
| **Ice Cube** | `ice_cube` | Applies Slowness to creatures you hit and freezes suitable water beneath your feet. | Slowness I for 100 ticks (5 s); Frost Walker setting 1; unchanged freezing-immunity option enabled. | Slowness strength/duration and freezing behavior are configurable; the source also includes a freezing-immunity setting. |
| **Light Gloves** | `light_gloves` | Increases mining speed when the wearer cannot see the sky. | Mining x1.5 (+50%) while unable to see the sky. | A roof can satisfy this check; the condition is not simply being below a fixed Y level. |
| **Lucky Rock** | `lucky_rock` | Gives a chance to obtain extra configured valuables when breaking eligible stone-like blocks. | 5% configured reward chance; eligible blocks and rewards are configured lists. | Both eligible blocks and reward items are configured lists; not every mined block qualifies. |
| **Miner's Soul** | `miners_soul` | Adds a configured Fortune bonus when calculating eligible block drops. | +1 Fortune level for eligible block loot; skips Silk Touch. | The handler skips Silk Touch and block-entity blocks; it is not an extra Looting level for mob kills. |
| **Missing Page** | `missing_page` | Taking sourced enemy damage releases a 5-block burst for 1 damage plus 10% of your strongest offensive power, capped at 6 per target. 4s interval; no boss-HP scaling. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Moon Stone** | `moon_stone` | Gently lowers gravity and prevents fall damage. Gravity reduction is 3/8 of the old effect (-0.02625; ordinary gravity becomes 0.05375). | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Nelumbo** | `nelumbo` | Allows standing and walking on water. | Water-walking enabled; no configurable numeric strength in Stats. | Does not supply breathing or faster underwater mining. |
| **Puffer Fish Liver** | `puffer_fish_liver` | Gives attacks a chance to poison targets and protects the wearer against poison. | 100% poison application on eligible targets; Poison I for 60 ticks (3 s). Poison-immune creatures remain immune. | Poison chance, strength and duration are configurable; poison immunity is not immunity to all negative effects. |
| **Rage Mind** | `rage_mind` | Deal +10% damage against the marked creature type; existing marking and range conditions remain. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Resonant Heart** | `resonant_heart` | Enemy hits have exactly a 10% chance to trigger a sonic retaliation for 2 damage plus 15% of your strongest offensive power, capped at 8. 6s interval. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Reverse Card** | `reverse_card` | Enemy hits have exactly a 25% chance to reflect 25% of incoming damage. Reflected damage is capped at 2 plus 10% of your strongest offensive power, never above 6. 4s interval; does not cancel the incoming hit. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Scarab Amulet** | `scarab_amulet` | Movement on sand +30% (1.3x); husk targeting protection retained. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Shrinking Veil** | `shrinking_veil` | Reduces body scale and increases movement speed, with refreshed Weakness II. | Movement x1.25 (+25%); scale x0.75 (-25%); Weakness II refreshed to 1,200 ticks (60 s). | Weakness can remain for up to 60 seconds after unequipping. It does not provide invisibility. |
| **Sigil Of Baphomet** | `sigil_of_baphomet` | Kills add 0.5s of damage immunity, banked up to 2s. Stored legacy counts are clamped to the current cap. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Sleeping Pills** | `sleeping_pills` | Removes nearby phantoms and grants night vision. | Phantom range 2 blocks; night-vision and weakness durations 1,200 ticks (60 s), refreshed by equipped behavior; bed sleeping disabled. | Also applies Weakness and, by default, prevents sleeping in beds. |
| **Spider Legs** | `spider_legs` | Enables climbing against walls. | Climb-speed setting 0.1 (raw movement value, not 10% speed). | Climb speed is configurable; this is not free flight. |
| **Tear Of The Sea** | `tear_of_the_sea` | Improves water movement/swimming capability. | +0.5 water-movement-efficiency attribute (configured as 50%); not a guaranteed +50% final swim speed. | Does not supply underwater breathing by itself. |
| **Tick** | `tick` | Once per second, drains eligible non-player enemies within an 8-block sphere for 1 damage plus 10% strongest offensive power, capped at 6 per target. Targets must have over 50 maximum HP and be above half HP. Each successful pulse costs the wearer 5% maximum HP, or exhaustion equal to 10% current food when below 5 HP. Own and party pets are protected. Normal removal still requires being on fire; creative bypass retained. | Capped character scaling. | Existing slot/equip restrictions remain. |
| **Titan's Mark** | `titans_mark` | Size +25%, attack damage +5%. Adds 15% of maximum HP before the three scaling health accessories and Health Boost, capped at +10 HP. Refreshes Resistance I for 5s and Slowness I for 60s; no healing on equip or refresh. | Capped character scaling. | Existing slot/equip restrictions remain. |
| **True Heart Of The Sea** | `true_heart_of_the_sea` | Combines underwater breathing, improved water movement and faster underwater mining. | Underwater mining x1.25 (+25%); other water and land-breathing effects unchanged: +1 water-movement efficiency, 1.5-HP choking damage, air drain 5/tick. | The wearer loses air on land and can choke and receive blindness. Treat it as a water-specialist item with a land-breathing drawback. |
| **Vampire Blood** | `vampire_blood` | Attack damage +10%; movement speed +10%. Enemy kills heal 2 HP plus 5% of wearer maximum HP, capped at 4 HP, once per 2s. Sunlight damage remains 2 HP. Removal stays locked for 48,000 equipped ticks (two full Minecraft days); offline time and sleep skips do not advance it. | Pack-enforced capped balance. | Existing slot and equip conditions apply; offensive procs protect party allies/pets. |
| **Wooden Stick** | `wooden_stick` | Provides a shield that can negate a hit, then recharges. | Recharge 200 ticks = 10 s. | Recharge cooldown prevents it from blocking every incoming hit. |
| **Woundbearer** | `woundbearer` | Stores 1% of damage taken as extra attack damage. Storage limit is 10% of melee power before Power Glove and Woundbearer, capped at +4 effective attack damage. Decay starts after 3s with base decay 0.01 per tick; legacy storage is clamped. | Capped character scaling. | Existing slot/equip restrictions remain. |

## Removed relics

These 12 relics are removed from obtainable pack content. Existing stacks are retained as disabled legacy items so saves do not lose registry IDs. They grant no effects and can be unequipped; old crown, Dying Star, Speed Force and flight modifiers are cleaned when the legacy item is ticked or removed.

| Removed relic | Item ID suffix |
| --- | --- |
| Broken Magnet | `broken_magnet` |
| Cracked Crown | `cracked_crown` |
| Dying Star | `dying_star` |
| Electric Paddle | `electric_paddle` |
| Ethereal Wings | `ethereal_wings` |
| God's Crown | `gods_crown` |
| Lunar Crest | `lunar_crest` |
| Pocket Lightning Rod | `pocket_lightning_rod` |
| Reforger | `reforger` |
| Speed Force | `speed_force` |
| Super Magnet | `super_magnet` |
| What...? Magnet | `what_magnet` |

## Titan's Mark refresh behavior

Size +25%, attack damage +5%. Adds 15% of maximum HP before the three scaling health accessories and Health Boost, capped at +10 HP. Refreshes Resistance I for 5s and Slowness I for 60s; no healing on equip or refresh. The health bonus uses a dedicated attribute modifier instead of renewing fixed Health Boost. Old short Health Boost expires naturally without stripping unrelated effects. Removing or disabling the item clears its owned modifiers; health is clamped only when capacity falls.

## Supporting items

These are not six additional wearable combat effects.

| Item | Item ID suffix | Purpose |
| --- | --- | --- |
| Dubious Dust | `dubious_dust` | Crafting ingredient used by the mod's recipes. Check JEI for recipes in this pack. |
| Glowing Dust | `glowing_dust` | Crafting ingredient; not a passive equipped buff. |
| Mysterious Trinket | `mysterious_trinket` | A usable random-trinket reward from the retained trinket pool. Extra-slot awarding is commented out in this installed implementation. |
| Trinket Bundle | `trinket_bundle` | Storage for eligible trinkets; keeping an item inside does not equip it or activate its effect. |
| Ultimate Dust | `ultimate_dust` | Crafting ingredient; not a passive equipped buff. |
| Unknown Fragment | `unknown_fragment` | Recycling material obtained through the mod's trinket-on-amethyst interaction. Recycling consumes the trinket; inspect the item and JEI before using it. |

## Current archive verification

The 30 October capped-balance revision retains all 42 available trinkets, 12 inert relics and six supporting items. See [the combined test record](../accessory-rpg-balance/README.md) for exact runtime results, hashes and verification limits. Earlier crash/uniqueness tests remain historical evidence; they are not counted as new checks.

## Confirmed crash and limits of this reference

The supplied dedicated-server report from **17 October 2026, 14:04:53** identifies `LunarCrestEvents.onLivingIncomingDamage`, followed by `NoClassDefFoundError: net/minecraft/client/multiplayer/ClientLevel`. The installed Lunar Crest handler contains a client-Minecraft helper in the same class used for server damage processing. This is a confirmed server compatibility defect, not an intended Lunar Crest penalty. The repaired pack removes the Lunar Crest event subscriber and removes client-Minecraft references from its shared handler. The original server crash was reproduced; the repaired server survived the same damage path with a legacy Lunar Crest equipped.

Evidence: the locally retained crash report (`mod fixing/crash-2026-10-17_14.04.53-server.txt`), especially lines 7-8, and the locally retained console log (`mod fixing/New Text Document.txt`), especially line 899 onward. The earlier AWI surface-rule and ALICompat/Spell Engine integration errors are separate from this fatal damage-handler exception.

The other rows describe intended behavior and code conditions, not a certification that they work correctly in this pack. In particular, distinguish configured percentage bonuses from multipliers, health points from hearts, and tooltip promises from implemented conditions when reporting issues. The project JAR is now repaired and balanced. Personal worlds, personal Minecraft configuration and the separate desktop server installation were not modified.

## Source record

- Installed JAR: `mods/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar`.
- JAR SHA-256: `294856632041f44dfd1b851a4e04717601afcb5e44e5b6086d1b7f9da3b1db31`.
- Item coverage and displayed names: embedded `data/nameless_trinkets/tags/item/nameless_trinkets_tag.json` and `assets/nameless_trinkets/lang/en_us.json`.
- Local source: `mod fixing/source/nameless-trinkets/`, particularly `common/.../items/trinkets/`, `common/.../events/`, and `neoforge-curios/.../items/trinkets/` and `events/`.
- Original numeric defaults were read directly from all 243 public instance fields in the 54 upstream `*Base$Stats` classes. Requested overrides and removals are recorded in `docs/patches/nameless-balance/balance.json`; the local patch source and verification notes are beside it.
- Installed bytecode was also inspected for the confirmed Lunar Crest client-class dependency. Other implementation details above were cross-checked against the local source, rather than runtime-testing every item.
