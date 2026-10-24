# Bifrost Skills

24 October 2026. **Planning reference for later balancing.** The tables below preserve the supplied screenshots; they are not approval to add or rebalance these custom abilities in this pass. Some abilities already exist in the prior expansion. Their current implementation is not being removed or rewritten by this document.

## Work requested for this session

| Weapon | RPG reference | Native weapon skill |
| --- | --- | --- |
| Stormbreaker | Netherite two-handed great hammer; 0.8 attack speed | Ground Slam |
| Mjolnir | Netherite Mace | Smash |
| Hofund / Heimdall's Sword | Diamond Sword | Swift Strikes |

Stormbreaker's handle is lengthened and its held orientation reversed. Weapon attributes are matched to the installed references, before the player's race, skills and equipment bonuses. The custom skills below are separate from these native weapon skills.

## Planned controls

- Mjolnir should be throwable by default and return to its owner.
- Right-click with Mjolnir, Hofund or Stormbreaker should open Bifrost travel again.
- Resolve a new activation for Hofund's charged slash and Mjolnir's beam before replacing their current right-click actions.
- These control changes are recorded for later, as requested; this pass does not reroute inputs or alter the existing throw/ownership system.

## Screenshot notation

`W` means weapon damage before the listed multiplier. For example, `1.35W` means 135% weapon damage. The existing expansion uses the wielder's attack-damage attribute; the future balance pass must confirm whether that remains the intended basis. Caps in the tables are per target unless stated otherwise.

## Hofund

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Realm Shift | 0.35s window; slow enemies within 6 blocks for 2.5s and grant the user 10% movement speed | None | 35s |
| Charged Bifrost Slash | Hold right-click up to 0.8s; release an 8-block purple wave | 160% W on the first target; 100% on others; up to 6 targets | 12s |
| Hilt Bash / Counter | Bash within 3 blocks; counter if used within 0.6s after a blockable hit; briefly interrupt | 60% W bash or 110% W counter | 10s |
| Berserk Charging Slashes | Below 40% health; lunge up to 7 blocks and strike three times | 65% + 65% + 90% W; 220% W total cap | 30s |

**Pending input conflict:** charged right-click Slash in the screenshot conflicts with the planned right-click Bifrost control. Choose its replacement input later.

## Mjolnir

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Throw and Return | Left-click in air, or Sneak + left-click a target; travel up to 20 blocks and return | 135% W; one hit per target per throw | 0.6s launch lockout |
| Lightning Beam | Hold right-click; 16-block beam for up to 3s; sustained-hit stacks | 85% W first hit, then 25% W every 0.4s; 250% W cap | 10s |
| Shattered Shard Swarm | Up to 8 shards seek 5 visible targets within 12 blocks | 45% W per hit; 90% W cap | 22s |
| Lightning-Charged Shield Strike | Requires an offhand shield; channel 0.7s, then fire a 10-block beam | 170% W first target; 90% W others; up to 4 targets | 25s |
| Propulsion Tornado Vortex | 5-block radius; lasts 3s and mildly pulls enemies | 35% W per second; 105% W cap | 30s |

**Healing passive:** five qualifying lightning hits trigger a 2 HP (one-heart) heal, with a 12s healing cooldown.

**Pending input conflict:** the beam's held right-click input conflicts with planned Bifrost travel. Default throw and owner return remain a separate planned requirement; do not infer a new auto-recall, ownership or death-recovery rule from it.

## Stormbreaker

| Skill | Activation and effect | Damage | Cooldown |
| --- | --- | --- | --- |
| Wakanda Ground Slam | 0.8s windup, short lift, then a 6-block shockwave | 180% W at the center, falling to 108% at the edge; up to 12 targets | 35s |
| Multi-Target Ricochet | Throw up to 24 blocks; each successful hit seeks an unhit target within 10 blocks in 3D, including different heights | 125% W first hit; 100% later hits; 180% W total on the first target if no second target is available | 16s |

**Healing passive:** below 40% health, a Ground Slam that damages an eligible enemy restores 4 HP (two hearts) over 4s. Cooldown: 60s.

**Separate skills:** native RPG Ground Slam on Stormbreaker is the standard hammer weapon skill. The screenshot's Wakanda Ground Slam is a separate custom ability for later review.

## Questions reserved for the later balance pass

- Assign inputs for the charged slash and lightning beam once right-click returns to Bifrost.
- Confirm the weapon-damage basis and whether the shown caps remain appropriate after the native weapon-stat changes.
- Review existing shared healing lockouts, protected party members/pets, cooldown haste and ownership constraints before revising custom skills.
