# Slice Evasion status icon

The registered `skill_tree_rpgs:slice_evasion` effect had no corresponding status texture, causing the missing-texture square. Its icon now copies the installed Rogues Slice & Dice status artwork byte-for-byte to `assets/skill_tree_rpgs/textures/mob_effect/slice_evasion.png` inside the current Skill Tree JAR.

Only this PNG is added. Every existing archive entry is unchanged, including the latest six-block Rain scatter changes. Archive CRC, exact entry comparison, PNG signature and identical active/fixed copies were checked. The source image was visually inspected; no live client screenshot was taken. Reload resources or restart Minecraft to refresh the icon.

Run `python docs/patches/slice-evasion-icon/build.py` to reproduce from the ignored snapshot in `tmp/slice-evasion-icon/`. `artifacts.json` records hashes and provenance. The previous JAR is backed up under ignored `mod fixing/retired packs/slice-evasion-icon-2026-10-23/`; the replacement keeps its existing active filename. No gameplay or handbook mechanics change. Nothing was committed or pushed.
