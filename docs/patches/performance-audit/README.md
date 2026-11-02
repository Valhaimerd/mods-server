# Shared-PC client/server performance audit

1 November 2026. Pack: Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25+. Host reported: **12 GB RAM, Intel Core i5 14th generation**. Exact processor, client/server heap allocations and live-server configuration remain unknown.

## Conclusion and evidence limits

The repository already has a substantial optimization stack. The first useful additions to evaluate are **spark for evidence** and **Lithium for server logic**. ModernFix-mVUS is a subsequent memory/loading candidate; client rendering additions should be evaluated individually. On a 12 GB shared machine, simultaneous client/server heap growth, native graphics memory, operating-system memory and worker contention are plausible causes of short pauses. That is a hypothesis, not a measured diagnosis. Lower CPU percentage alone is not the goal: evaluate server tick time, pause spikes and memory while retaining acceptable client responsiveness.

This audit inspects installed archive metadata, local profile configuration, publisher pages, API release metadata and actual optimization-pack manifests. Candidate JARs and reference packs were downloaded only into ignored `tmp/performance-audit/` and verified against publisher SHA-512 hashes before inspection. **No new mod was installed; no game, server, world or configuration was changed. No integration benchmark was run.** JARs remain 307 main + 23 client extras; gameplay documentation/PDF needs no update for this research-only pass.

Local configuration observations describe the inspected personal client profile and its integrated-server defaults. They do **not** establish what the multiplayer host's dedicated server runs. The previously inspected desktop server was obsolete; do not assume it represents the remote endpoint.

## Existing stack

| Installed component | Version | Inspected purpose / decision |
| --- | --- | --- |
| Sodium | 0.9.2+mc26.2 | Client rendering; retain as the rendering base. |
| More Culling | 1.8.1 | Client culling; retain initially and compare visibility with any new culler. |
| Vectors | 1.5.0-alpha.4 local NeoForge port | Client performance preset already active. It is an alpha port, not proof of server tick optimization. |
| FerriteCore | 9.0.0 | Memory reduction; retain on supported sides. |
| ServerCore | 1.5.19+26.2 | Server optimizations and optional load/activation control. Inspect the actual host's settings before tuning. |
| ScalableLux | 0.2.1+neoforge.729d9d3 | Lighting rewrite with automatic worker configuration. Budget workers against both Java processes. |
| Fast Async World Save | 2.6 | Inspected mixins target world/player metadata storage. |
| Smooth Chunk Save | 4.2 | Inspected mixins target chunk saving/unloading and entity storage. Different layers from Fast Async World Save; their coexistence is not itself proof of a conflict. |
| Structure Layout Optimizer | 1.1.4+26.1-neoforge | Jigsaw generation work; less directly relevant to recurring stationary-player spikes. Metadata declares the broad supported range; filename alone is not a rejection. |
| Clumps | 26.2.1 | XP-orb consolidation. Retain initially. |
| Leaky | 3.4 | Dropped-item performance and cluster detection/cleanup. This is not a general Java heap-leak repair mod. |
| Chunky | 1.5.4 | Pregeneration tool, useful for exploration-related work. Do not benchmark steady gameplay while a pregeneration task runs. |
| PacketFixer | 3.3.6 | Packet compatibility fixes. Its presence does not diagnose or solve CPU-bound ticks. |
| JEIOptimizer | 1.1.2-30.30 | Parallel ingredient-index construction, relevant to client join/load bursts rather than ordinary server simulation. |

`installed-metadata.json` records exact filenames, mod IDs, dependency ranges and declared entry metadata. Every listed mod was inspected beyond its filename.

## Local configuration findings

