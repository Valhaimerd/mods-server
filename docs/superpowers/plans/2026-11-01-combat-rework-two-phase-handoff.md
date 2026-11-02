# Combat Rework — Two Phases and Chat Handoff

**Status:** Phase 1 completed and installed on 2 November 2026. Phase 2 completed and installed on 2 November 2026; see [its completion record](../../patches/combat-phase-two/completion.json). See [the tested completion record](../../patches/combat-phase-one/completion.json). No automatic GitHub push.

**Detailed specifications:**

- [Main combat plan](2026-11-01-echoing-blast-charges.md)
- [Mage offensive damage ledger and enchantment comparison](2026-11-01-mage-offensive-damage-scaling.md)

The split groups changes by implementation dependencies rather than equal bullet counts. Phase 1 establishes the balance and charge/hit behavior; Phase 2 handles spatial mechanics and target eligibility. Each phase must leave playable, tested production JARs and synchronized player documentation.

## Phase 1 — Balance, preparation, charges and Frenzy impacts

**Status:** Completed. Installed same-filename repository and fixed-pack JARs; see the completion record for verification scope.

| Work item | Required result |
| --- | --- |
| Mage offensive damage | Apply the linked rounded ×0.8 ledger to Arcane/Fire/Frost active skills, offensive upgrade effects, weapon spells and owned summons. Exclude separate school passives and healing. Ordinary Explosion **210%**, Echoing **130%** after rounding. |
| Arcane Missiles | **12s** cooldown, non-proportional; **40-block** targeting. |
| Arcane Explosion / Echoing Blast | Base Explosion cooldown **12s**, targeting **40 blocks**; Echoing stores **2 charges**, recovering **one per haste-adjusted 12s**, with no on-hit reset. Include authoritative charge handling, persistence and usable charge/recharge UI. |
| Traps / Phase Shift | Arcane, Flame and Frost Trap plus Phase Shift: **4s** base cooldowns. Arcane Trap applies **Weakness I** instead of II. |
| All three mage Wards | **1 absorption heart per 8 matching school power**, **15s** cooldown; retain 8s duration and absorption safeguards. Read the main plan's rounding recommendation before implementation. |
| Progression nodes | Adventurer melee **3%** per ordinary node; RPG Warrior damage progression **4%** per node; RPG Rogue attack-speed progression **2%** per node. |
| Purge Heal | Targeting **30 blocks**, including upgraded variants; retain separate area radii. |
| Faster preparation | Blessed Strikes **1.5s / five pulses 0.3s apart**; Divine Oath **0.25s** cast; Warrior Throw Weapon / Throw Net approximately **0.3s full charge**, preserving charge curves. The former 0.5s Oath proposal is superseded. |
| Sinful Retaliation | Replace 4s burning with **1 HP true damage every 0.5s for 4s**; preserve mark, vulnerability, readiness and cooldown. Follow the main plan's absorption/source-policy details. |
| Purgatory cooldown | **12s**, haste-affected, starting at channel end. Its containment/pull rework belongs to Phase 2; do not claim it is included here. |
| Frenzy impacts | Frenzy arrows bypass ordinary post-hit damage i-frames, retaining **20/30/40/50%** heat damage, legitimate defenses and other Frenzy mechanics. Ordinary non-Frenzy arrows remain outside this change. |

### Phase 1 completion gate

- [x] Implement only Phase 1 and run meaningful before/after tests, including real impact/cast paths, exact charge/cooldown boundaries, stale saves and damage/absorption protections.
- [x] Rebuild final same-filename replacements and install them in repository `mods/` plus ignored fixed packs; preserve originals in ignored backups. Never ship probes or intermediate artifacts.
- [x] Update concise tooltips, relevant raw references/current patch notes and handbook/PDF where player-facing information changes. Verify and visually review the handbook as required by `AGENTS.md`.
- [x] Record changed filenames, before/after SHA-256 hashes, source/build paths, commands/results, tested runtime scope and unresolved issues in a Phase 1 completion record. Mark items complete only with evidence.
- [x] Update the Phase 2 handoff below with the actual final Phase 1 archive identities. End the implementation batch here, respecting the user's request to reserve the remainder for the next chat.

## Phase 2 — Spatial behavior, target protection and homing

**Status:** Completed and installed on 2 November 2026. See [the spatial patch](../../patches/combat-phase-two/README.md).

