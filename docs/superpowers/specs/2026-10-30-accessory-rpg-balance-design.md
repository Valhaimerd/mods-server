# Capped accessory scaling

The owner selected modest RPG scaling with hard caps and authorized implementation immediately after planning. This covers all 42 available Nameless Trinkets and all 45 wearable Artifacts. The 12 retired relics stay retired. Supporting items, food and the Mimic egg remain available under their existing rules.

## Approach

Targeted static nerfs alone are cheaper but leave boss-health and attack-frequency exploits. Rebuilding all items as class powers would duplicate RPG mechanics. Use existing per-item configuration for ordinary bonuses and small shared helpers only for procs requiring power scaling, party checks or cooldowns. Utility items remain useful without unnecessary combat scaling.

## Definitions and constraints

- Offensive power is the highest current melee, ranged, Arcane, Fire, Frost or Lightning attribute, never their sum. Healing power does not increase retaliation damage. Missing optional attributes contribute zero.
- Numeric proc damage is capped before defenses and the pack's existing 35% player / 80% mob damage rule. Attribute bonuses are not scaled a second time. Accessories do not change that rule.
- Retaliation cannot trigger retaliation or life steal. Damage remains attributed to the wearer. Party members, their owned pets, the wearer, allied scoreboard entities and spectators are protected. Ordinary non-party mobs remain eligible.
- Proc cooldowns are fixed, per wearer and effect, persist in player data, and cannot reset by swapping items. No global haste reduction. Preserve native item cooldowns too.
- Keep 75% size / Weakness II, the 48,000-equipped-tick Vampire Blood removal lock and sunlight damage. Do not edit personal profiles, worlds or the separate desktop server.
- Preserve current JAR filenames, registry IDs, removed-item guards, Feet slot and native toggles. Retain optional dependencies as optional.
- No new generic damage-reduction cap that would interfere with class skills. Instead bound individual accessories, with multiplicative native combination and documented examples.

## Changed Nameless items

| Item | Target |
| --- | --- |
| Moon Stone | Gravity modifier -0.02625: 3/8 of its previous -0.07 reduction. Retain fall immunity. Ordinary 0.08 becomes 0.05375. |
| Missing Page | 100% qualifying sourced retaliation; 1 + 10% offensive power, capped 6 damage per target; 5-block sphere; 4s cooldown. No victim-max-HP damage. |
| Eclipse Ashes | Preserve wolves and presentation. 1 + 5% offensive power, capped 3 damage per wolf; existing 1s spawn spacing / 5s session; new sessions at most every 12s, no repeated refresh extending a session. |
| Resonant Heart | Exact 10% chance; 2 + 15% offensive power, capped 8 damage; 6s cooldown. Retain sonic visuals with ordinary defensive counterplay. |
| Reverse Card | Exact 25% chance; reflects 25% incoming damage, capped by 2 + 10% offensive power, maximum 6; 4s cooldown. Does not cancel original damage. |
| Vampire Blood | +10% attack, +10% speed; kill heal 2 + 5% wearer maximum HP, capped 4 HP, 2s interval. Lock and sunlight drawback retained. |
| Ghast Eye | +4 maximum HP; kills refresh Regeneration I to 5s without accumulating duration. Stronger unrelated regeneration is preserved. |
| Titan's Mark | +25% size, +5% attack, Slowness I; Health Boost II (+8 HP), Resistance I. No equip/refresh healing. |
| Woundbearer | Retain 1% storage; cap +2 attack, decay starts after 3s with base decay 0.01 per tick. |
| Rage Mind | +10% damage to marked creature type. |
| Fractured Nullstone | 15% magic damage reduction. |
| Blaze Nucleus | 50% fire damage reduction; ignites for 4s. |
| Explosion-proof Jacket | 20% explosion reduction; TNT reward at most once every 20s. |
| Creeper Sense | Explosion power 3, 12s cooldown; no block destruction. Still an intentional self-risk. |
| Four Leaf Clover | 25% chance of one extra eligible entity loot-table roll; no extra rolls for player kills. |
| Fate Emerald | Discount boost 25 raw; retain hunger and removal drawback. Not a guaranteed 25% discount. |
| Sigil of Baphomet | +0.5s immunity per kill, bank capped 2s. |
| Scarab Amulet | +30% movement on sand; husk protection retained. |

Fragile Cloud retains descent-only slow fall: it does not give Moon Stone's altered upward jump. All other 24 available Nameless items retain their audited values and conditions, enumerated in the implementation plan's inventory.

## Changed Artifacts

| Item | Target |
| --- | --- |
| Vampiric Glove | 5% actual health damage, maximum 2 HP, once per 1s. Basic melee only; no absorption, overkill, retaliation or ally farming. Received-healing modifiers remain native. |
| Feral Claws | +10% base attack speed. |
| Power Glove | +1 attack damage. |
| Crystal Heart | +6 maximum HP; no instant healing. |
| Cross Necklace | 10 extra invulnerability ticks, native 5s cooldown. |
| Running Shoes | +15% base sprint speed; retain step height. |
| Steadfast Spikes | 30% knockback resistance; retain slip resistance and Feet assignment. |
| Fire Gauntlet | 4s burning attribute. |
| Flame Pendant | Existing 40% chance; 4s fire and native fire protection, 4s cooldown. |
| Thorn Pendant | Existing 50% chance; 1-3 damage; 4s cooldown. Shared anti-recursion and friendly checks. |
| Shock Pendant | Existing 25% chance / sky restriction; visual lightning, targeted 2 + 10% offensive power capped 6 damage; 6s cooldown; no collateral lightning damage or fire. |
| Withered Bracelet | Existing 30% chance; Wither I for 4s; 4s cooldown. |

The other 33 wearable Artifacts retain their audited values. Native per-slot cooldowns remain, supplemented by wearer-level retaliation timers so switching stacks cannot bypass them. Native duplicate attribute IDs already prevent identical modifier multiplication; life steal and retaliation receive explicit wearer-level gates. No blanket slot removal.

## Verification and documentation

Use an isolated combined server, current NeoForge and the smallest dependency set; add RPG attributes and Better Party when exercising those paths. Test weak/strong/capped power, boss versus ordinary HP, two retaliators, pet/party protection, damage attribution, overheal/absorption/overkill, stale configuration, swap cooldowns, all 42/12 IDs and Feet slot. Keep exact observed results separate from remaining client/comparative playtesting. Rebuild the handbook and visually review changed pages. No commit or push was requested.
