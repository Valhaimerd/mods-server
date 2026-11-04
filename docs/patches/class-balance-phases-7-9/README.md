# Combined class-balance phases 7-9

> **Priest/Ward follow-up:** [Current cooldowns, coefficients and Arcane Ward distance gate](../priest-ward-tuning/README.md) supersede the affected older values below.

> **Superseding Purelight update:** [80s haste-affected cooldown and radiant body/armor](../purelight-glow/README.md) are installed. This replaces the earlier no-cooldown value below.

4 November 2026. Current Priest, Paladin and Archer follow-up, layered over installed phases 1-6 **and** the 20-block Ancient Hydra detection fix. Existing spell IDs, node allocations and active JAR filenames remain. Matching client/server JARs and a restart are required. Installation and final results are recorded in [progress.md](progress.md), not implied by this source overview.

## Priest

| Skill | Updated behavior |
| --- | --- |
| Circle of Healing | 60% Healing Power; Improved 75%. Other branches remain |
| Ascension | Four fixed pulses at 0.5/1/1.5/2s, unaffected by haste; cooldown still scales with haste. Each pulse grants the caster and eligible allies within 8 blocks 6% melee/ranged/offensive magic power, four stacks/24%; offensive duration remains 6s or Improved 8s |
| Ascension defense | Caster gains **50% general damage reduction for 6s**, refreshed per pulse, while levitation remains. Uplift grants it to allies actually lifted within 3 blocks. It covers ordinary physical and magic damage once, uses the stronger overlapping Paladin protection, and is not immunity. Direct costs and bypass exceptions remain |
| Serenity | Per pulse, caster and eligible allies within 8 blocks gain `1 + round(10% P)` absorption, up to four stacks, lasting 6s/Improved 8s. Independent recipient capacities and no-refill accounting remain; Uplift is not required |
| Fade | Full body/equipment concealment lasts 2s; loaded-pursuer aggro clearing and 6s haste cooldown remain |
| Divine Favor | Attacker and nearby eligible enemies are stunned for 3s; existing 15s readiness and 12s haste cooldown remain |
| Lightwell / Barrier | Base cooldowns 60s / 40s. Existing two-mote timing, lowest-raw-HP targeting, radius, lifetime, upgrade functions and shield accounting remain |

## Paladin and shared charge defenses

| Skill/passive | Updated behavior |
| --- | --- |
| Vanguard | Existing empowered melee damage/stun and 5s haste cooldown remain. A successful stun grants 15% general damage reduction for 2s, or 25% when activated with a shield. No permanent reduction |
| Guardian | Constant 5% armor/Healing Power; taking qualifying hits adds up to five stacks at 2%, or 3% with a shield, for 15%/20% total. Five-second refresh and existing 0.25s haste gain interval remain. Shield swaps recalculate earned stacks |
| Divine Protection | Base cooldown 20s. Each qualifying direct physical/magic attack consumes one charge and opens a **five-tick/0.25s** block window. More qualifying hits inside do not consume charges or extend time. Final charge retains its window, bounded by parent expiry. Existing reduction, scaling and branches remain |
| Divine Oath | Base cooldown 18s; Improved 15s. Damage/support scaling unchanged |
| Break Restriction | Existing controlled roll and cleanse plus 3s knockback immunity |
| Purgatory | Base cooldown 16s. Channel begins with +30% armor for 3s; early cleanup removes it immediately. Existing 24s channel, 8/16s stages, fixed pulses, HP costs, late-stage control immunity, damage and finishing branches remain |
| Purgatory containment | Closed ellipsoidal collision volume within the existing 12/14-block radius and five blocks above/below. Roof, side and underside restrict hostile movement. Trapped hostiles cannot use Pearl/Chorus or teleport across it. Caster/party protections and mandatory cleanup remain |
| Being of Purelight, formerly Reinforced | After other lifesavers/totems fail, a qualifying fatal hit is prevented and fully heals the player. Then **1 HP is lost directly every 0.5s**, bypassing absorption, until real downing/death. No added cooldown; no second rescue while active. Dispel, respec, save/load and reconnect do not remove the cost. Real PlayerRevive/Better Revive downing stops it. Old low-HP armor/Healing Power is removed |

Protection and Archer Deflection share one block-window implementation. Protection has priority if both are available. Ongoing Poison/Bleed/Wither/burning/Frostbite, environmental/unknown sources and direct/true HP costs cannot open or spend windows; ordinary direct Fire/Frost spell hits remain eligible. Dispel, death/respec and parent expiry cannot preserve a free window. This is separate from ordinary directional shield-item blocking.

## Archer

| Skill/passive | Updated behavior |
| --- | --- |
| Entangling Roots | 8s cooldown, Improved 6s; 4.5s clouds; at most two concurrent clouds per caster. A legal third retires the oldest, without granting banked casts or extending prior expiry. Existing radius, slow, cadence and branches remain. Base pulses refresh a 1s roll lock through the existing synchronized anti-roll effect |
| Rhythm | 4% draw speed per stack, five stacks/20%, retaining 4s refresh |
| Concussive Shot | 4s base cooldown; existing 1s stun |
| Deflection | 30s base cooldown. Existing low-health trigger, three charges and 10s parent duration. Qualifying physical or magic attacks use the shared five-tick windows. Each newly opened window grants a finite 1.3x movement multiplier for 2s; no stacking/refresh from extra hits inside a window |
| Magic Arrows | Base projectile speed 1.95 instead of 1.5. Conjured launch override receives the same 1.3x once, and seeking begins at 8 ticks instead of 10. Existing magical seeker profiles, wall-passing, damage and upgrades remain |
| Barrage | Each arrow deals 70% of its normal weapon shot, rather than a flat coefficient of ranged power. Count, spacing and branch behavior remain |
| Onslaught | Rolling readies the next direct player melee/ranged/spell hit for 5s. Successful eligible enemy damage consumes it once and grants 30% draw speed and 15% ranged damage for 4s. Periodic, passive-generated and summon damage do not consume it. Base cooldown 8s, haste affected, starts on roll. Rhythm is separate |

New effects reuse their skill artwork and expose received amounts in status details. Skill descriptions remain concise; native fields show triggers and cooldowns.

## Reproduction

`baseline.json` pins current installed inputs and metadata. `artifacts.json` records exact changed members and output hashes. Build/test work stays under `tmp/class-balance-phases-7-9`; probes do not ship. See the ledger for exact final checks and any limitations.

```powershell
python docs/patches/class-balance-phases-7-9/check_resources.py --baseline
python docs/patches/class-balance-phases-7-9/build.py
python docs/patches/class-balance-phases-7-9/check_resources.py
python docs/patches/class-balance-phases-7-9/run_probe.py
python docs/patches/class-balance-phases-7-9/run_regression.py
python docs/patches/class-balance-phases-7-9/run_mage_regression.py
python docs/patches/class-balance-phases-7-9/run_hydra_regression.py
python docs/patches/class-balance-phases-7-9/run_client.py
python docs/patches/class-balance-phases-7-9/verify.py --installed
```

As requested, this balance batch updates patch notes/raw-reference notices without rebuilding the handbook/PDF. No personal installation, separate desktop server, commit or Git push is included.
