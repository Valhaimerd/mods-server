# Purgatory continuous pull implementation plan

> Execute inline with the executing-plans workflow. User approved continuous pull and immediate implementation after clarification.

**Goal:** Improve Purgatory's escalating pull and underground enclosure, correct node range UI, and raise Ardent Defender healing to 30% maximum HP.

**Architecture:** Overlay the currently installed archives, preserving the pending Ward tuning. Keep native channel/stage/pulse/end logic; replace the old half-second pull impulses with a bounded per-tick acceleration. Reuse the existing boundary and orange renderer, expanding its vertical interval. No new dependency or persistent character attribute.

**Tech stack:** Java 25, NeoForge 26.2, guarded ASM overlays, native spell JSON.

## Approved design

- Damage pulses remain every 0.5s; opening yank stays 1.6 blocks/tick before knockback resistance. Continuous pull begins after the opening impulse, with stage transitions at 10/20s.
- Phase I allows ordinary sprinting away. Phase II defeats unmodified sprinting but allows speed bonuses to matter. Phase III is substantially stronger, capped below the opening yank.
- Initial implementation tuning: inward acceleration 0.06/0.18/0.50 blocks per tick, inward speed ceilings 0.35/0.75/1.35; retain knockback resistance. Validate against native client locomotion and adjust if observations disagree. Preserve tangent movement and avoid overshooting the caster.
- Cache nearby candidates for 4 ticks per active caster; recheck eligibility/range each tick. Clear cache on every channel end. Preserve CC rules, true costs, allies, damage radii and cooldowns.
- Enclosure remains radius 12/14, now from caster Y-5 through Y+5; open above and below, no terrain edits. Expand collision and rendering together.
- Hide generic range rows on Purgatory modifier-only nodes; use explicit 7/14-block improved-node description for its different areas. Never display the default 50-block placeholder.
- Ardent Defender heals 30% maximum HP; preserve trigger, defenses, cooldown and non-cancellation of the triggering hit.

## Work

- [x] Snapshot current inputs; reproduce incorrect ranges/heal and wall depth in focused checks.
- [x] Implement narrow data/class overlays and continuous pull; verify eligible targets, staging, caps, native movement and cleanup.
- [x] Install same-filename replacements into repository mods/fixed packs with ignored backups. Preserve Ward changes and all other archive entries.
- [x] Synchronize player descriptions/reference/handbook, inspect changed PDF pages and record tested hashes. No automatic commit/push or personal/live installation.

Completed in repository on 2 November 2026. Native sprint-contest observations passed all three phases. See [completion and limits](../../patches/purgatory-continuous-pull/completion.json). No push or personal/live installation.
