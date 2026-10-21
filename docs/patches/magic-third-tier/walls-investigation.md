# Wall investigation and integration

Exact inputs: installed Spell Engine 1.10.5, Wizards 3.1.2, RPG Skill Tree 1.6.1, Minecraft 26.2, NeoForge 26.2.0.90. Existing revision-five patch is retained.

## Findings

* `SpellCooldowns.imposeCooldown` applies a cooldown after every successful wall release. A counter on items would reset on equipment changes. The replacement stores one burst per player UUID, with a fixed deadline of first successful release +100 world ticks. The fourth cast closes immediately. `SpellCooldownManager.tickUpdate` closes partially used windows after its normal tick, requiring no further input. Cooldown duration (normal haste-adjusted 24 seconds) is captured on the first cast. Logout closes the window, and server stop clears remaining state. Optional normal hosting-item locks, including spellbook extra cooldown, are deferred until burst completion using the original source stack; null sources remain supported by the probe.
* `SpellCloud.getDimensions` uses radius times vertical multiplier while `TargetHelper.targetsFromArea` subsequently rejects targets by spherical distance. Merely increasing the multiplier cannot deliver a cylinder. The selected cloud lookup now uses entity bounding-box intersection with a horizontal circle and a ground-to-height interval, preserving spectator/pickable eligibility, obstacle visibility and downstream impact/party checks. Meteor Splash is `skill_tree_rpgs:fire_tier_3_spell_1_modifier_2`, not `wizards:fire_scorch`.
* Server cloud dimensions were not refreshed on creation when the radius tracker was initially assigned; the creation return now refreshes dimensions. This matters for both hitboxes and entity collision.
* Great Wall already has two extra placements. The existing cloud radius modifier is increased by .3, which native entity data synchronizes as radius1.2. Selected cloud height is fixed at2, or3 for Great Wall. The usual animation/particle/render code remains intact and uses the new dimensions.
* Minecraft EntityGetter.getEntityCollisions checks the moving entity canCollideWith, which calls the other entity canBeCollidedWith. The cloud override permits living movers only while the Great Wall is active. Its native bounding box supplies temporary collision; no world blocks are placed. isPickable and canBeHitByProjectile explicitly return false. Physical collision is a native square horizontal bounding box; damage uses the specified cylindrical volume.

## Build integration

Compile WallRules.java into the Engine JAR with release25. Compile PatchWalls.java with the existing ASM classpath; run it separately over SpellCooldowns, SpellCooldownManager, SpellCloud and SpellImpacts classes. The patcher checks exact edit counts. Call WallRules.init once during registration. Apply walls_data.transform(name, files) to Wizards/Skill Tree entry maps. It changes wall damage to .2, Great Wall radius modifier to+.3, and Healing Flames appended healing to .005, retaining root lifetime+2 and extra placements.

WallProbe.java is a compiled isolated-server fragment. local.audit.WallProbe.immediate(level, player, arenaOrigin) exercises four casts, dimensions, real LivingEntity.move collision, projectile exclusion and cylinder boundaries. startTimeout/beforeTimeout/afterTimeout require actual scheduling at0/99/100 ticks. Use a clear arena with no blocks intersecting the test path. The immediate method returns14 successful assertions; timeout checks add one each. A dead/casting player or inherited cooldown group can invalidate unrelated assertions; use the fresh test player.

## Checks performed here

JDK26 javac --release25 compiled WallRules, PatchWalls and WallProbe against installed pack and NeoForge classpath. PatchWalls successfully rewrote all four actual installed input classes in tmp/magic-wall, with expected edit counts. This is compilation/structural verification; runtime execution is delegated to the combined isolated server suite and must not be claimed from these checks alone.
