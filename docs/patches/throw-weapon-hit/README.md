# Throw Weapon recast and Strong Throw — 24 October 2026

Strong Throw now pierces once, hitting at most **two enemies total**. It still steers toward a second unhit enemy, preserves its total flight budget, and does not bounce or ricochet.

The quick-recast problem was reproduced in an isolated server: with 10 melee power, a full first throw dealt 8 HP, but the second throw during the skill's Weakness dealt only 0.8 HP. This is a launch-power snapshot problem, not distance falloff. The native spell-power lookup clamps the reduced power to its minimum.

When the exact Weakness IV instance tracked from this skill is still active, launch briefly excludes its attribute modifier while capturing a fresh projectile power snapshot, then restores the modifier in `finally`. The effect itself is never removed, and ordinary melee remains weakened. The corrected immutable context is installed on the projectile with a narrow accessor. Critical statistics and charge/range remain unchanged. Untracked Weakness is not excluded. Tracking is transient and does not reconstruct ownership of a preexisting Weakness effect after a server restart.

Only Skill Tree's Strong Throw resource/description and Spell Engine's Warrior rule class/accessor are replaced. Throw Net, Rogue skills, party/pet checks, defenses and the prior Warrior ultimate revision remain unchanged. Both client and server need the matching same-name JARs and a restart. Personal installations/worlds are untouched; no commit or push requested.

`artifacts.json` records input/output hashes and exact changed entries. Build inputs are preserved in ignored `tmp/throw-weapon-hit/before`; originals are retired locally after passing verification. `run_probe.py --baseline` reproduces the recast failure. `run_probe.py` exercises the candidates, including repeated casts at several distances, actual projectile ticks, Weakness retention, the two-target cap, earlier Warrior behavior, shields and party/pet protection. The old Rogue tier-II damage probe is omitted because it predates the separate Rogue rebalance; unrelated archive entries are compared byte-for-byte.

```powershell
python docs/patches/throw-weapon-hit/build.py
python docs/patches/throw-weapon-hit/run_probe.py
python docs/patches/throw-weapon-hit/verify.py
```

See `runtime-results.txt` for final observed checks. Server tests do not establish client animation appearance.
