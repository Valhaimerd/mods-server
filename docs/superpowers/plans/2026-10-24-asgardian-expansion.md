# Asgardian expansion implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement the full approved ASGARDIAN.md expansion on the existing Bifrost/Iourus integration, preserving world data and party protection.

**Architecture:** Keep `bifrost` registry IDs and marker SavedData. A single ownership service owns each bound weapon's inventory/in-flight/final-death state. Server-authoritative combat, input packets and transit share that service and the existing Asgardian identity gate. Client code owns only input/UI/presentation.

**Tech stack:** Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 bytecode, existing Iourus, Spell Engine and Better Party integrations. Same-version Bifrost filename retained.

**Spec:** `mod fixing/ASGARDIAN.md`; user explicitly approved the full expansion on 24 October 2026. The weapon numbers in that document are the initial implementation targets; runtime verification must not be described as completed play-balance testing.

## Global constraints

- Preserve unrelated dirty files; no commit/push requested. Work only in new patch sources, tested JAR replacement and corresponding documentation. Use disposable runtime instances.
- Server owns identity, ownership, cooldowns, targeting, damage and movement. No party/pet damage, terrain destruction or uncontrolled vanilla lightning.
- No duplicate bound weapons. Final death differs from downing; one persistent authoritative record survives unload/restart.
- Preserve original marker codec and namespace. Same-dimension travel only, original 60-tick charge and 40-tick arrival, downward sweeps 14/25 ticks.
- Ability key remains unbound. Normal melee remains available, air/sneak-left throws; right-use invokes the weapon primary; the ability selector includes Travel to retain destination access.
- Retain all stated per-target damage/target caps. Each weapon permits one action at a time; haste follows native spell haste, Realm Shift/Berserk cooldowns floor at 75% base.

## Shared interfaces

Package `com.emandev.bifrost`, source root `docs/patches/asgardian-expansion/src`.

- `AsgardCombat.normalDamage(ServerPlayer, ItemStack): double`: ordinary held fully charged raw melee damage snapshot, no spell-power double bonus.
- `AsgardCombat.eligible(ServerPlayer, LivingEntity): boolean`: party/pet/downed/PvP eligibility, no visibility requirement.
- `AsgardCombat.targets(ServerPlayer, Vec3 center, double radius, int limit): List<LivingEntity>`: eligible visible targets sorted by distance.
- `AsgardCombat.hitRaw(ServerPlayer, LivingEntity, double raw, boolean lightning): boolean`: native hurt pipeline; confirmed hit result, protection and scaling once.
- `WeaponOwnership.canUse(ServerPlayer, ItemStack): boolean`: ownership state eligibility (Hofund does not bind); actor/race checks remain in AsgardianAccess.
- `WeaponOwnership.isBusy(ServerPlayer, ItemStack): boolean`: in flight/waiting/non-inventory state.
- `WeaponOwnership.throwWeapon(ServerPlayer, ItemStack, boolean ricochet): boolean`: owns projectile state, return, hit registry and snapshot damage.
- `WeaponOwnership.tick(MinecraftServer): void`, `WeaponOwnership.register(IEventBus): void` if registry setup is needed; lifecycle/event handlers may subscribe directly.
- `WeaponOwnership.claim(ServerPlayer): boolean`: explicit legacy owner claim, never pickup binding.
- `AsgardAbilities.activate(ServerPlayer, int skill): boolean`, `beginPrimary(ServerPlayer): boolean`, `endPrimary(ServerPlayer): void`, `tick(MinecraftServer): void`, `describe(String weapon): List<String>`.
- `AsgardNetwork.send(int action, int skill): void` for client; actions 0 activate, 1 begin primary, 2 release primary, 3 default throw. Strict server validation and payload protocol `asgardian-expansion-v1`.
- `AsgardClient.register(IEventBus): void`: unbound ability selector, scroll/select/activate, vanilla air attack input and primary release events. No authoritative client damage.
- `AsgardWeapons.kind(ItemStack): String`: `hofund`, `mjolnir`, `stormbreaker` or empty. New `ModItems.STORMBREAKER` retains other native static field descriptors.

