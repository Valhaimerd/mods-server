# Paladin gold areas and impact light

Approved visual repair,30 October 2026. Reuse native gold ground zones and holy particles; no gameplay changes, new polling or independent gameplay timers.

- Divine Protection:8-block cast footprint around the caster. This is a brief cast indication: its buff applies at cast time and does not become a following aura.
- Battle Banner:restore its continuously pulsing6/8-block gold area, attached to the wearer while its existing pulses run.
- Divine Strength:brief4-block gold heal area only after Purge Heal successfully hits with that node.
- Seal of Light:brief4-block gold heal area only on the fifth/final preparation pulse with that node.
- Divine Oath:4/6-block gold impact footprint and upward holy burst at the hammer recipient, including impacts with no enemies nearby. Existing target-hit particles stay.

Use existing native particle types/textures and the current event paths. Preserve Purgatory burning ground, worn Banner model, all balance data, healing eligibility and spell descriptions. Compile the current PaladinVisuals source; count-checked bytecode inserts add only the two missing conditional tier-I cues, preserving every previous class hook. Validate native particle groups, real deliveries/branch conditions and client captures, then install same-name replacement, synchronize docs and rebuild/review handbook.

## Cause and implementation

The old ground ring was only24 small random sparks. The native particle library adds half the source entity width to circle extents, so the spark ring also had a small unrequested radius offset. Banner's worn-model conversion preserves its pulse event, but that event no longer draws a readable luminous area. Protection's custom delivery supplies no persistent native area visual. Oath's enemy-hit particles do not substitute for a central impact cue when no enemy is hit.

`PaladinVisuals` now emits an emissive native `area_circle_1` perimeter at the actual radius, a translucent gold `area_effect_293` light pulse, and interior holy sparks. Banner zones follow horizontal owner motion and probe the ground; cast/heal/impact zones remain at their event location. Native particle lifetimes fade the cue; no additional world entity, damage impact, sound loop or visual polling system is added. Divine Strength and Seal of Light use two count-checked bytecode insertions in the current Tier-I handlers. All spell data, native damage/healing, party eligibility and Purgatory resources remain byte-identical.

Build and verify from repository root:

```powershell
python docs/patches/paladin-gold-areas/build.py
python docs/patches/paladin-gold-areas/run_server.py --baseline
python docs/patches/paladin-gold-areas/run_server.py
python docs/patches/paladin-gold-areas/run_client.py
```

The candidate is staged in ignored `tmp/paladin-gold/built/`. Server tests capture real native particle-send calls using a test-only Mixin, including failed and successful Purge, branch gating, all five Seal pulses, both Banner radii and Oath landing without enemies. The test Mixin is never included in production. Client captures exercise the actual native events in a disposable instance. Personal installations/worlds are untouched.

## Installed verification

The pre-patch native fixture reproduces the missing gold Protection area; the candidate passes **48 native checks**. Real event checks verify all requested radii, emissive ground-facing zones, stationary cast footprints versus following Banner presentation, success/node gating, final-pulse timing, an enemy-free Oath impact burst and preserved Purgatory ground data. The current client also completes actual Banner pulses, healing events, falling hammer delivery and expiry; captures were visually reviewed. This is isolated client/server coverage, not a full-pack shader or multiplayer certification.

[Protection area](screenshots/divine-protection-radius8.png) ? [Banner area](screenshots/battle-banner-radius6.png) ? [Final Seal pulse](screenshots/seal-of-light-final-radius4.png) ? [Oath impact](screenshots/divine-oath-impact.png)

Installed in `mods/` and ignored fixed packs under the original filename. Previous archive remains under ignored `mod fixing/retired packs/paladin-gold-areas-2026-10-30/`. Only `PaladinVisuals.class` and `PaladinTierOne.class` change; all 2,198 other entries remain byte-identical. No commit or push requested.
