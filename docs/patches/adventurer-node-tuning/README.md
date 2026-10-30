# Adventurer final-node tuning ? 30 October 2026

- **Bulwark:** resistance increases from 2% to 5%. Retains 4% ranged resistance, +2 armor and 20% knockback resistance.
- **Martial Prowess:** removes the 10% critical-damage reward. Retains 6% melee damage, 5% attack speed and 10% physical critical chance.
- **Hunter:** removes the 5% movement-speed reward. Retains 6% ranged damage, 5% draw speed and 25% bow/crossbow projectile speed.

One data resource changes in the existing Adventurer Skills JAR: `definitions.json`. Actual rewards and node descriptions are edited together. Node IDs, connections, other rewards and every other archive entry remain unchanged.

`build.py` creates a candidate under `tmp/adventurer-node-tuning/built`. `verify.py --baseline` fails all six new expectations; `verify.py` validates the candidate. `run_server.py` exercises real SkillsAPI node unlocking and refund cleanup on an isolated NeoForge server. Final installation uses the original filename; hashes are recorded in `artifacts.json`.

Installed and verified: six candidate resource checks and six native unlock/refund checks pass. Bulwark is tested with Pufferfish's actual DynamicModification API (its raw attribute is intentionally NaN). The tested candidate matches both active/fixed copies. Handbook page20 was rebuilt and reviewed; all51 contents links/page numbers match. No commit or push requested.
