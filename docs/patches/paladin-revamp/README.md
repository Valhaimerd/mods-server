Current Paladin follow-up values and archive identities supersede the affected earlier records: [Purgatory, Oath and worn Banner](../paladin-followup/README.md). Earlier tests/hashes below describe their original build.

# Paladin support rework and received-effect details

Current tier-III behavior and fixed Banner/Purgatory timing are documented in [the later Paladin tier-III update](../paladin-tier-three/README.md). Earlier build/test hashes below remain historical evidence.

Minecraft 26.2 · NeoForge 26.2.0.90 · Java 25 · 28 October 2026.

The new tier-II pair is Divine Protection / Divine Oath. Existing spell, effect, scroll and tree IDs remain compatible with saves. Tier I gains brief Purge Heal stuns and Blessed Strikes slows; Vanguard now provides Healing Power, while Guardian provides armor. Paladin and Priest damaging skill impacts deal 25% more damage to undead mobs. The former special undead power/critical bonuses are removed so this applies once. Healing and ordinary weapon attacks are unchanged.

## Final support values

P means caster Healing Power; M means caster physical-melee power. Support values are snapshotted before this skill's own previous bonuses.

| Divine Protection | Value |
|---|---|
| Activation | Instant; 28s haste-affected cooldown |
| Recipients | Caster and eligible allies within 8 blocks |
| Duration | 8s; Improved 10s |
| Damage reduction | 5% + 2% per P, capped at 50% |
| Flat mitigation | 1 + one additional point per complete 10 M; after ordinary defenses/percentage reduction, before absorption |
| Caster bonus | +10% Healing Power |
| Improved | Castable while controlled; cleanses movement impairments and prevents reapplication for 0.25s |
| Bulwark of Faith | Reduction becomes 5% + 5% per P, capped at 50% |
| Both branch upgrades | Knockback resistance 10% + 1% per M, capped at 80% |
| Merciful Aegis | First hostile hit starts five healing pulses, one per second for 5% caster P each |

Protection and Protective Banner use the stronger applicable Paladin reduction. Refreshing Protection does not rearm Merciful Aegis. Recovery is not a resurrection.

| Divine Oath | Value |
|---|---|
| Activation | 0.75s cast; 20s haste-affected cooldown; Improved 17s |
| Target | Self or eligible ally within 15 blocks; self fallback |
| Delivery | Original falling hammer follows the recipient and applies the effects on landing |
| Landing | 30% P damage within 4 blocks; 1s stun |
| Recipient, 7s | Extra damage 15% caster M, armor 5 + 10% caster armor, absorption 2 + 15% P |
| Decree | Extra damage 20% M; armor 5 + 15% caster armor; absorption 2 + 20% P |
| Judgement branch | 70% P landing damage, 6-block radius, 2s stun |

Extra damage has a hard 0.5s interval per recipient and cannot recursively trigger itself. Native player-damage routing still applies: 80% against mobs and 35% against players. A refreshed shield preserves damage already absorbed; only increased capacity grants additional HP. Timers and individual snapshots persist through normal saves; expiry, dispel and death clear their modifiers.

Vanguard gives 5% Healing Power with a shield equipped and four additional 5% stacks from hits, lasting 5s with a haste-affected 1s interval. Guardian gives 5% base armor with an empty offhand and four additional 5% stacks from attacker-sourced damage, lasting 5s with a haste-affected 0.25s interval. Modded shields use their blocking capabilities.

Sinful Retaliation replaces Conviction: rolling readies one retaliation for 6s, with a 12s haste-affected cooldown. The next attacker is revealed, takes 15% more damage for 8s and burns for 4s. Environment damage does not consume readiness. Break Restriction retains its 10s haste-affected cooldown and self cleanse. Only its selected, ready branch can roll out of control. Roll charges, food and other normal restrictions remain; a controlled grounded player can use actual block support when freezing clears the native grounded flag. Truly airborne rolls remain restricted by Combat Roll's setting.

## Effect details and artwork

Effect Insights keeps native stack-aware attribute rows for ordinary buffs/debuffs. A synchronized NeoForge attachment supplies caster-resolved custom values, extra armor loss, school vulnerability, absorption remaining and charge counts. Tooltips show received amounts rather than computing from the recipient's power. Pure control effects use plain restrictions instead of internal oversized negative movement modifiers. Supplemental text is wrapped by the native font splitter and follows the existing description visibility setting.

The user's three official transparent Root / Stun / Frozen images are converted to 64×64 nearest-neighbor textures. They replace applicable control-family icons without changing IDs. They are not regenerated artwork. New support states reuse existing skill-effect art.

## Rebuild and proof

`tmp/paladin-revamp/before/` pins the current input archives. Run `python docs/patches/paladin-revamp/build.py` to compile Java 25 helpers, apply asserted ASM hooks and build same-name candidates in `tmp/paladin-revamp/built/`. `artifacts.json` records every changed/added entry and before/after hashes. Unrelated entries remain byte-identical.

Run `run_server.py` and `run_server.py --legacy` for fresh/generated-config and stale-config native fixtures. The separate client scripts use an isolated copied world and offline identity, reading only cached launcher libraries/assets. The client's native Effect Insights extractor, inventory tooltips, falling hammer and Combat Roll input path are exercised. Personal installations and the separate desktop server are untouched.

Final results, exact tested hashes and reviewed screenshots are stored alongside these sources. Install the matching same-name repacks together on clients and servers. This patch does not publish or push anything to GitHub.

## Verified installation

- 77 fresh-config native checks and77 stale-config native checks passed.
- 26 actual-client checks passed, including native roll input, received values from a different caster, falling-hammer completion, shield anti-refill and control icons.
- Seven same-name active/fixed JARs match the tested hashes;5,700 unrelated entries are preserved.
- The79-page PDF passed51 TOC destination checks; all pages were rendered/reviewed and pages17/19 reviewed at reading resolution.

See [effect coverage](effect-coverage.md), [installed hashes](installed.json), [client evidence](client/client-checks.txt) and [handbook proof](handbook-verification.json).
