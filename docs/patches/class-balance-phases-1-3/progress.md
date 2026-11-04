# Combined phases 1-3 execution ledger

Status: implemented, verified and installed on 4 November 2026. Later phases remain planned.
Inputs: pinned current archives in baseline.json. Outputs and changed members: artifacts.json.
Branch: work/combat-followup-phase-1. No commit/push. Unrelated World Map update preserved.

- [x] Phase 1: ordinary RPG magic uses armor; existing general Resistance/Pain Suppression/shield coverage verified without a duplicate layer. Original mage coefficients restored; Magistrate 15%; Nullstone retired.
- [x] Phase 2: three eight-node Defense paths, scoped migration, clean Utilities branching, compact labels/icons, full Fitness hunger/saturation, Multi Roll enchant contribution capped at two.
- [x] Phase 3: Rogue pressure/control/regeneration changes and Warrior projectile/armor/ultimate updates.
- [x] Final isolated dedicated server: Minecraft 26.2, NeoForge 26.2.0.90, Temurin 25.0.4.1+1. PASS 104 native checks.
- [x] Final connected integrated client: same versions and exact candidate bytes. PASS 13 checks, including synchronized equipment, Vanish/roll and actual Shadowstep teleport/root.
- [x] Resource checks: PASS 39. Original resource fixture failed 38; original native fixture failed its armor-bypass check.
- [x] Nine active and fixed-pack replacements match the tested hashes. Original copies preserved in ignored retired packs.
- [x] Current handbook, root README, wearable inventory and raw-reference notices synchronized. PDF rebuilt; relevant pages and actual tree captures visually reviewed.

Rulings and findings:
- User selected natural regeneration and Regeneration only for Coated Blades. Direct healing and absorption remain available.
- Existing general Resistance and activated frontal shields already cover magic. Preserve their single layers.
- Nullstone follows the established safe retirement policy: inert saved ID, disabled acquisition/config/callback; unrelated trinkets unchanged. No external pack recipes/loot tables reference it.
- Defense Resistance uses additive base modifiers, verified as exact 4/2/8 percent full-path totals. Healing-received rewards do not increase outgoing Healing Power.
- Native channels originally offset deliveries by half an interval. Warrior-only scheduling now delivers at .8/1.6/2.4/3.2/4 seconds before haste.
- Vanilla maximum health caps at 1024; the Last Stand test uses an ordinary 100-HP fixture to test percentage growth without hitting that cap.
- Forge FakePlayer refuses PvP by default and does not perform ordinary equipment ticks. PvP fixtures explicitly permit combat; actual connected-player equipment confirms Multi Roll.
- Isolated superflat setup and platform-library logging warnings also occur in the baseline. No new startup blocker or test failure remains.

Boundaries: automated isolated combat/UI verification, not a live multiplayer load test. Personal worlds, the personal Minecraft installation and the separate desktop server were not modified. No test probes ship.
