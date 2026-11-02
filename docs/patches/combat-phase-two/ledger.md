# Combat phase two ledger

Completed, 2 November 2026. Inputs match the final Phase1 hashes. Five tested same-filename replacements are installed in repository mods and fixed packs. No push or live installation.

## Rulings

- Use the existing Better Combat proportional scale rule for native server interaction reach too. No new scale attribute/modifier, cap or persistent state: equipment and buff changes immediately change reach without stale owned modifiers.
- Flame Slash uses the recommended2 degrees/tick and8-block nearest-enemy detection, with visibility/party/Vanish checks and bounded local acquisition every4 ticks. Native speed/range/collision and Phase1 damage remain.
- Purgatory contains hostile living entities in both directions; caster and party allies/pets pass. It has no roof, teleport prevention or projectile shield. This implements the plan's recommended friendly policy under the user's approval to proceed.
- The first pull is a bounded1.6-block/tick impulse before knockback resistance, retaining native collision. Subsequent stage pulls remain unchanged. The boundary reuses the Priest Barrier entity and translucent-panel style on a five-block orange cylinder, without its buffs or defenses. A direct texture and world geometry submission avoid the native atlas/culling path that produced no visible enclosure in the client test.

## Investigation

- BetterCombat's AttackRangeExtensions scales client targeting by `getAttackRangeMultiplierForScale`. Its PlayerEntityRangeMixin returns the unscaled weapon range to vanilla server interaction validation. Large eye-height/weapon-range fixtures will reproduce that mismatch before patching. The original affected player's save/loadout remains unavailable; this is not a claim to have captured that live state.
- ArrowAssist subscribes to all owned AbstractArrow ticks, including Barrage. Verify actual volleys before deciding a gameplay repair is necessary.
- Vanish is the actual `rogues:stealth` status. Selection/retention must be filtered separately from damage eligibility so untargeted area damage still works.


## Completion and review

- Baseline reproduced native reach 3.0 versus expected 1.5 at scale 0.5. Final size fixtures cover 0.5 through 4.0, close targets, growth and modifier removal.
- Remote client entities do not expose the actual Stealth effect map. A dedicated synced Boolean fixes highlight eligibility without confusing ordinary invisibility; server checks still use the live effect.
- Final review found and corrected AREA filtering, inherited Priest projectile interception, and overlap-band collision above/below the wall. Their regressions pass.
- Native client rendering exposed missing geometry; direct world submission, an orange panel texture, and removal of duplicate coplanar faces produce the reviewed enclosure.
- Fresh isolated worlds prevent old failed-fixture entities from contaminating nearest-target checks. Barrage's four launched arrows use the shared assist handler with identical stationary near-miss fixtures; no Barrage-specific steering was added.
- 78 spatial server checks, 196 Phase 1 regressions, 9 client checks and 59 archive/resource checks pass. The 80-page handbook's changed page 20 and contents page were visually reviewed; 51 printed contents destinations and 471 internal links pass.
- Exact installed hashes, preservation hashes and limitations are in completion.json. No personal installation, live world, commit or GitHub push.
