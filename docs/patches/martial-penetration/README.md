# Martial Prowess: armor penetration

Replace the node's +10% physical critical chance with **+10% armor penetration**, using `puffish_attributes:armor_shred` and the pack's established `multiply_total` reward. Retain +6% melee damage and +5% attack speed. The earlier removal of critical damage remains in effect.

Only Martial Prowess's reward and description in Adventurer Skills `definitions.json` change. The previous Bulwark/Hunter changes, ordinary nodes, IDs, tree connections and all other archive entries remain intact. Candidates are built under `tmp/martial-penetration` and repacked with the existing filename.

`run_server.py --baseline` reproduces the old critical bonus; `run_server.py` verifies real node unlocking, the native dynamic armor-penetration modifier, absence of both critical bonuses, and removal of penetration on refund.

Installed: four native checks pass; active and fixed-pack hashes match the runtime-tested archive. Original backed up; no commit/push requested.
