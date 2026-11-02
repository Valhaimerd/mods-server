# Optimizer coordination design

1 November 2026. The user subsequently requested [repository-only native presets](presets/README.md), which are now prepared. No installed mods or runtime settings changed.

## Recommendation

Unify deployment and configuration policy while preserving the separate optimizer mods. Use a documented shared-PC profile with distinct client and server settings. Combining their classes into one JAR would not give two Java processes a shared memory pool or CPU scheduler, and would complicate updates and compatibility testing. Some installed projects also restrict redistribution; merging is not an assumed permission.

Compared with leaving every automatic setting independent, coordinated worker and distance budgets can address contention. A custom runtime coordinator is premature until profiling identifies a gap that the existing settings cannot handle. Do not add another per-tick scanner merely to manage optimizers.

## Subsystem ownership

| Work | Owner / baseline | Coordination rule |
| --- | --- | --- |
| Client terrain rendering and mesh workers | Sodium | Let Sodium own this path; Vectors already defers its native path when Sodium/Iris is present. |
| Supplemental client filtering | Vectors; More Culling for its block/model culling | Check actual compatibility gates and visual correctness. Do not assume every enabled flag runs or that every culler duplicates another. |
| Immediate drawing | ImmediatelyFast, candidate only | Benchmark separately with RPG HUDs, transparency, glints and particles. |
| Client entity occlusion | Entity Culling, conditional candidate | Test worn banners, invisible players, summons and effects extending beyond entity bounds. It cannot reduce dedicated-server AI work. |
| Idle client use | Existing FPS cap; optional Dynamic FPS | Retain a foreground cap. If added, give Dynamic FPS responsibility for idle/background policy. |
| Memory | FerriteCore; ModernFix-mVUS candidate | Evaluate ModernFix separately with defaults; do not enable every experimental option. |
| Simulation implementation | Existing vanilla/mod logic; Lithium candidate | Test Lithium defaults first. Its embedded configuration describes overrides, not a list of flags to enable wholesale. |
| Server load policy | ServerCore | Keep dynamic distances/mob caps and entity activation disabled in the initial baseline. Introduce behavior-changing controls only with RPG checks. |
| Lighting | ScalableLux | Keep one lighting rewrite; budget its workers alongside client chunk building. |
| Chunk writes/unloads | Smooth Chunk Save | Retain initially; profile persistence workloads before changing save cadence. |
| World/player metadata writes | Fast Async World Save | Different installed mixin targets from chunk saving; retain initially rather than declare an unproven conflict. |
| Autosave timing | ServerCore configuration, alongside chunk-save settings | Inspect the actual host. Matching 300-second local settings are a profiling lead, not proof of simultaneous stalls. |
| XP and dropped items | Clumps and Leaky respectively | Keep their different roles; changing cleanup thresholds changes gameplay. |
| Measurement | spark, first candidate | Capture tick spikes, GC and memory before and after each change. |

The save-layer distinction agrees with the publisher descriptions of [Fast Async World Save](https://www.curseforge.com/minecraft/mc-mods/fast-async-world-save-forge-fabric) and [Smooth Chunk Save](https://www.curseforge.com/minecraft/mc-mods/smooth-chunk-save), and with the installed mixin manifests. [ServerCore](https://github.com/Wesley1808/ServerCore) distinguishes optimizations from optional behavior-changing controls. [ScalableLux](https://github.com/RelativityMC/ScalableLux) supplies the lighting rewrite and optional parallel updates.

## Installed Vectors: verified gates

Inspected archive: `mod store/vectors-neoforge-26.2-1.5.0-alpha.4.jar`, SHA-256 `843e66c8aa67e1aa1802475fa2f45c82510309bd88241857a8bf8d156c86fe76`.

- `NativeTerrainMixinPlugin` excludes native terrain queue/upload/compiler hooks with Sodium or Iris present. `ChunkWorkers` does not create its own pool when requested workers are zero; zero is not automatic sizing in this build.
- `Compatibility.shaders()` checks whether Iris/Oculus is installed, not whether a shader pack is currently enabled. The inspected entity-distance, block-entity and weather-density hooks skip their filtering when that check is true.
- `SodiumSupport.sprites()` returns false in this local port. Configuration flags alone cannot establish that sprite integration is active.
- `ParticleMixin` filters only providers whose class names start with `net.minecraft.`; custom providers bypass that specific filter. This does not guarantee that every RPG effect is unaffected by other rendering stages.

Evidence came from the installed archive's decompiled classes under ignored `tmp/vectors-restore/source/`. These findings correct the earlier worker interpretation; they do not establish measured gains or full-pack runtime compatibility.

## Shared-PC rollout

1. Record the actual host's exact CPU, Java version, client/server heap limits, distances and configurations. The sampled personal profile is not the multiplayer server. With 12 GB total RAM, account for Windows, both live heaps and native/graphics memory; do not assign nearly all physical RAM to heap maxima.
2. Establish a repeatable baseline: server without host client, client at menu, host joined at the same base, then the reported burst. Compare long-tick counts, p95/p99 tick time, GC pauses, available RAM/paging, disk activity and client frame times.
3. Validate spark and then Lithium independently in disposable instances. Test combat, party checks, summons, shields, building queues and persistence. Preserve rollback copies.
4. If contention is demonstrated, compare bounded Sodium/ScalableLux worker settings with automatic settings. Choose limits from the actual CPU and measurements; no exact core allocation is justified by “14th-gen i5” alone. Do not add a Vectors pool to a Sodium-owned path.
5. Evaluate memory/loading and client candidates one at a time only where the baseline identifies useful work. Keep a change only if it improves the relevant measurement without gameplay or visual regressions.
6. The repository presets now provide explicit client/server scope with manual backup, merge and rollback instructions. Automatic deployment remains separate: the installer has an existing Vectors-specific merge but no general optimizer-profile deployment mechanism. Put any future client-only mod additions in `mod store/` and server-compatible components in `mods/` after metadata checks.

Acceptance: fewer severe tick spikes with acceptable frame pacing, no sustained paging, working RPG visuals/AI and clean saves/restarts. No FPS/TPS improvement or root cause is established by this documentation review. The evidence and candidate versions remain in the adjacent audit files.
