# Shared spell targeting and Better Party integration

Inspected the installed Spell Engine 1.10.5, Better Party 1.1.7 and Better Party: Tamed Mobs 1.0.4 bytecode/metadata, with CFR source cached in ignored `tmp/magic-party-audit`. The publisher describes the core's public integration API on [Better Party](https://modrinth.com/mod/better-party); the [Tamed Mobs official page](https://www.curseforge.com/minecraft/mc-mods/better-party-x-tamed-mobs) describes party pet protection. Exact behavior below comes from the installed archives, rather than assuming behavior from their filenames or newer online releases.

The core API is `dev.betterparty.api.v1.server.BetterPartyServerApi.arePartyMembers(MinecraftServer, UUID, UUID)`. It queries the server's `PartyManager` membership, so it does not require either member to be currently online. The client counterpart is `dev.betterparty.api.v1.client.BetterPartyClientApi.arePartyMembers(UUID, UUID)` and uses its synchronized party snapshot. That reference is isolated in a nested helper to avoid loading the client API on a dedicated server.

Tamed Mobs recognizes `OwnableEntity.getOwnerReference().getUUID()`. Its incoming damage listener normally protects one's own pets when its personal setting enables it, and party pets when party friendly fire is disabled. Spell targeting now protects own/party pets regardless of those optional damage toggles, as explicitly required for spells. The original Better Party and Tamed Mobs archives/listeners remain unchanged. An ownable spell caster is attributed to its owner UUID as well, protecting its owner's party and sibling pets.

## Root cause and shared boundary

Engine `EntityRelations.getRelation` has no Better Party integration. Its default passive relation can be FRIENDLY, while its area-harm permission table denies FRIENDLY targets and its helpful table can admit non-party entities. This explains both area spells missing passive entities and helpful targeting being too broad. The same function serves `SpellTarget`, projectile collision/homing, summon target selection and the final impact path.

`PartyRules.relation` returns ALLY for self, the same owner, Better Party members, and their ownable pets; all other targets are HOSTILE. Multipart targets are coalesced before checking. Existing direct/area intent tables then admit harm only outside that protected group and healing only within it. Configured passive/neutral relations no longer override the requested combat policy. Vanilla damage and event cancellation remain in place; no damage amount, resistance, player PvP permission or pack PvP multiplier is bypassed.

`SpellImpacts.performImpact` also receives a final guard before earlier local patches can schedule impacts. It resolves the effective caster from the impact context and rechecks membership each time, including the delayed Ice Lance invocation. Explicit `apply_to_caster` status effects and support actions remain valid (including self debuffs that accompany buffs); explicit DAMAGE and FIRE cannot injure the caster. Calling the ordinary shared `performImpacts` entry point automatically applies this guard for every class and delivery.

Public helper signatures available to other local spell delivery code:

- `relation(LivingEntity, Entity): EntityRelation`
- `allies(LivingEntity, Entity): boolean`
- `harmfulAllowed(LivingEntity, Entity): boolean`
- `impactAllowed(Level, LivingEntity, Entity, Spell.Impact, SpellExecution.ImpactContext): boolean`

## Frost and burn eligibility

The frost exceptions apply narrowly to `minecraft:slowness`, `wizards:frozen`, `wizards:frost_slowness`, and `skill_tree_rpgs:blizzard_slow`. Engine's status HP apply limit and its boss-tag movement/AI impairment gate are bypassed only for those effects. Other status conditions, durations, removal modes and unrelated immunities remain as before.

Vanilla 26.2 `forceAddEffect` still calls `canBeAffected`; it is insufficient for the requested immunity bypass. The helper first tries ordinary `addEffect`. If frost is rejected, it adds/merges the effect and invokes native `onEffectAdded`/`onEffectUpdated` callbacks, preserving effect attributes, synchronization and ordinary stronger/longer effect merging. These protected callbacks are resolved reflectively by their mapped names, with explicit failures if the expected 26.2 API changes. No `LivingEntity` class or global effect applicability is patched.

FIRE impacts establish remaining fire ticks regardless of entity-type ignition eligibility. Existing fire tick damage remains native and therefore respects Fire Resistance, intrinsic damage immunity and damage-event protections. This changes ignition eligibility; it does not promise damage to inherently fire-immune targets.

## Build and verification

Compile `PartyRules.java`, `PatchParty.java`, and optional `PartyProbe.java` with the installed mods plus the existing audit compile classpath, targeting Java 25. `PatchParty <input-engine.jar> <output-classes-directory>` reads but never mutates its input and emits patched `EntityRelations.class` and `SpellImpacts.class`. It asserts one relation replacement and exactly five impact patch sites. Package `PartyRules.class` plus **all its nested classes** from the output directory into the same Engine candidate. Do not ship PatchParty or PartyProbe.

The sources compile successfully with JDK 26.0.2 using `--release 25`. Patch-site assertions pass against the active same-filename revision-five Engine archive. No active JAR was edited by this investigation.

`PartyProbe.run(MinecraftServer, List<String>): int` is a helper for the integrated isolated-server probe. It seeds/restores a temporary party using the installed PartyManager, exercises both direct/area acquisition and actual final impacts, tests friendly-fire toggles in both states, self and offline-owner pet protection, wounded enemy healing rejection, allied pet healing, ordinary hostile/passive/neutral damage and burn eligibility, ownable caster attribution, and a 1,000-HP immune/boss-tag fixture receiving all four frost statuses. Runtime results belong in the integrated probe's report; compilation alone is not a runtime claim.
