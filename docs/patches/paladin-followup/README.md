# Paladin follow-up: Purgatory, Oath and worn Banner

Minecraft26.2 / NeoForge26.2.0.90 / Java25. Builds from the currently installed remaining-class visual repacks, preserving those uncommitted changes. Three same-version archive identities are recorded in `artifacts.json`.

## Behavior

- Purgatory can be interrupted by control before10s. From10s, native movement-control applications are rejected and existing movement impairments cleared; its own stationary stance remains. Manual release and death/dispel/logout/respec/teleport cleanup remain available. Final-stage protection still caps at0.7s per pulse.
- Its true self-damage uses absorption before health, while bypassing armor, resistance and ordinary invulnerability. Only damage left after absorption is used for fatal-cost handling. Final Resolute retains its critical-health/fatal behavior. A full channel still costs103combined shield/health points before outside recovery.
- Divine Oath's falling hammer deals70% caster M; Judgement deals100% M. The caster's P still supplies recipient absorption/support. The25% undead skill bonus and80%/35% final PvE/PvP routing remain.
- Battle Banner heals once per second, beginning at1s, for1HP plus1per complete10P. Combat buffs still pulse every0.5s. Aura6blocks, Improved8blocks; serialized modifier range and actual skill range agree.
- The existing banner model is worn on an animated player-body render layer, at1.15native scale and raised above the shoulders, rather than a world cloud following the owner. It turns/crouches with the body. Its synchronized carrier marker expires/clears with the cast; invisible, faded or dead players do not render it. No new texture or client-only dependency is required on servers.

## Investigation and verification

The native baseline reproduced14failures covering HP bypass, P-based Oath damage, old chasing entity and lack of post10s control protection. The first focus spike exposed a shared Freedom tick that removed Purgatory's own beneficial stance. Narrow incoming-control denial replaced that broad integration. A later RED test reproduced the old0.5s healing cadence before changing it to1s.

Fresh and stale-config dedicated-server suites each pass241checks under `-Xverify:all`, including actual native effect denial,10s boundary, shield spillover, M-based Oath hits,6/8range and per-second healing. Original Paladin and remaining-class checks stay in the suite. `check_resources.py` verifies the landing schools/coefficients and explicit Improved range. `verify.py --installed --runtime` binds active/fixed archives to tested hashes, checks entries/metadata/graph/control art, and excludes probes/temporary diagnostic bytecode.

Final focused client suites pass41full-channel/render checks and16early-release checks at5s/22s, with exact final archive hashes. Client tests use a copied world, native cast controller, actual hammer, Punchy and its compatibility layer. Evidence/captures are under `client/`. Native mouse-wheel scrolling and number9selection are exercised before opening inventory. Early-release tests on the original archives at5s/22s restored both inputs. An expanded installed-pack baseline5s reproduction also restored them. The reported persistent hotbar lock was not reproduced; no speculative input reset is claimed as its fix. One expanded candidate run could not allocate the class category and is recorded as a fixture failure rather than a passing gameplay test.

Personal Minecraft, worlds and the separate desktop server are untouched. Same active filenames are retained; ignored rollback copies live in `mod fixing/retired packs/paladin-followup-2026-10-29`.

## Rebuild

Run `build.py`, `server/run_server.py`, `server/run_server.py --legacy`, `check_resources.py tmp/paladin-followup/built` and `verify.py --installed --runtime` from the repository root with Python and Java25. Pinned inputs and native library caches stay under ignored `tmp/`. Client launcher/probe scripts use the isolated directory only. Tests carry their exact archive manifests.

Handbook rebuilt/reviewed:79pages,51matching contents destinations,184alphabetical entries;307main/23client archives unchanged in count. Only rendered pages17/18 changed from the previous reviewed build.
