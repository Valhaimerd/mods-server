# Class visual planning audit

29 October2026. Read-only asset investigation for the [remaining-class plan](../../superpowers/plans/2026-10-29-other-class-visuals-and-effect-icons.md). This records the pre-fix pinned baseline. The subsequent [installed pass](../class-visual-retouch/README.md) repairs all three sprites; use its verification for the current pack.

`registered-effects.json` contains109 native registered class/control effects exported by the final Paladin tier-III server fixture. All six current RPG archive hashes matched that fixture before reuse; `registry-baseline.json` records them. The audit intentionally includes Paladin as a shared-code regression baseline, although its visual pass is already installed.

Run `python docs/patches/class-visual-audit/audit.py` from the repository root. It scans assets in both active mod folders, checks the expected mob-effect PNG paths, alpha content, localized names and effect-atlas overrides. Results are in `icon-audit.json`. An archive hash mismatch requires a refreshed native registry export before claiming current coverage.

Three missing conventional sprite files are confirmed. Existing sprites are nonblank. This is an asset diagnosis, not proof that every status is visible in a running client: atlas loading, intentional hidden-status flags, resource-pack overrides, stack counters and synchronized descriptions remain runtime checks in the execution plan. Do not call a deliberately hidden internal state a missing-icon bug without inspecting its application flags.

| Effect | Planned existing artwork | Why |
|---|---|---|
| `rogues:improved_stealth` |`assets/rogues/textures/spell/vanish.png` |Applied by Improved Vanish; reuse its own skill artwork |
| `rogues:tracking_disruption` |`assets/rogues/textures/spell/shadow_step.png` |Shadowstep clears nearby mob targets and prevents reacquisition briefly; this is not Root/Stun |
| `skill_tree_rpgs:cheat_death_evasion` |`assets/skill_tree_rpgs/textures/mob_effect/cheat_death.png` |The six-second evasion tail belongs to Cheat Death; existing parent effect art is available |

Execution should copy compatible RGBA art to the exact missing effect paths, preserving IDs and gameplay. Check native icon size/readability rather than assuming a spell texture will look good when shrunk. Existing official Stun/Root/Frozen artwork remains unchanged.
