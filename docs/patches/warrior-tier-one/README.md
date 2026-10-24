# Warrior tier I and shield rework

Minecraft 26.2 / NeoForge 26.2.0.90, 23 October 2026. Same-filename overlays for Rogues & Warriors 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5. Install matching versions on both sides. This revision does not push or commit anything.

The current pack also includes [Warrior tier II and scaling](../warrior-tier-two/README.md), which supersedes the net cooldown, Vitality armor and second-tier values below. This document retains the first revision for recovery.

## Player behavior

See [the paired Warrior reference](../warrior-reference/readable.md) and the generated local `mod fixing/Warrior skills - raw attributes and nodes.md` for full tables.

- Throw Weapon: 5s cooldown, 0.5s charge, 80% full / 40% half-charge melee damage, 12–24 blocks, speed 1 and 2-degree steering. Slowness I lasts 1s. Weakness IV lasts 2s after release, without weakening the already captured projectile power. Improved deals 90%; Head Throw deals 100% and universally stuns for 0.25s. Strong Throw pierces twice for three enemy hits and reacquires unhit hostile targets, without ricochet or block bouncing.
- Throw Net: 5s cooldown, 0.5s charge, 15% full / 7.5% half-charge damage, 12–24 blocks and 2-degree steering. Eligible enemies are rooted for 2s and dragged in collision-checked steps toward a point 2 blocks in front of the caster. Maximum-HP eligibility stays `100 + 2M`; higher-HP enemies still take damage. Improved actually reaches 30 blocks. Bigger Net affects a 5-block impact radius. Multiple Nets reduces cooldown to 3s and root to 1.5s.
- Killing Spree: melee/active melee-skill hits add 2% base attack damage and -2% base incoming-damage modifier, capped at five stacks for 8s, refreshed. Base interval 0.25s.
- Endured Vitality: direct incoming attacks add 1 flat armor and 2 maximum HP per stack, capped at ten stacks for 8s, refreshed. Only newly added maximum HP is healed. Attributed damage over time, environment and spell clouds do not trigger it. Base interval 0.25s.
- Every Warrior active cast and active/passive cooldown explicitly enables haste. Last Stand and Second Wind retain health-based power but now use physical-melee haste for their timing; the native health school otherwise ignores spell haste.
- Activated shields reduce frontal damage by 80%, separately from armor, Resistance and enchantment defenses, before absorption hearts. Native activation delay and durability remain. Rear and source-less environmental damage receive no shield reduction. Axe/component disabling is cancelled. Sonic booms and piercing arrows use the same frontal multiplier. NeoForge shield-block callbacks are retained through the existing Spell Engine bridge.

Party allies and owned pets retain the existing shared targeting/final-impact protection. The net bypasses type/boss immunity only after passing its HP gate. Other class balances and weapon trees remain untouched.

## Implementation and rebuild

`build.py` reads the exact three pre-revision archives in ignored `tmp/warrior-tier-one/before/`, verifies `inputs.json`, compiles Java 25 bytecode and writes candidates under `tmp/warrior-tier-one/built/`. `artifacts.json` records every changed/added entry and final hash. Keep those pinned local inputs for recovery; do not layer this build over already patched outputs or replace newer unrelated revisions with these inputs.

WarriorRules wraps existing Rogue/party helpers. Narrow ASM hooks preserve the earlier magic, Rogue, Archer, rain and enchantment fixes. Three Mixins handle shield interception, component disabling and physical net rooting. Registry spell objects are never modified during combat. Revised first-passive registrations override stale generated effect configuration. Existing IDs and saved node choices are preserved; displayed names change.

The normal full-block path is bypassed while a blocking item is activated, so the hit reaches native armor and Resistance. At damage Pre, frontal eligibility applies the independent 80% layer and publishes a shield-block event. This is damage reduction, not full immunity. Attacks without an originating position cannot be classified as frontal. Durability can still break a shield. Direct health assignment by an unrelated mod is outside Minecraft's damage/blocking pipeline.

## Verification

Commands from repository root:

```powershell
python docs/patches/warrior-tier-one/build.py
python docs/patches/warrior-tier-one/run_probe.py
python docs/patches/warrior-tier-one/verify.py
python docs/patches/warrior-reference/export.py
```

The isolated dedicated server passed **625 measurements**: charge damage before Weakness, upgraded damage and high-HP stun, three-target piercing/reacquisition, actual improved net travel, gradual pull, preserved HP gate, Bigger Net radius and pet safety, shorter nets, live-target Killing Spree, capped Vitality healing/removal, attributed DOT exclusions, shield delay/front/rear/axe/sonic/piercing behavior, multiplicative defense, all-tier haste, existing Rogue tier-II checks and the Better Party regression suite. It used `-Xverify:all`; test classes are never shipped.

These are scripted server-side checks, not a full-pack visual/mouse-input or live multiplayer playtest. Personal Minecraft profiles, worlds and the separate desktop server were not modified. Candidate archives replace the three matching active filenames and their `1 fixed packs` copies only after tests pass; previous active/fixed copies are retained under ignored `retired packs/warrior-tier-one-2026-10-23/`.

Latest installed overlay: [Warrior tier III](../warrior-tier-three/README.md) supersedes Momentum and the third-tier skills/passives. Historical values below describe this revision's own pinned inputs and output.
