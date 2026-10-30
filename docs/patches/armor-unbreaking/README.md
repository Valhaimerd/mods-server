# Armor Unbreaking: 8% per level

Armor now ignores each potential point of durability loss with probability **8% ? Unbreaking level**: I?X gives 8/16/24/32/40/48/56/64/72/80%. At X, armor loses 20% of potential durability on average, lasting about five times as long from Unbreaking alone.

Only the armor branch changes. Non-armor Unbreaking retains L/(L+1) avoidance. The max level remains X; progression, costs and saved enchantment levels are preserved. Both the base definition and optional Durability Rework definition are updated, preventing module selection from restoring the old formula. `unbreaking.json` exposes the installed base definition for review.

No RPG node bonuses are changed. The inspected Adventurer utility nodes total 16% tool-use durability preservation; they are not evidence of a 14-point additive armor bonus. An independent 14% preservation roll would leave 17.2% durability loss at Unbreaking X (20% ? 86%), rather than 6%. No combined 6% claim is made without the exact armor-path mechanic.

Build: `python docs/patches/armor-unbreaking/build.py`. Native checks: `run_server.py`, and `run_server.py --module` with the bundled Durability Rework copied into the isolated world as a datapack. They exercise the real `EnchantmentHelper.processDurabilityChange` method over 200,000 durability points per case, with a 0.8 percentage-point statistical tolerance.

Installed verification: **18/18 native checks pass with Durability Rework disabled, and 18/18 with the module enabled**. Distinct maximum enchanting-cost values confirm which definition loaded. The previous build fails the armor curve checks. Active and fixed-pack JARs match the tested hash in `artifacts.json`; handbook regenerated and the edited Penchant page visually reviewed.
