# Protection enchantments: 1% per level per piece

Requested 30 October 2026. Protection, Projectile Protection, Fire Protection, Blast Protection and Magic Protection each grant **1% reduction per level per enchanted piece** in the native enchantment-protection layer. Level IV grants 4% per piece; four level-IV pieces contribute 16% for each applicable enchantment. General Protection adds to matching specialized protection: a full set carrying all five normally gives 16% against ordinary damage, or 32% against a source matching one specialization. Sources matching multiple specialization tags can receive additional contributions, under the existing layer cap.

The implementation uses **0.25 native protection points per level**, because each point represents 4% reduction. Fractional protection is retained through the native helper and damage calculation; no new global damage multiplier or combat hook is added.

The existing five-enchantment coexistence policy is preserved: no compatibility tags, enchantment-support tags or optional compatibility modules are edited. Native non-protection components, including Fire Protection's burning-time modifier and Blast Protection's knockback resistance, remain as configured. Feather Falling is outside this change.

Two same-filename data-only repacks: Penchant receives four vanilla enchantment overrides; Spell Power receives the Magic Protection coefficient change. Existing Java classes, Penchant progression/Unbreaking X, retired-enchantment rules and unrelated entries are preserved.

Build with `python docs/patches/protection-one-percent/build.py`. The input archives are pinned under `tmp/protection-one-percent/before`; `artifacts.json` records hashes and exact entry changes. `run_server.py --baseline` tests the old values and `run_server.py` tests the candidate using native enchantment-protection evaluation, rather than just inspecting the JSON. Its isolated test-only mod is never shipped.

## Verified installation

The old pack failed45 of49 native checks. The final candidates pass **49/49**: levels I-IV of every protection, actual protection-layer damage, all five retained on each armor slot, and full-set general/specialized totals. The native test uses the current Universal Enchants and Penchant modules, plus the installed Iron Wolf Armor tag provider and its dependencies. All Java classes and compatibility tags are unchanged. Both tested repacks are installed in `mods/` and the fixed-pack folder, with originals backed up. No GitHub push requested.
