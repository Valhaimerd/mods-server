# Iourus race balance and Elf falling synchronization

**Superseded Elf falling behavior:** the [native safe-fall revision](../elf-safe-fall/README.md) replaces the slow-falling mechanics described below.

24 October 2026. Same-filename overlay on the installed `iourusraces-1.1.0.jar`, retaining all eight race IDs and the Asgardian/Bifrost integration. Embedded metadata remains upstream 1.0.0. Both client and server need the matching replacement.

## Race changes

- **Elf:** removes the entire owned-projectile damage amplification; base movement bonus rises from 5% to 8%. Jump strength, night vision and leaf collision remain. Slow falling now caps downward velocity at -0.20 blocks/tick and sends the correction to the owning client. Horizontal/upward movement is preserved; flying, gliding, climbing and fluids bypass this correction.
- **Goblin:** uniform scale 0.75 instead of 0.60.
- **Asgardian:** scale 1.10 instead of 1.05. Removes the 2% block-breaking-speed modifier and cleans up the old persistent modifier on race application. The 2% damage bonus, lightning immunity, magic weakness, HP and movement remain.
- **Orc:** applies a named -10% total attack-speed modifier. It multiplies the attack-speed value used by native attacks and Better Combat, and feeds physical-melee RPG haste. At current health at or below **50/40/30/20%**, gains **+2/+3/+4/+5 melee attack damage** and **+1/+1.5/+2/+2.5 ranged damage**. The last tier also covers 10% HP; no further increase. Above 50%, neither damage bonus applies. The user confirmed starting at 50% and capping at 20%.
- **Dwarf:** +2 armor as a removable racial attribute modifier; HP, mining and movement retain their values.

Orc damage bonuses are additive attributes, available to weapon damage and corresponding RPG school power. They refresh after health changes and immediately on an attack, disappear after recovery/race changes, and never overwrite a newly configured base value. No new percentage hit multiplier is added. Existing armor/party/PvP rules remain separate.

### Legacy Orc base-value repair

The old handler directly wrote base attack damage in the 4–10 range. On the first application of this revision, an **existing Orc** carrying a value in that range is restored to the ordinary base 1; its original value is backed up in the player's persistent `iourusraces:legacy_orc_attack_base` field. A version marker prevents repeated repair and is copied on clone. Later external base-value changes are preserved. Other races' bases are not altered.

Historical Orc writes have no provenance marker, so a pre-update administrator-set Orc base in the same 4–10 range cannot be distinguished from the old mod's write and will also be backed up/restored. This limited migration avoids retaining the old damage inflation underneath the new named bonus. Players who switched away from Orc before this update cannot be reliably identified as old Orcs from the current race alone.

## Elf defect evidence

The old handler changed server velocity only after falling faster than -0.45, resetting it to -0.20 without flagging the motion update. A real connected client dropped 20 blocks onto a test platform still landed at fixture tick **48**, reaching 1.51 blocks/tick downward. The server cleared fall distance, but the client did not receive effective slow falling.

Marking motion synchronization alone improved the result to tick 75 but retained the threshold's fast-fall oscillation. The final constant downward cap plus synchronized motion landed at tick **109** with full 18 HP. Client/server tick latency means an individual client sample can briefly exceed the server cap; this is not a flight ability. Native falling remains active above the terminal cap.

## Rebuild and proof

```powershell
python docs/patches/race-balance/build.py
python docs/patches/race-balance/run_server.py
python docs/patches/race-balance/run_server.py
python docs/patches/race-balance/run_client.py
# Wait for tmp/race-balance/client/fall-result.txt before verification.
python docs/patches/race-balance/verify.py tmp/race-balance/built
```

`run_client.py --baseline` runs the same falling check against the active pre-install archive; retain its expected failing result as defect evidence. Use it before replacing the active JAR, not after installation. `artifacts.json` pins input/output hashes and exact changed entries; pinned source archive remains ignored at `tmp/race-balance/before-iourusraces-1.1.0.jar`. Existing Asgardian enum/attribute sources are reused; the revised ability handler is stored here for reproducible builds.

The dedicated fixture includes Better Combat 3.2.2, RPG class/weapon APIs and the pack's party/PvP integration. It checks native and Better Combat attack delays, physical melee/ranged spell power at all health thresholds, recovery/removal, equipment modifiers, legacy repair, repeated attribute application, unchanged Asgardian damage/weakness/lightning behavior and save/network/reset/respawn paths. The connected client checks actual falling and landing, rather than assuming a server velocity assignment reaches the player.

Previous active/fixed archives are retained under ignored `mod fixing/retired packs/race-balance-2026-10-24/`; matching replacement copies belong in `mods/` and `mod fixing/1 fixed packs/`. Personal Minecraft, external server installations and personal worlds remain untouched. No commit or push requested.

Final proof: **166 native server measurements passed**, including an actual restart and cloned-player migration marker. The final connected Elf drop landed at tick 117 with all 18 HP; an earlier corrected run landed at 109, compared with baseline 48. Timing varies with synchronization. Runtime details are in `runtime-results.txt` and `server-measurements.txt`.

Handbook verification: all 77 pages rendered; only pages 7 and 16 changed against the prior reviewed edition, and both were visually checked. All 51 contents destinations remain correct; root/output PDF copies match. The local owner reference is generated with `python docs/patches/race-balance/export_reference.py`.

Latest Orc clarification: [starting bonuses at full health and a 10% bow/crossbow draw-speed penalty](../orc-followup/README.md). Earlier above-50% values in this document are historical.

Latest Elf follow-up: [delayed slow falling](../elf-slowfall-delay/README.md) leaves the first 2.5 blocks of descent unchanged, so ordinary bunny hopping no longer floats. It preserves this patch and subsequent race changes.
