# Bifrost first-person visibility repair

24 October 2026. The reported symptom was successful teleportation with the beam/particles apparently missing. The installed personal-client JAR was checked read-only and matched the repository's initial expansion hash (`6994aee55ed85475c10c238c40efcee86a9444975ce4c46cbe8ea57e5c5de99b`), so this was not an outdated-file mismatch.

The earlier screenshots used an elevated observer camera. A new ground-level first-person run reproduced the symptom: the descending column starts overhead, then its lower edge rises above the player's eye almost immediately after impact. Most particle packet origins also move high above the player; native short-range delivery can omit those packets for someone at the base. The transit still runs and the large column remains visible from outside. Sodium, More Culling and Vectors were enabled for this reproduction; this does not establish those mods as the cause.

`TransitVisuals` now retains twelve inward-facing translucent panels around the ground-level footprint, three blocks tall, until that beam closes. The original tall column still descends/retracts independently. End Rod and Portal particle origins remain near the passengers instead of following the elevated front. The particle budget stays at 30 per beam every four ticks; geometry is bounded at 28 panels per beam. All panels remain non-colliding and are discarded by the existing cleanup paths.

Only `TransitVisuals.class` and its `Panel` record differ from the preceding production archive. Capture, transport, cooldowns, damage, ownership, recipes, protocol and transit sound timing are unchanged. Source closes at tick 54, transfer remains tick 60, and target cleanup remains tick 100.

## Proof and reproduction

The native regression failed on the prior build because no caster-height visual survived the rising phase. It passes with the repair. Both dedicated variants (with and without revive mods) pass 126 expansion checks, including the new visual lifetime check and the existing transit, combat, ownership and cleanup checks.

Focused connected-client tests capture descent, impact, upward closure and arrival, with actual initiator/passenger arrival and four phase sound-event checks:

```powershell
python docs/patches/asgardian-expansion/run_client.py --visual-only --first-person --renderers
# Wait for tmp/asgardian-expansion/client/expansion-client-result.txt.
python docs/patches/asgardian-expansion/run_client.py --visual-only --renderers
# Wait for the result and inspect the observer screenshots too.
```

`--renderers` installs only the repository's Sodium, More Culling and Vectors into the disposable client. Omitting it removes those three test copies. `--first-person` keeps the native player camera; the default uses the observer camera. `--visual-only` skips unrelated selector/weapon-input fixtures. Omitting these flags restores the original full client test. The native-menu fixture now waits up to 200 client ticks for an expected response instead of failing after a fixed 15-tick window during renderer startup.

Before: `tmp/asgardian-expansion/client/screenshots/2026-10-24_10.45.56.png` through `10.45.59.png` show the nearly empty first-person view. After: `10.53.25.png`, `10.53.26.png`, `10.53.26_2.png` and `10.53.28.png` show the coloured ground-level effect and nearby particles through departure/arrival. Screenshots and runtime logs remain local; final hashes/results are in [runtime-results.txt](runtime-results.txt).

The same active filename is retained, and the predecessor is backed up under ignored `mod fixing/retired packs/bifrost-visibility-2026-10-24/`. Both repository `mods/` and `1 fixed packs/` receive the tested build. Personal Minecraft and the separate desktop server are not modified. This fix is not committed or pushed.

The handbook's existing controls and travel description remain accurate, so its PDF needs no behavioral revision for this visual repair. First-person and observer screenshots were reviewed with the three rendering mods; other shader/resource-pack combinations and long-distance multiplayer packet delays remain outside this isolated proof.
