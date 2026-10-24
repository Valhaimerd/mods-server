# Iourus Races - attributes and benefits

Inspected **24 October 2026** from the installed `mods/iourusraces-1.1.0.jar`. This includes the pack's local Asgardian race. The filename says 1.1.0; embedded metadata declares **1.0.0**, Minecraft **26.2**, NeoForge **26.2.0.88+**. The pack itself requires NeoForge 26.2.0.90+. Updated for the installed race-balance revision; personal configuration files remain untouched.

## Reading the attributes

- **2 HP = 1 heart.** Health below is the racial maximum before equipment, skills and effects.
- Scale is uniform entity scale relative to a normal player. Movement percentages compare the racial base-speed contribution with a normal player; other attribute modifiers still apply.
- Health is implemented as an additive adjustment to the normal 20 HP. Movement and scale use base-relative attribute modifiers; these are not guaranteed multipliers of the final value after every other mod.
- `NONE` is the unselected state, not a ninth selectable race. It uses 20 HP, normal scale and normal movement.
- Verified against installed code and focused native server checks, including Better Combat timing and RPG power, plus a connected Elf falling test. This does not claim a complete all-race combat/visual playthrough.

## All eight races

| Race | HP / hearts | Scale | Movement | Benefits | Tradeoffs |
| --- | --- | --- | --- | --- | --- |
| Human | 20 / 10 | 1.00x | Normal | Standard attributes; no unique combat passive | No racial stat bonus |
| Orc | 24 / 12 | 1.25x | -10% | Starts at +2 melee/+1 ranged; at 40/30/20% HP: +3/4/5 melee and +1.5/2/2.5 ranged | Larger body; -10% attack and draw speed |
| Goblin | 16 / 8 | 0.75x | +15% | Small body, +0.15 jump strength, night vision | Lowest racial HP |
| Elf | 18 / 9 | 1.10x | +8% | +0.10 jump strength, synchronized slow falling and solid leaf collision | Lower HP; no racial projectile-damage bonus |
| Dwarf | 22 / 11 | 0.80x | -5% | +2 armor and +2.3 mining-efficiency attribute | Slower movement |
| Melfork | 20 / 10 | 1.00x | +10% | +1 water-movement efficiency; refills air in water; underwater night vision; +3 attack damage in water | -2 attack damage outside water |
| Draconic | 28 / 14 | 1.10x | -10% | Immunity to fire-tagged damage; reduced sinking in lava | Slower movement; no separate racial armor or general damage-reduction bonus |
| Asgardian | 22 / 11 | 1.10x | -5% | +2% qualifying melee/projectile/magic hit damage, direct lightning immunity; access to the separate Asgardian equipment system | +10% incoming magic damage; ordinary fire remains harmful |

## Human

No unique combat or utility ability is registered. Uses the normal 20 HP, movement and size. Human is the neutral baseline rather than a race with an additional hidden adaptation bonus.

## Orc

Racial damage bonuses follow current health and feed both normal weapon attributes and RPG school power. They refresh after health changes and immediately before a native attack. These are additive damage bonuses, not extra percentage hit multipliers.

| Current HP | Melee damage bonus | Ranged damage bonus |
| --- | --- | --- |
| Above 40%, including full health | +2 | +1 |
| At or below 40%, above 30% | +3 | +1.5 |
| At or below 30%, above 20% | +4 | +2 |
| At or below 20%, including 10% | +5 | +2.5 |

Attack speed and bow/crossbow draw speed are **90% of their values without the racial penalties**, including equipment bonuses. The starting damage bonus applies even at full health; 50% is not an activation requirement. The native attack-speed attribute drives Better Combat's attack timing and physical-melee RPG haste. Bonuses are named, removable modifiers; healing returns damage bonuses to the starting +2 melee/+1 ranged level; switching race removes them entirely. Low-health crimson particles remain at or below 50% HP.

The previous version wrote attack-damage base values directly. Existing Orcs carrying that old 4-10 base value are repaired once to normal base 1, retaining the former value in persistent backup data. A pre-update custom Orc base in the same range cannot be distinguished from old racial writes. Later custom base changes are preserved. See [migration and tests](README.md).

## Goblin

- Jump strength: **+0.15 ADD_VALUE**, a raw attribute increase rather than a 15% jump-height bonus.
- Night vision is implemented through the client lightmap; it does not require a Night Vision potion icon.
- 0.75x scale changes the body and collision dimensions; 16 HP is the main defensive tradeoff.
- No racial outgoing-damage bonus was found.

## Elf

- Movement speed: **+8% base**, increased from +5%.
- **No racial projectile-damage bonus.** The old 35% projectile amplification is removed entirely.
- Jump strength: **+0.10 ADD_VALUE**, not 10% extra jump height.
- **No racial night vision.** Goblin keeps its vision; Melfork keeps underwater-only vision.
- Slow falling: descending velocity is capped at **-0.20 blocks/tick**, fall distance is cleared and corrections are synchronized to the owning client. Horizontal movement and upward jumps are preserved. Creative flight, Elytra gliding, climbing and fluids are excluded.
- The old server-only velocity change failed a connected-client drop test (landing tick 48). The corrected same drop landed at tick 109 with full 18 HP; client network/prediction samples can briefly exceed the server cap.
- Retains solid leaf collision: Elf cannot pass through leaves. The hook covers tagged leaves and LeavesBlock subclasses, including untagged modded subclasses. Other player races retain this mod's leaf pass-through policy.
- No racial draw-speed or critical-chance bonus.

