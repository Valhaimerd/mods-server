# Penance specialization buff

4 November 2026. Hysteria grants **1.5% attack speed, draw speed and spell haste per stack**, ten stacks / **15% maximum**. Chastise makes enemies **take 1.5% more damage per stack**, ten stacks / **15% maximum**; it does not increase their outgoing attacks.

Both retain four-second refreshed durations, existing explosion eligibility, remote caster support, stack limits and absorption accounting. Penance damage remains 40% P / Improved 50% P, with its existing 8-12s proportional cooldown.

Only PriestRules' four specialization attribute modifiers and the two concise node descriptions change. Current Priest/Ward, Holy Wand, Purelight, Hydra and Adventurer fixes are preserved. Same-name Spell Engine and Skill Tree replacements require matching client/server files and restart. No handbook rebuild or Git push.

Reproduce with build.py and run_client.py in this folder. Tests use isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25. Runtime reports and exact hashes are stored alongside this file.

Installed the exact tested Spell Engine and Skill Tree replacements in active/fixed packs after **41 connected runtime checks and 19 artifact/install checks**. Tests cover 1/10 stacks, real Penance applications, capped refreshes, actual 15% damage amplification and modifier cleanup. Originals are retained in `retired packs/penance-specializations-2026-11-04/{active,fixed}`.
