# Sophisticated Building: power-independent reach

31 October 2026. Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25+. Same-filename overlay for `sophisticatedbuilding-neoforge-26.2-5.1.0.jar`.

The previous patch removed only the axis-length cap. `ServerBlockPlacer.validateRequest` still rejected a starting point beyond `PowerLevel.getPlacementReach`, producing **Out of reach for your power level**. The native targeting ray and shape criteria also used the power-tier reach. A 512-block batch allowance did not imply 512-block targeting distance. The personal client's JAR matched the repository; deleting its config would not remove these checks.

The four shared getters in `AttachmentHandler` and `PowerLevel` now return **1,000-block placement reach for every tier and Creative**, with the native six-block shape-selection allowance retained (1,006). Both current/next-tier queries and no-attachment fallbacks use the same values. Existing `reachLevel0/1/2/3` values are superseded, so old configs need no deletion. The native radial tooltip uses these getters and displays the current reach.

1,000 is the mod's supported reach-config maximum. A finite value avoids casting arbitrarily long client rays and avoids the integer-square overflow that an unlimited sentinel would cause in native targeting/shape checks. The existing unlimited-axis sentinel remains restricted to the separate axis policy.

Survival batch limits stay 128/256/512/2,048, Creative 10,000. Material consumption, mirror radius, loaded-world behavior, server permissions, block eligibility, undo/redo and PvP combat guards are unchanged. This does not extend ordinary vanilla interaction reach. Only two existing classes change; all 535 other archive entries, including `ServerBlockPlacer` and the prior combat guards, remain byte-identical.

## Build and verification

```powershell
python docs/patches/building-reach/run_client.py --baseline
python docs/patches/building-reach/build.py
python docs/patches/building-reach/run_client.py
python docs/patches/building-reach/verify.py
```

The runner creates a disposable client and copied test world. The baseline fails the expanded reach assertion. Candidate coverage includes native client targeting at 64 blocks; previews on positive/negative X/Y/Z; all power tiers and next-tier queries; old low reach settings; batch limits; rejection beyond the new targeting bound; actual Survival placement from 64 blocks away and exact material use. It uses a real connected player and integrated server; it does not modify the user's profile, multiplayer server, configs or worlds.

All **41 checks passed** on the exact installed candidate. A 32-block Survival build from 64 blocks away consumed exactly 32 materials. `runtime-tested-sha256.txt` pins that runtime archive; `artifacts.json` records its predecessor and two changed entries.

Install the same updated JAR on **both client and server**, then restart them. A client-only update cannot change an older server's validation. No config deletion is needed. The active filename stays the same; predecessor active/fixed bytes are backed up under ignored `mod fixing/retired packs/building-reach-2026-10-31/`.
