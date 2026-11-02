# Coordinated client/server configuration presets

Prepared 1 November 2026 for Minecraft 26.2 / NeoForge, with a client and dedicated server sharing a 12 GB, 14th-generation i5 PC. These are **opt-in native config files**, not automatic installer changes. Personal installations and servers have not been modified. No new optimizer is required.

## Settings and responsibilities

| Component | Client | Dedicated server | Reason |
| --- | --- | --- | --- |
| Sodium 0.9.2 | 2 chunk-building workers; deferred building retained | Not installed | Bound background mesh work instead of sizing solely from logical CPU count. |
| ScalableLux 0.2.1 | `parallelism=1` | `parallelism=1` | Bound each process's lighting workers while both share the CPU. |
| Vectors local alpha.4 | Existing installer performance preset; `chunkWorkers=0`, adaptive quality and automatic render distance off | Not installed | Preserve existing filters and let Sodium own terrain work. Compatibility gates still apply. |
| ServerCore 1.5.19 | No separate client preset | Dynamic adjustments and activation range off; reduced synchronous loads and ticking-chunk cache on | Avoid competing automatic distance policies and altered RPG entity ticking. |
| Smooth Chunk Save 4.2 | No separate client preset | Retain 300-second save delay and 20 unloads/tick | Keep the existing save baseline until profiling implicates disk/save work. |

Only the worker limits are new tuning changes relative to the sampled profile. Other supplied settings pin the reviewed baseline. These limits are a starting point, not a proven optimum: fewer workers may slow chunk catch-up when exploring. Sodium can also execute work on the calling thread; these numbers do not limit the entire JVM's threads.

Retain FerriteCore, More Culling, Fast Async World Save, Clumps, Leaky and Structure Layout Optimizer at their current settings. They handle different work; no unsupported disable switches are invented. Chunky should not be actively pregenerating during the baseline test. Candidates such as Lithium, spark, ModernFix-mVUS and ImmediatelyFast remain uninstalled and are not prerequisites for these files.

## Installation and rollback

1. Stop the target client/server completely. Back up the matching destination files below outside its `config` folder. Preserve existing unrelated settings and every world.
2. On the **client**, use files under `client/config/` in the instance's `config/` folder. On an existing customized instance, merge the settings rather than replacing the entire Sodium file: the essential change is `performance.chunk_builder_threads=2`. Preserve personal visual and notification preferences. Vectors uses the same managed values as the existing installer preset plus explicit zero workers.
3. On the **dedicated server**, use `server/config/` in its own `config/` folder, preserving the `servercore/` subdirectory. These complete ServerCore templates retain the inspected default mob counts and behavior controls; review/merge them if the host has custom rules. The essential worker change is `parallelism=1` in its own `scalablelux.properties`.
4. A client joining a dedicated server needs only the client files. For a client hosting a singleplayer/LAN integrated server, apply both sets into that one instance; the identical ScalableLux file needs copying only once.
5. Restart both. The repository installer does **not** deploy these presets. Its existing Vectors merge uses matching managed values, so it does not undo this preset's overlapping Vectors settings. Recheck settings after mod updates.
6. To roll back, stop the affected process and restore its backed-up files. If a supplied config did not previously exist, move it out of `config/` and let the mod regenerate its defaults on restart.

Do not copy a whole `options.txt` or `server.properties` from another user. Start testing with foreground FPS capped at 60, client render distance 8, and server view/simulation distances reviewed separately; the files here do not change those settings or allocate either heap. Do not increase combined heap limits beyond what measured physical memory can support.

## Verification and limits

The native file schemas and accepted worker settings were checked against the installed JARs and generated configs. Sodium's `ChunkBuilder.getThreadCount()` accepts a positive count capped to available processors. ScalableLux's `Config` accepts `parallelism=1`; this installed build's `SchedulingUtil.isExternallyManaged()` returns false, so that config controls its own scheduler.

Validation passed: all three JSON files, both properties files, both YAML files using ServerCore's bundled SnakeYAML 2.6, assertions for the retained ServerCore controls, matching installer Vectors values and local documentation links. No live configuration was written. No JAR or installed mod set changed, so no gameplay handbook/PDF rebuild is needed.

Syntax/schema checks are not a full-pack runtime test. After applying on the actual host, compare the same base and players with/without the host client joined: long tick counts, frame times, available RAM/paging, GC and disk activity. A lower worker limit can reduce contention but cannot fix an unmeasured heap, disk or gameplay hotspot. Test combat/summons, visual effects and clean saves/restarts before adopting permanently.

See the adjacent [coordination design](../unification-design.md) and [audit](../README.md) for exact versions, compatibility gates and profiling steps.
