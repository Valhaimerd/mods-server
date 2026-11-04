# Client optimization without Vectors

4 November 2026. Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25. User reported excessive client CPU/memory use and requested removal of Vectors plus a CurseForge-researched replacement combination.

## Selected combination

| Component | Decision | Role |
| --- | --- | --- |
| Sodium 0.9.2 | Retained | Main terrain renderer |
| FerriteCore 9.0.0 | Retained | Existing block-state/model/data memory optimizations |
| More Culling 1.8.1 | Retained | Existing block-face rendering culling |
| [ImmediatelyFast 1.16.5](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast/files/8898471) | Added to client store | Batches immediate rendering work; this build includes the Iris banner-rendering fix |
| [Entity Culling 1.11.3](https://www.curseforge.com/minecraft/mc-mods/entityculling/files/9100314) | Added to client store | Skips rendering hidden entities/block entities |
| [Dynamic FPS 3.11.9](https://www.curseforge.com/minecraft/mc-mods/dynamic-fps/files/8260229) | Added to client store | Reduces background/idle rendering and power use |
| [ModernFix-mVUS 5.27.21](https://www.curseforge.com/minecraft/mc-mods/modernfix-mvus/files/8896910) | Added to client store | Loading/memory optimizations; explicitly the maintained port/fork, not an original upstream 26.2 release |
| Vectors local 1.5.0-alpha.4 | Removed | Original retained in ignored retired packs |

All four additions were downloaded from the selected CurseForge file's ForgeCDN URL. Exact source URLs, SHA-256 hashes and embedded NeoForge metadata are recorded in `downloads.json`. Loader/game version metadata and archive integrity were inspected. These are client-profile additions; no dedicated-server optimizer or gameplay JAR is changed.

The pieces target different work. Lithium is not added to solve a remote client's rendering/heat problem; extra threaded chunk generators, another lighting replacement and experimental dynamic model loading are also not enabled. The retained FerriteCore already supplies memory optimization, so memory savings from this combination must be measured rather than added from publishers' claims.

## Installer and configuration

Removed the installer's Vectors config merge, embedded preset, source/self-test and retired unbound control. Existing old Vectors config files are harmless and are not deleted from personal instances. Entity Culling debug controls remain unbound in the recommended profile. The source installer is built/tested locally; no new GitHub release is published by this task.

Use a **60 FPS foreground cap**, around **8 render-distance chunks**, and begin with shaders off when prioritizing heat over maximum frames. Dynamic FPS primarily helps while idle/backgrounded; it does not cap foreground gameplay for you. The retained coordinated preset sets Sodium's chunk workers to **2**; it is opt-in and not automatically deployed. Native optimizer defaults remain enabled, without experimental memory switches or fabricated JVM flags. Do not allocate nearly all system RAM to the heap, especially when hosting a server on the same machine.

The pack installer can retire managed Vectors copies when syncing the changed client-store listing. It preserves untracked files, so a manually installed copy may need removal from that instance's mods folder. This task does not alter the user's personal `.minecraft` or separate desktop server.

## Validation and limits

The complete-pack isolated client fixture uses every active server-compatible JAR plus every client-store JAR, removes Vectors and adds the four exact candidates. The probe checks loaded optimizer identities, texture/entity renderer initialization, a full resource reload and continued responsiveness. Its heap is capped at 3 GB for this startup test. A startup heap limit is not a promise that a populated world fits in that heap.

No claim is made of a measured temperature reduction, FPS percentage or before/after memory saving. The user's reported hardware problem has not been profiled on their live installation. Runtime and installation evidence are stored beside this file. Personal worlds remain untouched; no commit/push is requested.

Final result: **12 full-pack client checks passed**, including both resource loads and responsive menu rendering. The fixture bypasses only WoVer's first-run welcome acknowledgement to run unattended. Four exact tested official-release JARs are installed in `mod store/` and fixed packs; Vectors is absent and backed up. Main/client counts are **305/26**. Installer build and self-tests passed without Vectors provisioning. Handbook counts and the two verified native Entity Culling debug bindings were updated; optimizers were not added as gameplay catalog entries. No hardware temperature/FPS/RAM benchmark or in-world multiplayer playtest is claimed.
