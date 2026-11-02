# Purgatory pull, enclosure and Paladin tooltips

2 November 2026. Purgatory now draws enemies continuously between its unchanged half-second damage pulses. Normal sprinting can overcome phase I; phase II wins against ordinary sprinting; phase III pulls much harder. The opening yank remains stronger than sustained pull. Knockback resistance still applies, allies are excluded, and normal terrain collision remains. The per-tick pull uses received player movement so stale server impulse velocity does not overwhelm movement input. Nearby candidates are refreshed every four ticks, with eligibility checked each tick.

The orange enclosure extends **5 blocks below and 5 above** the caster, at the existing **12/14-block radius**. It has no floor or roof, changes no terrain, and keeps the existing friendly, projectile and cleanup behavior. Purgatory upgrade-only nodes hide the generic range row that displayed 50 blocks; Improved explicitly lists its 7-block flames and 14-block outer effects.

Ardent Defender heals **30% maximum HP**, up from 20%, before healing-received modifiers. Its existing trigger, cooldown, defenses and refusal to cancel the triggering hit remain.

## Validation

The focused integrated-client probe holds sprint against an enemy caster's channel. Over 30 ticks, the player moved **4.45 blocks outward in phase I**, **5.49 inward in phase II**, and **4.88 inward in phase III**, where it reached the center early. The final figure is limited by starting distance, not a weaker pull. All four client checks passed, including enclosure cleanup. Eighteen movement/collision checks and sixteen archive/data checks passed. Guarded bytecode edits verify the single Ardent healing constant change. This is an isolated locomotion fixture, not a full multiplayer balance playtest.

Replacements retain their active filenames in repository `mods/` and ignored fixed packs. Originals are backed up under ignored `mod backup/purgatory-continuous-pull-2026-11-02`. The pending six-second Ward tuning is preserved. Both client and server need matching JARs and a restart. No personal profile, live server, commit or push is included.

Sources: [approved plan](../../superpowers/plans/2026-11-02-purgatory-continuous-pull.md), [archive identities](artifacts.json), [focused results](focused-results.txt).

The [24-second timing follow-up](../purgatory-24s/README.md) moves the stage transitions to8s/16s and rescales the final explosion over eight seconds; the pull-strength observations above remain those of the original tuning fixture.
