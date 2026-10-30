# Purgatory burning-ground pulse visual

Purgatory now emits Meteor's burning-ground cloud particles on each fixed 0.5s pulse, in a six-block radius around the caster. The source is `skill_tree_rpgs:fire_tier_3_spell_1_modifier_2`, the Fire Meteor upgrade, including `flame_ground`, `flame_medium_a`, `flame_medium_b` and `flame_spark`.

A short native Spell Cloud carries the copied client appearance. Its lifetime is one pulse interval; its spell has no impacts and no repeated sound, so it neither adds damage nor changes Purgatory's existing burn/pull/healing behavior. The previous finishing explosion remains Meteor's impact burst. The burning-ground visual radius is six blocks, including with Improved Purgatory; the existing combat radius/range marker is unchanged.

Build: `python docs/patches/purgatory-ground/build.py`. Repack only the current Spell Engine JAR under its existing filename. Only `PaladinVisuals.class` changes, with one added internal spell resource. The baseline is the final class-follow-up build; every other archive entry is preserved.

Verification: `run_server.py --baseline` reproduces the missing ground visual resource. Candidate native checks verify six-block radius/twelve-block diameter, the copied Meteor flame, one cloud per pulse, zero damage impacts and expiry. `run_client.py` uses an isolated world and an unlocked Paladin root to show real channel pulses and cleanup. No personal installation or world is modified.

Installed and verified: eight native server checks pass, and real channel/cleanup captures show the ground flames appear during pulses and clear afterward. [Channel capture](screenshots/channel-ground-flames.png). All unrelated archive entries match the previous installed build. The same JAR is installed in `mods` and the fixed-pack folder. No commit or push requested.
