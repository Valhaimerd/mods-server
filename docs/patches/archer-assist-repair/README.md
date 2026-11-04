# Archer-only launch correction

4 November 2026. Replaces ordinary-arrow continuous steering with a small, launch-only near-miss correction. User requested Archer-only assistance capped at two degrees per tick, without aimbot behavior.

## Design

- Only a player with **Archer as their sole allocated RPG class root** qualifies. Other classes, no class, and legacy/malformed saves retaining Archer plus another class fail closed. Weapon specializations and the Adventurer Hunter node do not grant this ability.
- On the arrow's first observed launch tick, select the nearest eligible visible enemy within **24 blocks**, within a **3-degree forward acquisition cone**, and no more than **0.4 blocks outside its predicted ballistic path**. Being near the bow alone is insufficient: the shooter must aim close to the enemy.
- Apply at most **2 degrees on that tick**, preserving speed. That is also the **entire shot's correction budget**. Unlimited two-degree-per-tick turning would still produce a seeker; this deliberately does not chase targets throughout flight.
- Aim at the near edge of the target's hitbox, not its center. Then leave the arrow ballistic. No retargeting, orbiting, acceleration, through-wall acquisition or newly acquired target after the shot has missed. Existing arrows and later class changes cannot gain assistance.
- Eligible enemy players remain valid when PvP permits. Party players/pets, own pets, spectators, dead targets and Vanish-hidden enemies remain excluded by the existing shared policy.
- Applies to ordinary player-owned native arrows, including ordinary class/barrage shots. Spell-projectile Magic Arrows and their specialization behavior remain separate and unchanged.

## Cause and evidence

The old helper checked whether Archer's root was unlocked but did not reject simultaneous stale class roots. A native regression reproduces acceptance of Archer plus Warrior. Normal clean non-Archer saves already passed the old class check; that does not by itself explain every reported in-game instance. The repair adds an explicit sole-class guard without modifying or deleting saved allocations.

The old helper could keep rotating toward the target's center after acquisition. It had a finite total budget, but its sustained target tracking is removed entirely. This patch replaces **one existing class**, `net/spell_engine/internals/ArrowAssist.class`, in the current installed Spell Engine JAR. No class balance, projectile speed, native damage, IDs, configuration or separate magical seekers are changed.

Build/test files and exact hashes live beside this note. Isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 fixtures leave personal worlds and the separate desktop server untouched. No handbook rebuild or Git push.

```powershell
python docs/patches/archer-assist-repair/build.py
python docs/patches/archer-assist-repair/run_probe.py --baseline
python docs/patches/archer-assist-repair/run_probe.py
```

Final result: the original installed helper fails **27 of 65 regression checks**; the candidate passes **65/65 native checks** and **20 artifact/install checks**. Native event-bus tests confirm launch correction and no subsequent tracking for 20 ticks. Tests cover all seven other class roots, no-class and ambiguous stale allocations, speed preservation, nearest target, walls, clear misses, old shots, own/party pets, party players, eligible enemy players and Vanish exclusion. The exact tested same-name Spell Engine replacement is installed in repository active/fixed packs. Originals remain in `retired packs/archer-assist-repair-2026-11-04/{active,fixed}`. Restart client/server with matching JARs. No connected-client visual playtest is claimed; behavior was exercised on the native isolated server.