| Work item | Required result |
| --- | --- |
| Warrior size / melee reach | Investigate the reported large Warrior with short reach. Trace effective scale, native/Better Combat reach, hitbox targeting, Enrage and accessories. Larger size must increase usable melee reach; smaller size must reduce it; no stale penalty after changes. Do not assume a cause or approved formula/cap. |
| Vanish targeting | Vanished enemies cannot be highlighted, selected or retained by hostile target-lock skills. Clear existing locks and validate stale client requests on the server. Audit homing acquisition; preserve distinction from untargeted damage immunity. |
| Flame Slash homing | Retain free-aim casting; gently seek the nearest eligible detected enemy. **2°/tick** and **8-block detection** are recommendations, not separately confirmed numbers. Preserve travel limits, collision and Phase 1 damage. |
| Archer Barrage homing | Verify every Barrage arrow, including upgraded extra arrows, inherits the allocated ordinary-arrow slight homing assist. Repair only a demonstrated bypass; do not introduce stronger/additive homing. |
| Purgatory containment | Very strong first-pulse pull; visible **orange Priest-Barrier-style boundary**, approximately **5 blocks high**, at **12/14-block outer radius**. Boundary ends with the skill on every cleanup path. Preserve first-phase CC counterplay and the Phase 1 **12s cooldown**. |

### Decisions still identified as recommendations

The main plan separates confirmed requests from proposed details. Do not silently label these as user-approved:

- Exact size-to-reach curve/limits after root-cause investigation.
- Flame Slash's proposed detection radius and exact steering strength.
- Purgatory boundary collision for allies/caster; hostile-only containment is the current recommendation. A wall is not automatically a roof, anti-teleport field or projectile shield.
- Ward whole-heart rounding/no-extra-base-heart interpretation, and Sinful Retaliation's interaction with the existing player-damage policy, remain documented implementation recommendations. Resolve any material conflict when those Phase 1 items are implemented.

### Phase 2 completion gate

- [x] Build from the **final Phase 1 archives**, not an older pinned October baseline that would overwrite the new balance, node data or charge handling.
- [x] Reproduce the reach/targeting defects before patching; verify real collision, target selection, native casts and cleanup. Use isolated worlds and client/server checks where required.
- [x] Rerun the relevant Phase 1 regression tests, particularly new Echoing charges, revised damage, shorter preparation and Frenzy impacts.
- [x] Install final artifacts, synchronize descriptions/references/handbook, record hashes/results and mark the phase complete only after validation. Push only if explicitly requested.

## Existing work — preserve across both phases

There are already unrelated, tested but uncommitted repository changes from earlier requests: the Better Revive dead-login retained-gear fix, Nameless Trinkets tick-overhead repair, documentation/PDF updates and repository-only optimizer presets. They are not pending combat-phase tasks and must not be reverted or rebuilt from an older baseline accidentally. Inspect `git status --short` and their patch records first. No personal/live server was changed by those repository updates.

## Next-chat starting instructions

Read `AGENTS.md`, `README.md`, this phase index and the two linked detailed plans. Check the working tree and the actual current JAR hashes. **Phase 1 is now complete; continue Phase 2 from these exact input archives.** If a later completion record exists, trust its tested hashes/status rather than assuming this original state still applies. Continue the earliest incomplete phase; do not repeat completed work or infer that a drafted plan is already installed.

Suggested resume message after Phase 1 is actually complete:

> Continue Phase 2 from `docs/superpowers/plans/2026-11-01-combat-rework-two-phase-handoff.md`. Preserve the completed Phase 1 changes and the earlier grave/trinket repairs. Read the completion record and current JAR hashes before editing.

## Final Phase 1 inputs for Phase 2

Verified repository and fixed-pack archives, 2 November 2026. Do not overwrite these with an older builder.

| Archive | SHA-256 |
| --- | --- |
| `adventurerskills-EN-1.3-26.2-neoforge-fixed.jar` | `679a4939ad62e2d3130bcfb17928669a8f8e43459347f5fc561ca9f85705c7eb` |
| `crossarms-1.4.jar` | `9a4d15a3e047a8e09a83fc7aac73a001d5693742ec49a5895aa0e1ba81edcdc4` |
| `paladins-neoforge-3.1.2+26.2.jar` | `1a0cf43aafc1e9d67e594e1454ffe5c239fc691ff09f4198f4f808a008f16aa2` |
| `rogues-neoforge-3.1.2+26.2.jar` | `4ad8482a9f9e283a0aca6726c33433dbf5cdf4e6c3be8046dee93fda0ba166ba` |
| `skill_tree-neoforge-1.6.1+26.2.jar` | `9f673596c819a4e4cc110c05602a0cc9e36d89f8ae84067e9024b7c4580d73eb` |
| `spell_engine-neoforge-1.10.5+26.2.jar` | `ab9f7aa4960682038c8c31cbb1caf755f94e439f09b23066f002a07616391ee3` |
| `wizards-neoforge-3.1.2+26.2.jar` | `24fa67c3b563716d31c40d70038c976561a015b1a326b35e5530c4a6c4868a18` |

Verification: 196 dedicated-server checks, 10 integrated-client checks, 116 resource/archive checks. The 80-page handbook has 51 verified printed contents numbers. See the completion record for exact test files and limits.
