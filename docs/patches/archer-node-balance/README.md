# Archer ordinary node balance — 25 October 2026

Adventurer Skills ordinary ranged damage changes from 3% to **2%**. Its ordinary 5% armor-penetration node becomes **3% movement speed**, retaining its original multiplication operation and node ID. Hunter loses its **10% armor penetration** without receiving another movement reward: its existing 6% ranged damage, 5% movement speed, 5% draw speed and 25% bow/crossbow projectile speed remain.

RPG Skills Archer root/boost and conditional bow damage rewards change from 3% to **2%**, with matching descriptions, in both class and weapon definitions. No RPG ordinary penetration reward exists in the installed input. Node IDs, connections, existing unlocks, spell descriptions, combat code and all unrelated rewards are preserved.

Only three JSON resources change across the two same-name active/fixed archives: Adventurer Skills' definitions and RPG Skill Tree's class/weapon definitions. `artifacts.json` records input/output hashes. `build.py` writes candidates without installing; `verify.py` checks candidate hashes, ZIP inventories/CRC, node values and unchanged unrelated entries/rewards. **573 archive and node checks passed.** This is a data-only edit; no fresh in-game or dedicated-server gameplay test is claimed.

Original active/fixed copies remain under ignored `retired packs/archer-node-balance-20261025`. Sync/restart client and server to receive the new definitions. Existing learned nodes keep their IDs. Personal worlds and the external server were not modified; no commit or push requested.

```powershell
python docs/patches/archer-node-balance/build.py
python docs/patches/archer-node-balance/verify.py
```