## Tasks

### 1. Ownership, throws and death recovery
- [x] Write failing native server checks for unclaimed use, owner-only possession, duplicate IDs, throws/returns and final-death persistence.
- [x] Implement one SavedData authority, crafting result binding, explicit claim/admin recovery, inventory/container/pickup/drop/hopper/grave guards, return and 3D ricochet.
- [x] Prove inventory/in-flight/waiting transitions, restart, unsafe death locations, downing vs death, full inventory and mixed-owner transactions. Record exact limits.

### 2. Transit passengers and presentation
- [x] Write failing tests for impact capture timing, late entrants, riders, safe destinations and cancellation.
- [x] Extend existing manager to 14/25-tick downward timing and impact-time one-shot source capture, bounded safe landing groups, exclusions and cleanup.
- [x] Add original translucent beam resource/presentation and original packaged phase audio with once-per-transition server broadcasts.
- [x] Prove timing, snapshots, riding relationships, no duplicate transit, unsafe/offline cases, and resource/runtime registration.

### 3. Combat, items and input
- [x] Write failing assertions for missing Stormbreaker/weapon attributes and ability coefficients/caps.
- [x] Add 8/1.5 Hofund, 9/1.2 Mjolnir and 10/1.0 Stormbreaker, recipe, unbreakability and item/input integration.
- [x] Implement all Hofund/Mjolnir/Stormbreaker skills and healing passives, shared recovery lockout and persistent cooldowns; enforce same party/PvP policy as installed spells.
- [x] Register input/network, selector and tooltips exposing name/range/cast/cooldown/failure. Package original models/textures and resources.
- [x] Run native server and connected client tests including actual hits, channel interruption, immunity/party/pets, ownership, cooldown/haste and UI.

### 4. Integration and delivery
- [x] Review each subsystem against the spec, fix findings, then perform a whole-feature review.
- [x] Build final same-filename replacement only after tests; archive predecessor ignored; compare final/fixed/tested hashes and no duplicate mod IDs/test leakage.
- [x] Synchronize ASGARDIAN.md, patch README, pack README, handbook entry/controls/indexes and PDF; render/inspect changed pages and validate all links/contents.
- [x] Report observed evidence and limitations; do not push.

## Review focus / rulings

- Ruling: use the existing workspace and disposable instances because the working Asgardian/travel integrations are uncommitted inputs; a clean HEAD worktree would omit them. No unrelated edits or commits.
- Ruling: default Stormbreaker throw follows the finite Mjolnir throw/return behavior; its selected ricochet uses the separate stated numbers/cooldown. Hofund has no thrown-sword behavior specified, so air attack remains normal unless a defined weapon throw exists.
- Ruling: Travel is selected from the ability UI; primary right-use is combat. Marker right-use still opens the destination interface. This preserves both capabilities without assigning conflicting use gestures.
- Ownership before result extraction, swap/cursor/creative copies, death during flight and revive/grave callback ordering require native-path tests, not helper-only tests.
- Damage W is the ordinary raw fully charged held melee attribute snapshot; defenses and racial/PvP multipliers apply once through native hurt. Do not apply armor twice.
- Source movement after charge starts must not move the capture beam; late entrants and destination beams cannot trigger a second capture.
- Haste/cooldown persistence, invalid packet/replay/channel cancellation and shared healing lockout must survive reconnect/restart and weapon swapping.
- Assets must be original or existing licensed resources; no copied movie assets. Any remaining visual/network/load limitations must be reported precisely.


Final local evidence: `docs/patches/asgardian-expansion/runtime-results.txt`. Manual integration review checked gates, damage limits, ownership transitions, native recipe extraction, persistence, optional revive paths, transit timing and resource/UI paths. Deferred play-balance and multiplayer timing limits are stated in the patch README. No push or commit.
