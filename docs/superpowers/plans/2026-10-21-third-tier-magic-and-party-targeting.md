# Third-tier magic and party targeting implementation plan

> **For agentic workers:** Use the executing-plans workflow and the independent specialist tasks recorded below. Complete integrated verification before replacing active JARs.

**Goal:** Rework the third active Wizard tier and let all class spells affect non-party targets while protecting Better Party players and pets.

**Architecture:** Layer small helpers and resource changes over the currently installed revision-five archives. Shared Spell Engine relation/impact rules establish party safety for every class; focused helpers cover timed wall casts, cloud volumes, Blink bursts and summon attributes/AI.

**Tech stack:** Minecraft 26.2, NeoForge 26.2.0.90, Java 25 bytecode, existing Spell Engine/Wizards/Skill Tree/Better Party APIs, Python ZIP repacking and ASM.

**Spec:** User request of 21 October 2026; detailed effective values will be recorded in `docs/patches/magic-third-tier/README.md`.

## Constraints and interpretation

- Preserve active JAR filenames, existing skill IDs, earlier balance revisions, PvP damage retention and party-friendly-fire protection. No personal installation or server changes; no Git push requested.
- Harmful class spells may affect passive mobs, neutral mobs and non-party players/pets. Self, own pets and party-owned entities remain protected. Beneficial shared spells affect self and party allies/pets only.
- Remove frost slowing/freezing health and boss exclusions. Fire application remains subject to actual fire damage resistance/immunity.
- Burst Blink: 10/15 blocks, 50% Arcane damage in radius five at departure immediately and arrival five ticks later; Slipstream cooldown -3s; Purge grants 1.2x movement for four seconds.
- Evocation: 35s full-channel cooldown, 5% base critical and 3% base haste per stack, retaining damage-taken drawback; Rapid permits 12 stacks, Lasting lasts 15 seconds.
- Wall: four casts within five seconds from first cast, then normal cooldown; timeout also starts cooldown. Damage20%, cylinder height2; Great Wall radius1.2/height3 and living collision; healing0.5%. Meteor cloud height2.
- Hydra: haste affected30s cooldown,20s active; Ancient one3x head continuously channels own-power5%/delivery,25 deliveries/5s,12-block20-degree cone,12-block acquisition, mouth origin.
- Blizzard:12-20s cooldown by completion,raw40% coefficient,3-block splash without falloff.
- Elemental: haste affected30s cooldown,20s lifetime; HP30+.5P,armor5+.08P,scale1,Frost power.5P,melee.5P,shard100% ownpower. Legion three at.8x all listed formulas; Colossus one at2.5x and caster knockback resistance. Existing Improved summon +5s remains. Better Party's owned-pet recognition must include them.

## Review focus

- Party membership and pet ownership must remain protected for direct, area, delayed, cloud and summon attacks; no friendly frost/burn side effects.
- Mixed heal/damage spells must heal only allies and hurt only enemies.
- Walls must expire, avoid modifying world blocks and not block projectiles or become attack targets.
- Shared spell definitions must not mutate across different casters, nodes or summoned entities.
- Test helpers must never enter production archives; cached personal configs must not be touched.

## Tasks

- [x] Trace and test shared relations and impact eligibility; implement PartyRules/PatchParty (party specialist).
- [x] Implement Hydra/Elemental data and summon behavior; validate actual owned-pet recognition (summon specialist).
- [x] Implement wall burst cooldown, cylinder lookup and collision (wall specialist).
- [x] Implement Burst Blink, Evocation and Blizzard resources/helpers; assemble all changes with an entry inventory (root).
- [x] Run structural checks plus isolated server tests covering enemy/party player and pet matrices, status eligibility, cooldown boundaries, cloud height/collision, Blink timings and actual summon attributes/AI.
- [x] Replace active/fixed copies, keep old archives ignored; regenerate raw notes, update player docs, rebuild and review handbook. Leave Git changes local.