## Dwarf

- **+2.3 mining efficiency**, using `minecraft:mining_efficiency` with `ADD_VALUE`.
- This is neither +230% mining speed nor a universal 2.3x multiplier. The result depends on the normal tool/block mining calculation.
- **+2 armor** as a removable racial modifier. 22 HP and 0.80x scale; no additional toughness or general resistance bonus.

## Melfork

- Movement is +10% even outside water; water has a separate **+1 water-movement-efficiency** modifier.
- While `isInWater()` is true, air is set to maximum every server player tick and a transient **+3 attack-damage** modifier is applied.
- Outside water, the water strength modifier is replaced by **-2 attack damage**. This is an attribute penalty, not the Weakness potion effect.
- Underwater night vision is enabled by the client lightmap only while `isInWater()` is true.
- No drying-out timer or extra land damage was found.

## Draconic

- Incoming damage tagged `minecraft:is_fire` is canceled for Draconic server players. This includes ordinary fire/lava damage when tagged accordingly; an unrelated custom damage type is not covered merely because a spell looks fiery.
- While in lava and sinking faster than -0.10 blocks/tick, vertical speed is clamped to -0.10. This helps lava traversal; it is not a dedicated lava-walking or flight mechanic.
- 28 HP is the highest racial maximum. No extra armor, general resistance or melee-damage bonus was found.

## Asgardian

The pack-local eighth race retains its existing damage, lightning and magic-weakness policy; this revision changes size and removes its mining bonus.

- **+2% damage**, once per qualifying attributed hit: player melee, projectile-tagged damage, or recognized magic. A magic projectile does not receive two racial multipliers. Healing and the displayed school-power attributes are unchanged.
- **No mining-speed bonus.** The previous 2% block-breaking-speed modifier is removed and cleaned up on race application.
- **Direct lightning immunity**: both the lightning-strike event and lightning-bolt damage are canceled. This prevents direct strike ignition; unrelated fire is not blocked.
- **+10% magic damage taken**. Recognized magic includes `c:is_magic`, vanilla magic, indirect magic, dragon breath and Wither damage.
- Other defenses, party protection and the pack's PvP adjustment remain separate.

### Separate Asgardian_Mod / Bifrost benefits

These come from the installed **Bifrost expansion**, not free stats inside Iourus itself:

- Race-gated use of Hofund, Mjolnir and Stormbreaker, with weapon abilities and Bifrost travel.
- Mjolnir and Stormbreaker are owner-bound and unbreakable, with finite throwing/return and death-site retrieval rules.
- Combat abilities have their own requirements, cooldowns and party/pet protections; choosing Asgardian does not automatically give the equipment.
- Full current equipment/ability details: [Asgardian expansion](../asgardian-expansion/README.md). Local design reference: [ASGARDIAN.md](../../../mod%20fixing/ASGARDIAN.md).

## Selection and persistence

Race identity changes now remove old named racial attributes, immediately apply the new race, then synchronize it. Direct admin SavedData changes reconcile on the next server pre-tick. Melfork water/land attack bonuses clear immediately; client identity resets on logout. Repeated switches and connected water/vision transitions are tested. See [race-state cleanup](../race-state/README.md).

Eight races are selectable. Players without a race get the selection screen; ordinary players cannot freely replace an already selected race. `/race <type>` selects when none is set. Administrator `/race reset` clears the selection. Race data is copied on clone and its attributes are reapplied/synchronized on login and respawn. Equipment/skill health bonuses are separate from racial HP adjustments.

## Current revision and remaining implementation notes

The [race-balance patch](README.md) removes Elf projectile amplification, fixes connected slow falling, changes Elf movement/Goblin and Asgardian size, revises Orc attributes (including its full-health bonus and draw-speed follow-up), gives Dwarf armor and removes Asgardian mining speed. Other race abilities remain unchanged.

Dwarf's +2.3 mining efficiency is still a flat attribute bonus, not a percentage. Leaf collision is still removed for non-Elf players. This revision does not redesign those mechanics.

## Sources and archive identity

Authoritative inspection targets inside the installed Iourus archive:

- `race/Race.class`: scale, HP and movement for all nine enum states, including unselected NONE.
- `race/RaceAttributes.class`: health, speed, scale, jump, mining and water modifiers.
- `event/RaceAbilityHandler.class`: Orc, Elf projectile/falling, Melfork and Draconic behavior; registered by `IourusRaces.class`.
- `race/AsgardianRules.class`: retained damage/lightning policy and legacy mining cleanup.
- `race/RaceBalanceRules.class`: Orc scaling/speed/migration, Dwarf armor and connected Elf falling.
- `mixin/LightmapMixin.class` and `mixin/EntityCollisionContextMixin.class`: vision and leaves.
- `event/RaceCommandHandler.class`, `event/PlayerJoinHandler.class`, `client/ClientInputHandler.class`: selection and persistence.
- `assets/iourusraces/lang/en_us.json`: selector wording, compared against behavior rather than treated as proof of mechanics.

Decompiled inspection output is local/ignored under `tmp/iourus-reference/decompiled/`. Original local addition and its earlier runtime evidence: [Asgardian race patch](../asgardian-race/README.md).

Installed Iourus archive SHA-256: `{{IOURUS_SHA256}}`.
