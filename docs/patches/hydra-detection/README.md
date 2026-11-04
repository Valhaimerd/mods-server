# Ancient Hydra detection and firing range

4 November 2026. Small engine-only overlay over the installed class-balance phases 4-6. Supersedes their Ancient Hydra target-selection/movement description; other class changes remain intact.

- Detect eligible visible enemies within **20 blocks**, excluding party allies, protected pets, vanished targets and spectators through the existing targeting rules.
- Prioritize enemies inside the existing firing volume; choose the closest among them. If none can be fired upon, pursue the closest detected enemy at the existing approximately **one block per second**.
- Stop walking and breathe when in firing range. The breath retains its **12-block body/mouth limits**, existing cone, damage and line-of-sight rules.
- Stop the breath/channel presentation when the selected target leaves firing range; resume pursuit. No fire or damage is delivered merely because an enemy is detected.
- Ordinary Hydras retain their 12-block detection, stationary behavior and original action path. Ground-pulse scaling, summon size, boundaries and movement-control handling remain.

Only `SummonRules.class` and `MageSummons.class` change in the current Spell Engine JAR. Same active filename; matching client/server files and restart required. Original active/fixed copies are backed up before installation.

## Focused verification

The installed baseline failed seven behavior checks, reproducing the old twelve-block acquisition and premature/continuous channel behavior. The replacement is tested in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 server. See `candidate-result.txt`, `candidate-checks.txt`, `artifact.json` and `verification.json` for exact results and hashes. The fixture exercises actual summon creation, target selection, movement, native breath damage and channel state, including 20-block boundaries, shooting priority, line of sight and unchanged ordinary Hydras.

```powershell
python docs/patches/hydra-detection/build.py
python docs/patches/hydra-detection/run_probe.py
```

No handbook/PDF rebuild, screenshot review, Git push, personal world or separate desktop-server modification is part of this small follow-up. `AGENTS.md` now records that routine existing-mod balance/AI fixes use focused patch notes instead.