- ServerCore has `dynamic.enabled: false` and `activation-range.enabled: false`. Its reduced synchronous loading and ticking-chunk cache are enabled. These observations apply to the local profile only.
- Enabling all ServerCore defaults wholesale would change behavior: its dynamic template includes mob-cap reductions, and its default activation range is only 16 blocks. The pack's summons, ranged AI, bosses and farms need consideration. Start with a documented distance policy; explicitly test/exclude relevant RPG entities before any activation throttling.
- Smooth Chunk Save currently delays chunk saves by 300 seconds, limits unload processing to 20 chunks/tick and disables protochunk saving. ServerCore's autosave interval is also 300 seconds. The timing makes save activity worth checking against spikes, but matching intervals do not prove the cause.
- Leaky's inspected configuration enables item-performance work and has automatic large item-cluster removal thresholds. Do not lower those thresholds casually as a general optimization; that changes item retention.
- Sodium chunk-builder threads are automatic (`0`) and ScalableLux parallelism is automatic (`-1`). Automatic worker choices in two JVMs are not a shared CPU budget. **Correction after inspecting the installed Vectors code:** its `chunkWorkers=0` creates no Vectors worker pool; its native terrain/worker path also defers to Sodium/Iris compatibility gates. Select limits only after identifying the actual host CPU/model and the busy threads.
- The inspected client already caps foreground rendering at 60 FPS, with render distance 8 and simulation distance 6. Confirm the host client uses comparable settings. Multiplayer simulation distance is controlled by the server, not this client option.

## Verified release candidates

The official Modrinth API was filtered by both `26.2` and `neoforge`. The five first entries below were also downloaded to the temporary audit folder, hash-checked and inspected for embedded NeoForge metadata. This verifies identity and advertised compatibility, **not compatibility with every RPG patch**.

