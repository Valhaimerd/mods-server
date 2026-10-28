# Paladin Revamp and Effect Details Implementation Plan

Execution: primary agent inline with executing-plans; no worker dispatch. Spec: ../specs/2026-10-28-paladin-tier-two-and-effect-details-design.md. No commit or push.

Goal: deliver the approved tier-I/tier-II Paladin revamp, uniform undead damage and accurate effect details/control icons while preserving current pack changes.
Architecture: extend current Paladin helpers, use native Spell Engine impacts/projectiles/effects and NeoForge synchronized attachment data. Reuse Effect Insights attribute rendering and append only missing received values. Patch specific roll/cast eligibility callsites, not global control semantics.
Tech: Java25, Minecraft26.2, NeoForge26.2.0.90, current pinned JARs.

- [x] Snapshot current inputs and dump registered effect audit. Record baseline resource values and add focused native acceptance assertions for the new values and caps.
- [x] Implement shared synchronized state/details attachment registration in Spell Engine NeoForge constructor. Support expiry/dispel/death/refresh without absorption refill and ensure no client-only classes are loaded on servers.
- [x] Extend Paladin tier-I impact/stun/slow and passive helper/config/resources; add one scoped native undead impact wrapper and remove old undead-specific bonuses.
- [x] Implement Divine Protection custom delivery and scalar snapshots/mitigation/strongest-overlap. Disable only its old Protection registry registration. Implement control cleanse/immunity and node-gated cast permission.
- [x] Implement self/ally falling Oath delivery and impact,7s snapshot buffs/armor/absorption and per-recipient hard0.5s extra-damage limit. Implement node variants, no friendly fire, no recursive extra hit and correct attribution.
- [x] Implement Sinful readiness/retaliation and Break Restriction cleanse/controlled-roll gate in both client and server validation. Preserve native roll resources and prevent the unselected branch from escaping control.
- [x] Add resolved-value supplemental effect descriptions, audit all class effect modifiers/custom mechanics and provide source-aware values. Convert/install supplied Root/Stun/Frozen icons without redesigning art. Keep original native numeric attribute lines.
- [x] Build and run focused native fresh/legacy tests. Test caps, procs, party targeting, undead exactly once, suppression/absorption/mitigation, snapshots, cleanup and icon/resource scope. Resolve each actual failure before claiming success.
- [x] Run an isolated actual client: self/ally casts, control escape and node/cooldown restrictions, caster-vs-recipient numbers, positive/negative effects, icons and names. Review captures; rerun only affected tests after changes.
- [x] Inline final diff review; install final same-name tested active/fixed archives, preserve ignored backups and unrelated entries. Update raw class references, player notes and handbook. Build/render/review PDF, verify TOC/links/hashes and git diff --check. Report observed results and limitations.

Review focus: high-power caps; extra-damage reentrancy and same-tick multi-hit; first-hit charge refresh abuse; partial absorption/overlapping casters; stun/freeze escape without bypassing ordinary roll cooldowns; native tooltip config/attribute lines; player-vs-mob attribution and anti-undead effects not increasing healing.

Completed:77 fresh-config +77 stale-config native checks;26 actual-client checks. All seven installed archive hashes match those tested;5,700 unrelated archive entries remain byte-identical. The79-page handbook was rendered/reviewed, including reading-resolution pages17/19, and all51 TOC destinations match. No workers, personal installation edits, commit or push. See docs/patches/paladin-revamp for evidence.
