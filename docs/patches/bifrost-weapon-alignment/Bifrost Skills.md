# Bifrost Skills

25 October 2026. **Bifrost is the only enabled custom weapon skill.** Previous combat abilities and healing passives remain in the code as disabled future work. Their old selector, held-use channels and throw controls cannot activate them. The tables below preserve the proposed effects for later review; their historical input descriptions are superseded by the slot plan.

## Current weapon identities

| Weapon | RPG reference | Native weapon skill |
| --- | --- | --- |
| Stormbreaker | Netherite two-handed great hammer; 0.8 attack speed | Ground Slam |
| Mjolnir | Netherite Mace | Smash |
| Hofund / Heimdall's Sword | Diamond two-handed Claymore | Flurry |

The normal RPG weapon skill remains separate from Bifrost. Stormbreaker's longer handle and corrected held orientation remain. Race, binding and death-recovery restrictions remain enforced.

## Current Bifrost skill

- **Name:** Bifrost.
- **Effect:** open the existing named-destination menu.
- **Cast time / spell cooldown:** instant / 0 seconds. Closing or reopening the menu does not add a cooldown.
- **Source:** holding any of the three Bifrost weapons in the main hand.
- **Position:** appended after the normal weapon, class/book and active accessory skills. One shared Bifrost entry, with no duplicate on repeated updates.
- **Input:** the normal Spell Engine skill-hotbar binding for that position. It follows the current loadout and user bindings instead of reserving a fixed numeric key.
- **Example:** with the weapon skill on right-click, three class skills on 1-3 and an active trinket on 4, Bifrost occupies 5.
- **Travel remains unchanged:** selecting a destination starts the existing three-second teleport charge and five-second travel cooldown. The zero-second cooldown applies to opening the menu.
- **Icon:** the supplied Rainbow Rune Portal Gateway image.
- **Unavailable for now:** default hammer/axe throwing, beam/slash channels, custom selector moves and both custom healing passives. Right-click no longer starts these custom actions or opens Bifrost in air; it remains available to the native RPG weapon skill. Marker interaction remains available.

## Future appended-slot plan

Let `N` be the next skill position after the player's existing class and accessory skills. Keep that existing order, then append this weapon's skills in the following order. These are ordering plans only; slots after Bifrost are not implemented now.

| Weapon | N | N+1 | N+2 | N+3 | N+4 |
| --- | --- | --- | --- | --- | --- |
| Hofund | Bifrost | Charged Bifrost Slash | Realm Shift | Hilt Bash / Counter | Berserk Charging Slashes |
| Mjolnir | Bifrost | Lightning Beam | Shattered Shard Swarm | Lightning-Charged Shield Strike | Propulsion Tornado Vortex |
| Stormbreaker | Bifrost | Wakanda Ground Slam | Multi-Target Ricochet | ? | ? |

For the example where `N=5`, Hofund would use 5 for Bifrost, 6 for Slash, 7 for Realm Shift, 8 for Bash and 9 for Berserk. Do not silently replace existing bindings if a future loadout has more active skills than the hotbar can bind. Future throw/return needs a separate decision on its activation; it is dormant now.

## Screenshot notation

`W` means weapon damage before the listed multiplier. For example, `1.35W` means 135% weapon damage. The existing expansion uses the wielder's attack-damage attribute; the future balance pass must confirm whether that remains the intended basis. Caps in the tables are per target unless stated otherwise.

## Hofund

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Realm Shift | 0.35s window; slow enemies within 6 blocks for 2.5s and grant the user 10% movement speed | None | 35s |
| Charged Bifrost Slash | Hold right-click up to 0.8s; release an 8-block purple wave | 160% W on the first target; 100% on others; up to 6 targets | 12s |
| Hilt Bash / Counter | Bash within 3 blocks; counter if used within 0.6s after a blockable hit; briefly interrupt | 60% W bash or 110% W counter | 10s |
| Berserk Charging Slashes | Below 40% health; lunge up to 7 blocks and strike three times | 65% + 65% + 90% W; 220% W total cap | 30s |

**Future activation:** appended skill slot N+1; the screenshot?s held-right-click wording is historical.

## Mjolnir

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Throw and Return | Left-click in air, or Sneak + left-click a target; travel up to 20 blocks and return | 135% W; one hit per target per throw | 0.6s launch lockout |
| Lightning Beam | Hold right-click; 16-block beam for up to 3s; sustained-hit stacks | 85% W first hit, then 25% W every 0.4s; 250% W cap | 10s |
| Shattered Shard Swarm | Up to 8 shards seek 5 visible targets within 12 blocks | 45% W per hit; 90% W cap | 22s |
| Lightning-Charged Shield Strike | Requires an offhand shield; channel 0.7s, then fire a 10-block beam | 170% W first target; 90% W others; up to 4 targets | 25s |
| Propulsion Tornado Vortex | 5-block radius; lasts 3s and mildly pulls enemies | 35% W per second; 105% W cap | 30s |

**Healing passive:** five qualifying lightning hits trigger a 2 HP (one-heart) heal, with a 12s healing cooldown.

**Future activation:** Beam uses appended skill slot N+1. Throw and return are disabled for now; any later reactivation must preserve owner validation and protected death recovery.

## Stormbreaker

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Wakanda Ground Slam | 0.8s windup, short lift, then a 6-block shockwave | 180% W at the center, falling to 108% at the edge; up to 12 targets | 35s |
| Multi-Target Ricochet | Throw up to 24 blocks; each successful hit seeks an unhit target within 10 blocks in 3D, including different heights | 125% W first hit; 100% later hits; 180% W total on the first target if no second target is available | 16s |

**Healing passive:** below 40% health, a Ground Slam that damages an eligible enemy restores 4 HP (two hearts) over 4s. Cooldown: 60s.

**Separate skills:** native RPG Ground Slam on Stormbreaker is the standard hammer weapon skill. The screenshot's Wakanda Ground Slam is a separate custom ability for later review.

## Questions reserved for the later balance pass

- Rework charged/channel actions as native skill-hotbar casts using the appended order above.
- Confirm the weapon-damage basis and whether the shown caps remain appropriate after the native weapon-stat changes.
- Review existing shared healing lockouts, protected party members/pets, cooldown haste and ownership constraints before revising custom skills.
