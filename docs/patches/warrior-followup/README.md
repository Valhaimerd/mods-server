# Warrior follow-up — 24 October 2026

Same-name overlays for Rogues & Warriors, Skill Tree and Spell Engine. Both client and server need the matching archives, followed by a restart. No commit or GitHub push is part of this task.

- Throw Weapon now scales from **40% to 80%** melee power between earliest release and full charge. Improved uses **50%-90%**; Head Throw uses **60%-100%**. Throw Net uses **7.5%-15%**. The charge curve is normalized from the native earliest release point, with damage clamped at the minimum below that point. Native output scaling is disabled only for these two spells, avoiding a second charge multiplier. Charge-based range and captured pre-Weakness power remain intact.
- Lethality is now solely a modifier selected by the existing melee-healing/self-drain hooks. Its leftover `SPELL_CAST` trigger, damaging spike clouds, launch effects and sounds are removed. Berserker plays `skill_tree_rpgs:rock_spike_impact` once on natural expiry or fatal-hit rescue, never from channel completion or death cleanup.
- Last Stand adds **20% base maximum health per pulse**, up to **100% at five pulses**, and heals the maximum health newly added. Its health bonus expires with Last Stand; Improved extends it to twenty seconds. Damage immunity, regeneration, knockback immunity and the other nodes retain their existing behavior.
- Actual death, death cloning and dead-player ticks clear Berserker, Brutality, Last Stand, Juggernaut and Sentinel together with their tracked state. Cleanup suppresses Berserker's expiry healing/sound. Active ultimate regeneration, speed and brief pulse immunity are cleared too. Fatal-hit rescue still prevents death as designed.
- Nine custom Warrior effect icons reuse the corresponding existing skill artwork. Both English spell-name and tree-title keys are verified as **Strong Throw** and **Head Throw**. Those two keys were already correct in the active repository input, so an older running client must receive the updated JARs and restart to display them. No personal Minecraft installation was edited.

`build.py` snapshots the current inputs under ignored `tmp/warrior-followup/before`, compiles only the two responsible rule classes, and writes candidates under `built`. `artifacts.json` pins input/output hashes and exact changed entries. Do not install an older Warrior builder over this overlay.

The baseline reproduces zero damage at zero charge, where 40% was expected. The isolated NeoForge 26.2.0.90 candidate passes **895 Warrior/shield/party checks**, including six charge points for both throws and all three Throw Weapon variants, pre-Weakness power, five health/healing pulses, death and respawn-copy cleanup, universal control and party/pet protection. The historical Rogue tier-II regression was omitted because its damage expectations predate the independent Rogue rebalance; unrelated Rogue class bytes remain unchanged. Assets and English names are verified statically; this is not an in-game visual/audio playtest.

```powershell
python docs/patches/warrior-followup/build.py
python docs/patches/warrior-followup/run_probe.py
python docs/patches/warrior-followup/verify.py
python docs/patches/warrior-reference/export.py
```

Latest overlay: [Throw Weapon recasts and two-enemy Strong Throw](../throw-weapon-hit/README.md). Its manifest identifies current Skill Tree and Spell Engine archives.
