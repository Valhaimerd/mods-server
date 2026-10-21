# Summon implementation and investigation

Installed targets: Wizards 3.1.2, Spell Engine 1.10.5, Skill Tree 1.6.1, Minecraft26.2 / NeoForge26.2.0.90. JAR filenames are retained. Source changes are layered over revision five.

## Root cause and chosen hooks

Upstream summon attribute scaling adds bonuses to configurable entity base attributes, ignores each modifier's operation, and merges skill bonuses additively. Therefore changing coefficients alone cannot produce exact Legion/Colossus multipliers or eliminate existing base melee/power. A post-summon hook removes the upstream scaling modifier for each specified stat and assigns the complete formula to the entity base. Effective owner Frost spell power, including power modifiers, is sampled at summon time. Existing other summon properties remain unchanged.

Frost Elemental exact base formulas are health30+.5P, armor5+.08P, scale1, Frost power.5P, melee.5P. Legion is exactly three at .8 of those formulas, including scale. Colossus is exactly one at2.5 and copies owner knockback resistance. Colossus takes precedence if both incompatible skill modifiers are supplied. Its attack-knockback and other unspecified properties are unchanged. Health starts full. An attribute-value override confined to the Frost Elemental subclass evaluates the normal modifier operations without the vanilla max-health/armor/attack cap, preventing the specified formulas being clipped; other mobs retain their normal caps.

Frost Shard is shared by upstream content. A summon-only `wizards:elemental_frost_shard` clone sets its damage coefficient to1. Ordinary Frost Shard is unchanged. Both summon cooldowns are30sec and haste affected; both have20 active sec, retaining the existing Improved +5sec modifiers.

Ancient Hydra replaces its +15sec modifier with a marker. Count/group-count overrides enforce exactly one Hydra even when the spawn-count upgrade is also present. Its scale is3, detection radius12, and only action is `wizards:ancient_hydra_breath`. This clone uses native Fire Breath sounds/particles/impacts,5sec/25deliveries, zero cooldown,20degree cone and12block reach without distance falloff. Native channel damage multiplies the configured coefficient by.2, so coefficient.25 gives exactly.05 of the Hydra's own Fire power per delivery. The custom channel hook rolls over100tick windows while preserving the4tick delivery cadence and avoids an AI goal restart gap. It stops when the native target leaves detection range/dies or the summon ceases being active.

The client Hydra model holds the final native firebreath pose during the giant's active cast. The mouth anchor was calculated from its complete model neck/head transform chain, at local side.05009654, height1.91098274, forward1.74546007 times entity scale. Both launch particles and server cone use that anchor. Ancient-only rotation tracks target bounding-box center from that same mouth. This avoids the upstream eye-height/launch-height mismatch, including close targets below a giant Hydra. Cone selection checks12block distance from mouth, +/-10degrees, relation policy and block line of sight. Target acquisition remains12blocks from the entity through the existing controller.

## Better Party compatibility

Inspected installed Better Party Tamed Mobs1.0.4 `TamedMobNetwork.collectLoadedPets` and `BetterPartyTamedMobs.protectPartyPet`. The HUD accepts every living `OwnableEntity` with non-null `getOwnerReference`; it does not require a `TamableAnimal`. Spell Engine `SummonedEntity` already implements `OwnableEntity` and synchronizes/saves its owner reference. The existing addon therefore recognizes Frost Elementals without changing either addon or inheritance. Its incoming player-to-pet protection consults BetterPartyServerApi personal protection, party membership and friendlyFire. Shared offensive party targeting is handled by the pack PartyRules integration.

## Build integration

Compile `SummonRules.java`, `PatchSummons.java`. Put `net/spell_engine/internals/SummonRules.class` into Spell Engine. Apply `java PatchSummons MODE INPUT OUTPUT` to these entries:

| Mode | JAR | Class |
|---|---|---|
| entity | engine | net/spell_engine/entity/SummonedEntity.class |
| impacts | engine | net/spell_engine/internals/impact/SpellImpacts.class |
| controller | engine | net/spell_engine/internals/casting/MobCastController.class |
| launch | engine | net/spell_engine/internals/delivery/LaunchGeometry.class |
| frost | wizards | net/wizards/entity/FrostElementalEntity.class |
| model | wizards | net/wizards/client/entity/FireHydraModel.class |

`summons_data.transform(name, files)` mutates an entry-to-bytes mapping in place for Wizards/Skill Tree. No startup registration is needed. Markers are unknown `magic_pack:*` attribute IDs with empty modifier arrays; upstream scaling safely skips unregistered IDs while preserving markers across merging/saving.

## Verification

Production helper, patch tool and `SummonProbe.java` compile to Java25. All six ASM modes match expected hook counts against active revision-five classes. `SummonProbe.run(ServerLevel, ServerPlayer, Consumer<String>)` provides integrated isolated-server checks for exact formulas, high-value cap bypass, pet owner reference, count precedence, cloned spell coefficients and Ancient reach/cone/channel fields. Runtime result evidence is recorded by the root integrated probe; compilation alone is not a runtime or client visual claim. No personal profile or external server was changed.

Additional verification fixtures now exercise the installed Better Party addon private loaded-pet collector on a real spawned Frost Elemental and round-trip its exact `PetSnapshotPayload.STREAM_CODEC` payload. The breath fixture advances and restores the isolated world's clock across200 synthetic ticks, invokes the production patched `MobCastController.tickChannel`, and checks actual cone selection, damage pipeline,25 deliveries per5sec window and an uninterrupted4tick cadence over two windows. These are deterministic pipeline tests, not a claim of observed full autonomous AI combat.

The built client `FireHydraModel.class` passes ASM `CheckClassAdapter.verify` with no errors and JVM `-Xverify:all` class loading/method linkage. This verifies bytecode structure without launching/initializing a client. Its rendered pose and particles remain subject to a client visual check.
