# Elf slow-falling delay

25 October 2026. Same-filename replacement for `iourusraces-1.1.0.jar`; install matching client/server copies and restart.

## Behavior and cause

The previous ability capped every descent faster than 0.20 blocks per tick, including the downward half of normal jumps. The replacement starts only after 2.5 blocks of accumulated descent. Ordinary flat-ground bunny hopping stays natural; higher jumps or downward terrain drops can still cross the threshold.

After activation, the existing synchronized 0.20-block-per-tick downward cap remains. Fall distance stays at a safe 2.5-block floor instead of being reset to zero, keeping protection active throughout a long drop without oscillation. Native landing resets it for the next jump. Rising, flight, gliding, climbing and fluid exclusions remain. No timers, player maps, saved flags, effects or extra tick handlers are added.

Only `RaceBalanceRules.class` changes, using the latest Orc-followup source as its base. All other installed race-state/leaf/UI/Asgardian patches, resources and race stats remain byte-identical. Historical race-balance builds predate this follow-up; use this overlay last.

## Verification

The baseline connected-client fixture failed with `Short jump altered at 0.0`. The candidate passed direct short-fall, threshold, continuation, landing, next-hop, upward and flight checks; four actual connected-client jumps ran without the slow-fall cap. A 20-block drop then landed at fixture tick 184 with all 18 HP. The report and trace are checked against the exact tested JAR hash. Timing is fixture-specific, not a performance benchmark. Tests used an isolated Minecraft 26.2 / NeoForge 26.2.0.90 client and integrated server; no personal profile, server or worlds changed.

```powershell
python docs/patches/elf-slowfall-delay/build.py
python docs/patches/elf-slowfall-delay/run_client.py
python docs/patches/elf-slowfall-delay/verify.py --installed
```

Active and fixed copies match. Predecessors are retained in ignored `mod fixing/retired packs/elf-slowfall-delay-2026-10-25/`; build/test work stays under ignored `tmp/elf-slowfall-delay/`. No push was requested.
