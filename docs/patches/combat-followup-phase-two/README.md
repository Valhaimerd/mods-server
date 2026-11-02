# Combat follow-up phase two: multiplayer wall and weapon reach

2 November 2026. Same-version replacements for **Spell Engine** and **Better Combat**, built over the installed phase-one outputs. Existing filenames, saved IDs, phase-one skill changes and all unrelated archive entries are retained. Update both client and server, then restart. No configuration deletion is needed.

## Purgatory wall

The orange enclosure now enforces physical player passage on the server after the native movement packet is processed. Its existing entity-motion clipping was not sufficient: vanilla can accept a small clipped movement and subsequently snap the player to the packet's original coordinates. Repeated short movements could therefore cross a wall that does not consist of world blocks. This is a validation gap, not evidence that ping alone caused it.

The correction evaluates the swept displacement against active hostile walls and uses the native position-correction/acknowledgment path. It reads **current Better Party membership** on every relevant movement. Current allies and the caster pass; outsiders and players who leave, are kicked or lose the party cannot cross. A player overlapping the wall can move back toward the valid side without being ejected outside. Overlapping casters' walls are checked independently.

The shape remains a cylinder with a **12-block radius**, or **14 with Improved**, extending **five blocks below and five above** its origin. There is no floor, roof or terrain modification. Existing spectator, pet, projectile and genuine teleport policies remain; movement correction does not turn this into teleport prevention. Expired/removed walls, dead owners, different levels and owners without an active channel do not restrict movement. No saved whitelist, retained player reference or world-wide entity scan is added.

## Better Combat reach

The earlier patch correctly scaled `entityInteractionRange`, but Minecraft 26.2's native attack packet checks **`isWithinAttackRange` against the item's AttackRange component**, a different path. Better Combat normally routes attacks through that packet. Large players could therefore have a correct client range and still fail native validation. Its optional direct server range check also used the unscaled weapon range.

Registered **non-piercing Better Combat weapons** now validate using the weapon's current range and configured scale multiplier, with the same shoulder-height origin used by Better Combat target selection. The native validation buffer remains. Non-Better-Combat items and native piercing-component handling keep their existing validation. The optional direct Better Combat check now applies size once as well. Block reach, attack geometry, damage, cooldowns and target/party rules are unchanged.

There is no new persistent reach modifier to become stale. Titan's Mark and Warrior passives affect reach through their actual synchronized scale. Existing configured scale influence remains authoritative.

Measured dedicated-server sword values:

| Loadout | Actual size | Calculated weapon reach |
| --- | ---: | ---: |
| Normal | 1.00 | 3.00 blocks |
| Titan's Mark | 1.25 | 3.75 blocks |
| Revised Enrage, six thresholds | 1.24 | 3.72 blocks |
| Titan's Mark + Enrage | 1.55 | 4.65 blocks |
| Large fixture | 2.50 | 7.50 blocks |
| Very large fixture | 4.00 | 12.00 blocks |
| Shrunk fixture | 0.75 | 2.25 blocks |
| Effects/items removed | 1.00 | 3.00 blocks |

Titan's multiplier combines multiplicatively with Enrage's modifier, hence 1.25 × 1.24 = 1.55. These are configured weapon reach values, not promises about every target's hittable distance regardless of facing, hitbox, obstacles or height.

## Evidence and limits

An isolated NeoForge **dedicated server and a real connected NeoForge client** reproduced the old wall crossings and rejected large-size attacks, then passed **61 focused checks** with the candidates. The client sent actual movement and Better Combat attack packets; this is a scripted network fixture, not a manual PvP session. Caster ownership/party setup is synthetic, while the moving/attacking player uses the real remote connection. Native party/team state was synchronized for join/leave scenarios. Baseline logs identify the rejected native attack checks and failed damage registration at large sizes.

Coverage includes entry/exit, current allies, former/kicked/dissolved-party members, an overlapping player, two overlapping walls, release cleanup, actual Curios Titan equipment, the selected Enrage node, shrink/removal, accepted damage and unchanged block reach. **14 additional native lifecycle checks** cover dead/removed owners, channel release, caster/pet/spectator passage, Creative blocking, overlapping-body recovery, vertical bounds and rejection of a distant forged target. **51 geometry checks** cover both radii, multiple body widths, outside-to-outside sweeping and the open top/bottom. **2,631 unchanged-entry preservation checks** cover the phase-one input archives. No full-pack latency benchmark or separate reconnect/dimension-transition playtest is claimed; the new correction keeps no per-player state that needs restoring across those transitions. The affected external live server/profile was not inspected or changed.

Read-only final review found no important actionable issue. [Archive identities](artifacts.json), [remote checks](candidate-checks.txt), [geometry results](geometry-result.txt) and [completion record](completion.json) accompany the patch.

## Rebuild and verification

```powershell
python docs/patches/combat-followup-phase-two/build.py
python docs/patches/combat-followup-phase-two/run_geometry.py
python docs/patches/combat-followup-phase-two/run_lifecycle.py
python docs/patches/combat-followup-phase-two/run_remote.py
python docs/patches/combat-followup-phase-two/verify.py
```

Inputs are pinned under ignored `tmp/combat-followup-phase-two/before`; archives in `built` are complete current-input candidates. `run_remote.py --baseline` uses the original snapshots. The fixture uses only isolated test worlds and port 25578, never personal/server worlds. Stop its isolated processes before replacing its test JARs. `install.py` verifies candidate/test hashes and backs up production inputs before installing same-name replacements. No commit or GitHub push is included.

The rebuilt handbook remains 80 pages with 469 valid internal links and 51 correct printed contents destinations. Changed pages 18–19 and the contents were visually reviewed; root/output copies match.
