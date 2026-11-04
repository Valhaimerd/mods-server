# Combined class-balance phases 1-3

4 November 2026. This overlay supersedes the affected values in the earlier combat-phase-one, combat-followup-phase-one and Utilities patches. [Phases 4-6](../class-balance-phases-4-6/README.md) now layer the mage follow-up over these inputs; phases 7-9 remain planned. Active JAR filenames and saved skill IDs are retained; clients and servers need matching files and a restart.

## Shared combat

- Ordinary RPG magic now passes through native armor and toughness. General Resistance and damage reduction already cover physical and magic damage; they are not applied a second time. Pain Suppression retains its four 6/12/18/24% tiers against both.
- An active frontal item shield reduces qualifying physical or magic damage by 80%. Rear attacks, source-less damage and direct health costs retain their existing handling. Global player/summon damage retention remains 80% against mobs and 35% against players.
- The previous additional mage coefficient reduction is reverted using the original coefficient ledger. Fire Breath's existing delivery is 14% P; Frost Splinters is 20% P total. Later replacement deliveries are not introduced early. Arcane Beam's charged ramp is 10-15% per delivery; Echoing Blast is 160%, Shattering Lance splash 50%. Ordinary Hydra fireballs are 55%, Ancient breath 24%, and elemental Super Nova 180% of their own school power. Enchantments, healing, spell-power inheritance and the separate global retention are unchanged.
- Magistrate grants 15% magic resistance. Its offensive power and haste rewards are unchanged.
- Fractured Nullstone is retired from acquisition and creative eligibility, with its damage callback disabled and config forced inactive/zero. The inert registry ID remains for existing saves, matching the pack's established retirement policy. Other trinkets remain available.

## Adventurer defense and Utilities

Each defense path costs seven ordinary nodes plus one specialty. Five chain nodes split into two required penultimate nodes before the specialty. Existing defense icons and specialty icons are retained, with normal node sizes. All paths suit melee, ranged and magic users.

| Full path | Permanent benefits | Specialty |
| --- | --- | --- |
| Aegis | +4 HP, +2 armor, 4% general Resistance, 4% healing received | Surviving a direct enemy hit grants 15% general damage reduction for 3s; fixed 20s cooldown |
| Colossus | +6 HP, +2 armor, 2% general Resistance, 8% healing received | A direct hit below 40% HP starts five recovery ticks, totaling 10% sampled maximum HP before healing-received bonuses; fixed 30s cooldown |
| Bulwark | +2 HP, +4 armor, 8% general Resistance, 20 knockback-resistance points | All bonuses are permanent |

Defense rewards improve healing received, not outgoing Healing Power. A one-time scoped refund removes old Defense allocations while preserving other paths, points and earned category XP. Reactive cooldowns survive reconnects and death; removing the mastery clears its active benefit/recovery.

Utilities uses the same five-chain/two-branch/mastery layout. Fitness uses glistering melon, Adaptability diamond boots, and Ingenuity a writable book. Masteries require all seven ordinary nodes. Compact one-line titles replace embedded newline glyphs. Fitness fills hunger and saturation to 20 when at three drumsticks or less, once per three minutes; other Utility benefits and cooldowns remain. The golden hunger outline reflects real saturation.

Multi Roll keeps three enchantment levels: +1/+2/+2 additional rolls. Saved higher levels are also capped at two from this enchantment; other roll sources are not capped.

## Rogue

| Skill/node | Updated behavior |
| --- | --- |
| Shock Powder | 12s base cooldown |
| Slice & Dice | Stronger horizontal hit pull, retaining knockback resistance and ordinary movement/collision |
| Smoke Screen | 65 evasion points; existing cloud and refresh duration |
| Explosive Powder | Existing 40% D splash plus Weakness I and Blindness I for 3s |
| Coated Blades | Existing poison plus an 8s lock on natural regeneration and the Regeneration effect. Direct healing and absorption remain available |
| Vital Hit, formerly Fracture | Armor loss per stack: 1 + floor(D / 13). Three stacks, 6s. Each proc also deals physical damage equal to 5% target maximum HP; armor, resistance and global retention apply. Its extra hit is not lost to the triggering attack's hurt cooldown |
| Shadowstep | A successful enemy teleport roots its selected target for 1s |
| Bear Trap | 25s placed lifetime; Serrated Bleed 5s; Extensive hit radius 2 blocks and activation radius 1.5 blocks |
| Bloody Strikes | Bleed and combat-roll prevention for 5s, using the existing synchronized anti-roll enforcement |
| Base Vanish | Sprinting and rolling preserve Stealth; its movement penalty and other break rules remain. Shadow Stealth still breaks on outgoing damage |

## Warrior

| Skill/node | Updated behavior |
| --- | --- |
| Throw Weapon / Throw Net | Projectile speeds 1.8 / 1.4; existing 0.3s charge and damage curves remain |
| Endured Vitality | +1 armor and +2 maximum HP per stack; ten stacks and existing refresh/gain interval |
| Endurance | Wild Charge's 4 armor plus 4 additional armor: 8 total |
| Berserker / Last Stand | Five pulses at 0.8/1.6/2.4/3.2/4s before haste; earned base cooldowns 40/45/50/55/60s |
| Last Stand | Every pulse heals newly added HP and another 20% current maximum HP. No base Regeneration I or special fifth-pulse full heal. Knockback immunity remains from the first pulse; Sentinel's separate Regeneration II remains |
| Berserker fatal rescue | Stun lasts 8s; existing rescue healing, Nausea, cooldowns and death cleanup remain |

## Reproduction and evidence

Inputs are pinned by [baseline.json](baseline.json). [artifacts.json](artifacts.json) records exact changed members and final hashes. Builders read only the pinned input archives and produce isolated candidates. No probes ship.

```powershell
python docs/patches/class-balance-phases-1-3/check_resources.py --baseline
python docs/patches/class-balance-phases-1-3/build.py
python docs/patches/class-balance-phases-1-3/check_resources.py
python docs/patches/class-balance-phases-1-3/run_probe.py
python docs/patches/class-balance-phases-1-3/run_client.py
```

The server/client runners use fresh isolated worlds or the dedicated copied test world under `tmp/class-balance-phases-1-3`. Personal worlds and the separate desktop server are untouched. See [progress.md](progress.md) for final verification and installation state.
