# Combat rework — phase one

2 November 2026. **Implemented and installed in repository `mods/` and ignored fixed packs.** Same-filename overlays for Wizards, Skill Tree, Spell Engine, Paladins, Rogues & Warriors, CrossArms and Adventurer Skills. Phase two is deliberately excluded. Exact installed hashes are in [the completion record](completion.json).

## Gameplay

- Mage active skills, offensive upgrades, weapon spells and owned summon attacks use the rounded 80% coefficients in [the damage ledger](../../superpowers/plans/2026-11-01-mage-offensive-damage-scaling.md). Separate school passives, healing, power attributes and enchantments retain their values. Channel coefficients are normalized before rounding; summons are reduced once, without also reducing their inherited spell power.
- Arcane Missiles and Explosion target within **40 blocks** and have **12s** base cooldowns. Missiles always incurs its full haste-adjusted cooldown, regardless of channel completion.
- Echoing Blast gives Explosion **two banked casts**, dealing **130% Arcane Power** instead of the ordinary **210%**. One charge recovers at a time per haste-adjusted 12s. Spending the second does not restart the first recovery. Hits never refund charges. The hotbar shows the available count and a separate recharge bar while a charge remains usable.
- Recharge is saved using native cooldown records and copied on death/respawn. Offline time pauses it; haste is sampled when each serial recovery begins. Weapon switches and node removal do not refund charges. Ordinary pending Explosion cooldowns remain authoritative. Operator cooldown-reset commands remain intentional administrative overrides.
- Arcane, Flame and Frost Trap, plus Phase Shift, have **4s** base cooldowns. Arcane Trap applies **Weakness I for 6s**. Their other effects remain.
- All three Wards give **one absorption heart per complete eight matching school power**, last **8s** and have **15s** base cooldowns. Below eight power gives no shield. Refreshes preserve spent absorption. Overlapping mage Wards share the strongest capacity rather than adding shields together; a stronger shield grants only the newly added capacity. Capacity tracking persists with the entity.
- Adventurer ordinary melee nodes give **3%**. RPG Warrior root/ordinary damage nodes give **4%**; Rogue root/ordinary attack-speed nodes give **2%**. IDs, allocations, exclusions and attribute operations remain. Native reward loading/reloading refreshes existing allocations.
- Purge Heal targets within **30 blocks**. Blessed Strikes prepares five charges over **1.5s**, with **0.3s** between pulses. Divine Oath casts in **0.25s**; the hammer still needs to land. Throw Weapon and Throw Net fully charge in **0.3s**, retaining their existing minimum/maximum damage and range curves.
- Sinful Retaliation retains its readiness, mark, vulnerability and cooldown. Its burn is replaced by **eight raw 1-HP true-damage pulses**, first at 0.5s and last at 4s. The separate **Sinful Suffering** debuff uses the skill icon, a short holy particle cue and received-damage details. Absorption is consumed before health. Armor and ordinary reductions do not reduce the pulse, but explicit invulnerability, creative/spectator immunity, native totem protection and party safeguards remain. The existing world-configurable player-damage retention applies once (defaults 35% PvP / 80% PvE); no crit or haste multiplier applies to the ticks. Cleansing the debuff stops it.
- Purgatory's cooldown is **12s**, haste-affected, starting when channeling ends. Its planned containment wall and stronger first pull belong to phase two.
- Only tagged, player-owned Frenzy arrows bypass ordinary post-hit damage i-frames, through the normal arrow damage path. Armor, shields, resistance, party rules and explicit immunity remain. Frenzy's **20/30/40/50%** heat damage and all other enchantment mechanics remain. A first Frenzy hit still creates the normal damage timer for subsequent ordinary attacks.

## Key damage coefficients

All percentages are per impact, before criticals, defenses and the existing player-source damage policy. Summon attacks use their own school power.

| Arcane | Damage | Fire | Damage | Frost | Damage |
| --- | ---: | --- | ---: | --- | ---: |
| Missiles | 21% | Breath | 10% | Nova | 145% |
| Explosion / Echoing | 210% / 130% | Slash | 80% | Spikes / Fan | 64% / 20% |
| Chain follow-up | 24% | Meteor | 120% | Lance / splash | 160% / 40% |
| Beam / Charged ramp | 16% / 8–12% | Explosive Breath | 24% | Splinters | 16% |
| Emitter bolt | 32% | Meteor ground | 8% | Blizzard | 21% |
| Blink, each end | 40% | Firestorm / Wall | 32% / 16% | Elemental shard | 80% |
| Bolt / Blast | 56% / 64% | Hydra / Ancient breath | 44% / 19% | Elemental Nova | 145% |
| | | Scorch / Fireball / Pyroblast | 48% / 64% / 80% | Frostbolt / player Shard | 64% / 48% |

Elemental melee inherits 40% caster Frost Power, Legion 32%, Colossus 80%. Health, armor, size and spell-power inheritance are unchanged. Frost Fan's existing calibration already accounts for Improved; it is not reduced twice. Charged Beam's smooth ramp and width growth remain, with its new damage endpoints.

## Reproduction and evidence

```powershell
python docs/patches/combat-phase-one/build.py
python docs/patches/combat-phase-one/verify_data.py
python docs/patches/combat-phase-one/run_probe.py --baseline
python docs/patches/combat-phase-one/run_probe.py
python docs/patches/combat-phase-one/run_client.py
```

The builder starts from exact current archives preserved in ignored `tmp/combat-phase-one/before`; it never rebuilds from an older October baseline. `baseline.json` and `artifacts.json` record input/output hashes and exact modified/added entries. Changed classes target Java 25. Probes and worlds stay outside production JARs.

The baseline reproduces the old Missiles damage. **116 resource/archive checks** validate revised values, Java 25 bytecode and preservation of passive damage/healing. **196 isolated dedicated-server checks** exercise actual impacts, native cooldown spending/release, exact serial/haste boundaries, reload and clone state, real node rewards, Ward boundaries/overlap, arrow impacts and finite true-damage ticks. **10 integrated-client checks** exercise native casts, synchronized counts, an unavailable third cast, actual death/respawn and serial recovery. [HUD captures](echo-charge-hud.png) were visually reviewed. The 80-page handbook was rebuilt; all 51 contents destinations and printed page numbers were reconciled and changed pages reviewed.

Review caught and fixed zero-timer Frenzy restoration, charge cloning, incremental packets ignoring internal recharge IDs, and post-respawn client synchronization. Sinful Suffering retains explicit immunity and native totem protection; its periodic counter is separately visible. Tests use isolated fixtures with the real class/engine/party/damage-policy dependencies, not a full-pack live multiplayer playtest. No performance claim or additional spatial/Purgatory-containment fix is implied.

No personal Minecraft profile, desktop server, live world, commit or GitHub push is part of this batch. Both clients and server need the matching updated pack and a restart.

The native Spell Power API also passes 60 comparison checks using the exact enchantment modifier amounts/operations: three schools, five outside-school bonus levels, armor-only, weapon-only, both and neither. At 1 innate +19 flat power, the maximum-enchantment example yields36.75 power rather than20; the requested coefficient pass leaves147% of the unenchanted damage in that low-bonus example. This is why the20% reduction is approximate balance, not universal enchantment cancellation. Item-slot acquisition and compatibility rules are unchanged.