| Candidate | Matching release | Placement / role | Priority |
| --- | --- | --- | --- |
| [spark](https://modrinth.com/mod/spark) | 1.10.187 NeoForge | Server first; CPU, tick-time, GC and memory diagnosis. It measures performance rather than fixing it. | First |
| [Lithium](https://modrinth.com/mod/lithium) | 0.25.3, mc26.2 NeoForge | Server logic optimization; optional client installation also covers an integrated server. A remote client copy does not optimize the dedicated server. | First candidate to benchmark |
| [ImmediatelyFast](https://modrinth.com/mod/immediatelyfast) | 1.16.5+26.2 NeoForge | Client draw/rendering work. Test with Sodium, Vectors and RPG overlays. | Second-stage client test |
| [Entity Culling](https://modrinth.com/mod/entityculling) | 1.11.3, mc26.2 NeoForge | Client occlusion/tick culling; adds visibility work to save rendering. Test effects and entities rendered beyond their bounds. | Conditional |
| [Dynamic FPS](https://modrinth.com/mod/dynamic-fps) | 3.11.9, mc26.2 NeoForge | Reduce client use when idle/unfocused. It does not cap active gameplay by default. | Useful for idle/background use |
| [ModernFix-mVUS](https://modrinth.com/mod/modernfix-mvus) | 5.27.21+mc26.2 NeoForge | Separate maintenance fork for versions the original does not support. Potential memory/loading benefit; requires its own metadata/integration review before installation. | Second stage, particularly if memory is the bottleneck |

The original [ModernFix project](https://modrinth.com/mod/modernfix) returned **zero** matching 26.2/NeoForge releases in this audit. That does not mean all ModernFix-derived builds are absent: both reference packs below use **ModernFix-mVUS**, verified from their download project IDs. Do not confuse those projects or blindly substitute an older original build.

Entity Culling's [publisher documents](https://modrinth.com/mod/entityculling) visual issues for client-side magic entities and renderers extending beyond their bounds. Test Bifrost travel effects, worn Battle Banner, summoned creatures and readied/invisible-player rendering before enabling its most aggressive options. It does not reduce the server's entity simulation.

The pack's save and light optimizers already cover multiple subsystems. C2ME/chunk-threading alphas, another light engine, and extra save schedulers are deferred until profiling identifies a relevant bottleneck. More worker threads can compete with the host's foreground client; an exploration optimizer is not evidence of a fix for idle-base pauses.

## Optimization packs actually inspected

These are reference manifests for selective testing with the existing RPG pack. Their compatibility labels are not an integrated RPG benchmark.

| Reference | Actual inspected manifest | Finding |
| --- | --- | --- |
| [Simply NeoForged](https://modrinth.com/modpack/simply-neoforged) | 26.2-1.0.4; Minecraft 26.2 / NeoForge 26.2.0.54-beta; 13 indexed files | Useful optimization reference. It includes Sodium/Lithium/FerriteCore/ImmediatelyFast, but also C2ME and ScalableLux alphas, an older Sodium alpha and ModernFix-mVUS. Preserve the pack's newer tested renderer; evaluate missing pieces individually. |
| [Optimized fps](https://modrinth.com/modpack/optimized-fps) | 4.3.0; Minecraft 26.2 / NeoForge 26.2.0.88; 18 indexed files | Useful second reference. Includes Lithium, ImmediatelyFast and ModernFix-mVUS, plus visual/QoL/network extras which do not all serve the shared-host resource goal. |
| [Simply Optimized Continued](https://modrinth.com/mod/IpYIvkPB) | Selected API-filtered 2.1+26.2 artifact actually declares Fabric Loader 0.19.3 | Reject this inspected artifact for the NeoForge pack. Its broad listing/filter association is insufficient; the downloaded manifest and Fabric JARs are decisive. Other project releases were not exhaustively audited. |

All three downloaded pack files passed their published SHA-512 hashes. `reference-packs.json` records their actual loader declarations, file lists and override paths. Files were inspected as ZIP resources and were not installed or merged.

## Proposed measurement and rollout

See [Optimizer coordination design](unification-design.md) for the documentation-backed ownership model, exact Vectors compatibility findings and staged evaluation. Enabled configuration flags do not necessarily mean a compatibility-gated optimization is executing.

The requested [client/server presets](presets/README.md) now provide native configuration files with bounded worker counts and the retained RPG-safe baseline. They are prepared in the repository only; the installer does not deploy them and personal/live configurations remain untouched.

1. Confirm the actual host uses matching pack JARs; collect exact CPU, both heap allocations, Java version and server distance settings. With 12 GB physical memory, leave room for Windows and native memory; assigning nearly all physical RAM to the two Java heaps can create paging. Do not choose a smaller heap that cannot accommodate measured live usage.
2. Introduce spark in a disposable compatibility instance, then collect a host baseline during: server with other players but host client closed; host client at the menu; host joined at the same base; and the visible spike. Hold player count, location, distances and time-of-day work comparable. Record tick percentiles/max, process/system CPU, live heap, GC pauses, disk activity and available physical memory.
3. Use [spark's spike workflow](https://spark.lucko.me/docs/guides/Finding-lag-spikes): `/spark tickmonitor --threshold-tick 50`, then `/spark profiler start --only-ticks-over 50` during the reproduction and stop with `/spark profiler stop --save-to-file` to retain a local report. Thresholds can be adjusted after observing the actual spike duration.
4. Test Lithium alone against that baseline. Exercise combat, party protection, summons, shields, building queues and saves as well as idle ticks. Keep or reject it on measured results.
5. Test ModernFix-mVUS separately if heap/loading dominates; test ImmediatelyFast separately for the foreground client. Validate all custom visual effects. Evaluate Dynamic FPS for idle/unfocused time; use the foreground FPS cap while actively playing.
6. Tune ServerCore and worker budgets only on the actual host configuration. Treat entity activation and mob-cap changes as explicit gameplay changes, with RPG exclusions and behavior checks.
7. Compare Fast Async World Save / Smooth Chunk Save only if profiles implicate saving. Check clean save/restart, inventories and chunk/entity persistence. Do not infer a conflict from overlapping marketing terms.

Acceptance is fewer long server ticks and acceptable client frame pacing without excessive memory pressure or gameplay regressions. No FPS/TPS improvement percentage is claimed from this inspection. Host hardware details alone cannot establish the cause of the existing spikes.

## Sources and evidence

- Publisher pages and exact-version API records are linked above and retained in `official-versions.json`.
- `candidate-metadata.json` records the five hash-checked candidate archives' embedded metadata; ModernFix-mVUS remains a listing/manifest-verified candidate pending that additional archive review.
- [ServerCore upstream](https://github.com/Wesley1808/ServerCore) and the inspected installed configuration distinguish basic optimizations from behavior-changing controls.
- Temporary candidate JARs, reference `.mrpack` files and local inspection outputs stay ignored under `tmp/performance-audit/`.
- No JAR, private config, running process, world, handbook or installer was altered. No commit or push was requested.
