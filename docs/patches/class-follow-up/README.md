# Class follow-up ? 30 October 2026

Same-version repacks implementing the approved [plan](../../superpowers/plans/2026-10-30-archer-homing-and-paladin-follow-up.md). Candidate builds and tests use `tmp/class-follow-up`; personal installations/worlds are untouched.

## Gameplay

- Archer normal/tipped/spectral arrows, including crossbow multishot and native arrow subclasses, receive small assistance only for close aim: 3? acquisition cone, 24-block range, ballistic path within 0.75 blocks of the target hitbox, at most 0.25? steering each tick for ten ticks and 2? total. Speed, gravity, damage and collisions remain native. One target, no reacquisition. Party players/pets are excluded; eligible enemy players are included. Saved targets use UUIDs. Existing Magic Arrows remain stronger seekers; Rain uses its existing area scatter rather than shrinking its footprint. Barrage/Power Shot arrows use assistance when delivered as native arrow entities.
- Supercharge: 35% extra normal-arrow damage, no prepared-state draw penalty. Concussive Shot: 6s base cooldown.
- Warrior Warm Up: `1 + floor(D / 14)` HP, using native combined Dual Wield power. Momentum is Onslaught; stacks grant only 2% attack speed each, up to 10%.
- Opportunist: rolling readies a hit for 5s; a successful basic melee hit opens a 1s critical window for subsequent basic hits. Existing 6s base cooldown starts on readiness consumption/expiry. Additional rolls cannot refresh readiness. Guarantee is applied only to native basic-melee critical evaluation, not global critical chance. Separate readiness/window icons use the skill art.
- Shadow Stealth breaks on successful outgoing damage, including attributed projectiles/spells, but survives incoming damage and non-damaging actions. Vanish remains 7s long with an 8s base cooldown. Mutilate heals 10% D, or 20% D with Crimson Strikes, once per successful cast.
- Priest Cadence: 3% haste per stack, five stacks/15% maximum. Divine Favor: 12s base cooldown and separate 15s readiness. Fade clears currently loaded pursuers in its dimension without a radius limit, hides body/equipment for 1s, and has no misleading range row.
- Blazing Speed: 5s base cooldown. Vanguard: 5s. Ardent Defender: 20% maximum-HP healing, 80s haste-affected base cooldown; does not cancel the triggering hit.
- Purgatory copies Fire Meteor's actual ground-impact particle groups/sound at its existing explosion event, sizes the ground marker to 12/14 blocks, and does not create another damaging spell.

## Rendering

Archers crossbows use the unloaded idle arm pose while loaded, letting the existing RPG two-handed carrying animation control the grip. Using/charging remains native. Player Animation Library's cache previously keyed animation evaluation only by frame time; a first-person pass could suppress same-frame third-person evaluation. The patch also invalidates that cache on a pass change, so Paper Doll uses the world animation rather than the local first-person result. No hardcoded hammer rotation or duplicate doll renderer.

## Build and verification

`python docs/patches/class-follow-up/build.py` recompiles focused helpers and applies count-checked ASM edits to pinned current archives in `tmp/class-follow-up/before`. Unrelated archive entries are preserved; only changed candidates are installed, under the same filenames. Exact hashes and changed-entry lists are in `artifacts.json`.

`verify_resources.py` checks base cooldown/haste flags, Supercharge and Fade metadata. `run_server.py` and `run_server.py --legacy` run isolated native NeoForge checks; stale configs intentionally contain the retired draw penalty, movement bonus and old haste/crit settings. `run_client.py` checks native render states and captures real frames. The test-only probe JARs are never shipped.

## Completion evidence

Installed five same-filename repacks: Spell Engine, Skill Tree, Rogues, Critical Strike and Player Animation Library. Active and fixed-pack bytes match. The other baseline JARs were inspected but not changed. Originals are retained in the ignored retired-pack backup.

- Fresh and deliberately stale-config dedicated servers each pass **80 native checks** with the exact final installed hashes. Thirteen resource checks pass. This includes positive/neutral/negative haste: Ardent's former Health school ignored haste, so its inert passive metadata now uses Healing school; the custom heal remains 20% of maximum HP.
- Migration regression tests reproduced and then verified removal of saved legacy Onslaught movement and Supercharge draw modifiers. Cleanup removes only obsolete effect-owned modifier IDs.
- Five native client render-state checks pass. Actual two-client LAN observation confirms the remote hammer uses `bettercombat:pose_two_handed_heavy`; the owner doll uses the same grip. Reviewed captures are in [screenshots](screenshots/).
- Purgatory's actual final-phase release path was captured with Meteor ground-impact presentation. Existing damage/ward/channel code is preserved apart from the requested Ardent coefficient.
- The 79-page handbook was rebuilt, rendered and reviewed, including the changed class pages. All 51 contents page links/numbers match; no broken HTML anchors or blank pages.
- Final review found and corrected the new Opportunist window's missing translation. No unresolved correctness findings remain in the focused review. Native tests also proved repeated guaranteed hits at zero normal critical chance.

The native verification covers focused pack combinations and the supplied weapon examples. It is not a full shader-pack or public-server balance playtest. No commit or push was requested.


A later [Purgatory ground-visual follow-up](../purgatory-ground/README.md) supersedes this patch's Spell Engine archive hash, changing only its visual helper and adding an internal visual cloud resource. The 80-check results above remain the historical gameplay baseline; the follow-up records its own eight native checks and channel/cleanup captures.
