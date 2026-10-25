# Berserker appearance

26 October 2026. Same-filename Skill Tree and Spell Engine overlay. Berserker's active status now gives the character model a fixed **50% red tint** and sparse, white firework sparkles. Combat values, cooldowns, spell IDs, language and mixins are unchanged.

## Presentation and lifetime

- Reuses Spell Engine `EntityTints`, registered against the actual Berserker effect. The tint is the opaque neutral-to-red blend `0xFFFF8080`, independent of stacks; the existing renderer multiplies this into the model's color. It does not make the character transparent.
- Reuses `CustomParticleStatusEffect`, driven by synchronized active effects on the client. Each cycle emits three white firework sparks at randomized body positions: ticks 0, 4 and 8, then the next cycle starts at tick 13. This interprets the requested timing as 0.2s between individual sparks followed by a 0.25s pause. Stack gains do not increase spark count or restart the cycle.
- Sparks have a four-tick lifetime and very low outward drift. A small client-only list tracks the live sparks. Client tick cleanup removes remaining particles when the synchronized buff disappears, the entity dies/is removed, or the client changes worlds. No delayed task can continue spawning particles after removal.
- Native effect synchronization clears the tint and particle-emission state on normal expiry, fatal-hit rescue, dispel and the existing death/respawn cleanup. Remote viewers need the matching client archives; synchronization is subject to normal network tick latency. Other effects keep their native tint composition.

## Rebuild and verification

```powershell
python docs/patches/berserker-visuals/build.py
python docs/patches/berserker-visuals/run_client.py
python docs/patches/berserker-visuals/verify.py --installed --runtime
```

Pinned active inputs are in ignored `tmp/berserker-visuals/before/`; candidates are under `built/`. `artifacts.json` records before/after hashes, one changed registration class and one new helper per archive. The common helper contains no Minecraft client references; the particle helper is registered solely from Skill Tree's existing client initializer. No new mixins or resource dependencies are required.

**41 connected-client checks and 2,701 archive preservation checks passed.** The disposable connected-client/integrated-server probe checks the tint attachment, synchronized buff state, staggered schedule, white firework particles and their lifetime, fatal rescue/particle cleanup, natural expiry and death cleanup. Screenshots show the active red model and its return to normal. Archive verification checks every unrelated entry byte-for-byte. This is an isolated reduced pack test, not a full-pack multiplayer performance test; personal installations/worlds are untouched.

The handbook and Warrior owner reference describe the new visual cue. Original active/fixed archives are preserved under ignored `mod fixing/retired packs/berserker-visuals-20261026/`. Matching same-name replacements belong on both client and server. No GitHub push is part of this request.
