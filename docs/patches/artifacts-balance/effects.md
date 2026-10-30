# Artifacts — individual effects and adjustment reference

Inspected **2026-10-30**. Installed **Artifacts 15.1.3+26.2.1**, NeoForge / Minecraft 26.2. **45 wearables + 3 handheld/food items**, plus the separate Mimic Spawn Egg.

**Current pack reference:** the leading effect descriptions and **Current pack value** column describe the installed pack. The **Upstream default** column is retained for comparison. Unchanged settings remain configurable; pack-enforced overrides win over stale item configuration. The 30 October capped-balance revision is installed in the same-filename archive; see [the combined record](../accessory-rpg-balance/README.md).

## Reading the settings

- Configuration: `config/artifacts/items.toml`. Each item’s section and exact keys are listed below.
- **2 health points = 1 heart**; **2 hunger points = 1 drumstick**. Durations are **seconds** except explicitly named tick settings: **20 ticks = 1 second**.
- Chance **0.25 = 25%**. Base attribute bonus **0.3 = +30% of base**; final results interact with other modifiers. Flat additions retain raw units. Effect level **2 = II**.
- All 48 configurable items default to `generateAsLoot = true`.
- **Durability is disabled by default** where present: `canBeDamaged = false`, `canBeRepaired = false`, `indestructible = true`. Listed durability limits/costs are dormant until enabled.

## Contents

- [Handheld and food](#handheld-and-food)
- [Head](#head)
- [Necklace](#necklace)
- [Belt](#belt)
- [Hands](#hands)
- [Feet](#feet)
- [General curio](#general-curio)
- [Mimic Spawn Egg](#mimic-spawn-egg)
- [Important keywords](#important-keywords)
- [Sources](#sources)

## Handheld and food

### Umbrella

**Item:** `artifacts:umbrella` · **Section:** `[umbrella]`

Held **GLIDER**, **SHIELD**, and melee weapon. Gliding and shielding can be disabled separately.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `isGlider` | **true** | — |
| `isShield` | **true** | — |

**Optional durability (disabled)** — section `[umbrella.durability]`: `damagePerAttack` **1**; `damagePerBlockedAttackBase` **1**; `damagePerBlockedAttackFactor` **1.0**; `maxDamage` **672**.

### Everlasting Beef

**Item:** `artifacts:everlasting_beef` · **Section:** `[everlasting_beef]`

Reusable **RAW BEEF**, not consumed while enabled. **15-second cooldown**; qualifying cow/mooshroom player-kill drop chance **0.2% (1 in 500)**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **15** | — |
| `dropRate` | **0.002** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[everlasting_beef.durability]`: `damageWhenConsumed` **1**; `maxDamage` **320**.

### Eternal Steak

**Item:** `artifacts:eternal_steak` · **Section:** `[eternal_steak]`

Reusable **COOKED BEEF**, not consumed while enabled. **15-second cooldown**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **15** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[eternal_steak.durability]`: `damageWhenConsumed` **1**; `maxDamage` **320**.

## Head

### Plastic Drinking Hat

**Item:** `artifacts:plastic_drinking_hat` · **Section:** `[plastic_drinking_hat]`

**+150% drinking speed** and **+50% eating speed**. These are speed increases, not equivalent reductions in consumption time.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `drinkingSpeedBonus` | **1.5** | — |
| `eatingSpeedBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[plastic_drinking_hat.durability]`: `damagePerItemDrunk` **1**; `damagePerItemEaten` **1**; `maxDamage` **256**.

### Novelty Drinking Hat

**Item:** `artifacts:novelty_drinking_hat` · **Section:** `[novelty_drinking_hat]`

**+150% drinking speed** and **+50% eating speed**. Same default bonuses as Plastic Drinking Hat; extra flavor text is not another combat bonus.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `drinkingSpeedBonus` | **1.5** | — |
| `eatingSpeedBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[novelty_drinking_hat.durability]`: `damagePerItemDrunk` **1**; `damagePerItemEaten` **1**; `maxDamage` **256**.

### Snorkel

**Item:** `artifacts:snorkel` · **Section:** `[snorkel]`

**Water Breathing I for 30 seconds**, replenished while recovering air. **Infinite underwater breathing is disabled** by default.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `isInfinite` | **false** | — |
| `waterBreathingDuration` | **30** | — |

**Optional durability (disabled)** — section `[snorkel.durability]`: `damagePerSecondActive` **0.2**; `maxDamage` **900**.

### Night Vision Goggles

**Item:** `artifacts:night_vision_goggles` · **Section:** `[night_vision_goggles]`

Toggleable **NIGHT VISION**, reduced to brightness strength **0.15 (15%)**. This is visual strength, not activation chance.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `strength` | **0.15** | — |

**Optional durability (disabled)** — section `[night_vision_goggles.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Villager Hat

**Item:** `artifacts:villager_hat` · **Section:** `[villager_hat]`

**+75 villager reputation points**, affecting reputation-based trading. This is **not a guaranteed 75% discount**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `reputationBonus` | **75.0** | — |

**Optional durability (disabled)** — section `[villager_hat.durability]`: `damagePerTrade` **1**; `maxDamage` **240**.

### Superstitious Hat

**Item:** `artifacts:superstitious_hat` · **Section:** `[superstitious_hat]`

**+1 Looting level** for applicable drops, not generic Luck.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `lootingLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[superstitious_hat.durability]`: `damagePerKill` **1**; `maxDamage` **300**.

### Cowboy Hat

**Item:** `artifacts:cowboy_hat` · **Section:** `[cowboy_hat]`

**+40% base mount movement speed** while riding.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `mountSpeedBonus` | **0.4** | — |

**Optional durability (disabled)** — section `[cowboy_hat.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Angler's Hat

**Item:** `artifacts:anglers_hat` · **Section:** `[anglers_hat]`

**+1 Luck of the Sea** and **+1 Lure level** for fishing.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `luckOfTheSeaLevelBonus` | **1** | — |
| `lureLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[anglers_hat.durability]`: `damagePerItemFished` **1**; `maxDamage` **320**.

## Necklace

### Lucky Scarf

**Item:** `artifacts:lucky_scarf` · **Section:** `[lucky_scarf]`

**+1 Fortune level** for applicable block drops, not generic Luck.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `fortuneLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[lucky_scarf.durability]`: `damagePerOreMined` **1**; `maxDamage` **256**.

### Scarf of Invisibility

**Item:** `artifacts:scarf_of_invisibility` · **Section:** `[scarf_of_invisibility]`

Toggleable **INVISIBILITY**. Defaults do not hide its own model when invisible or hide effect particles.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `hideWhenInvisible` | **false** | — |
| `hidesEffectParticles` | **false** | — |

**Optional durability (disabled)** — section `[scarf_of_invisibility.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Cross Necklace

**Item:** `artifacts:cross_necklace` · **Section:** `[cross_necklace]`

Adds 10 invulnerability ticks (0.5s) after taking damage; native cooldown 5s.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `bonusInvincibilityTicks` | **20.0** | **10.0** |
| `cooldown` | **2** | **5** |

**Optional durability (disabled)** — section `[cross_necklace.durability]`: `damagePerActivation` **1**; `maxDamage` **500**.

### Panic Necklace

**Item:** `artifacts:panic_necklace` · **Section:** `[panic_necklace]`

After being hurt, grants **Speed I for5s**; **7s cooldown**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **2** | **7** |
| `speedDuration` | **8** | **5** |
| `speedLevel` | **1** | **1** |

**Optional durability (disabled)** — section `[panic_necklace.durability]`: `damagePerActivation` **1**; `maxDamage` **500**.

### Shock Pendant

**Item:** `artifacts:shock_pendant` · **Section:** `[shock_pendant]`

25% chance to retaliate against an eligible attacker under open sky. Visual lightning accompanies 2 damage plus 10% of strongest offensive power, capped at 6. 6s cooldown. Retains lightning immunity; no collateral lightning damage or fire.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cancelLightningDamage` | **true** | — |
| `cooldown` | **2** | **6** |
| `strikeChance` | **0.25** | — |

**Optional durability (disabled)** — section `[shock_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **50**.

### Flame Pendant

**Item:** `artifacts:flame_pendant` · **Section:** `[flame_pendant]`

40% chance to ignite an eligible attacker for 4s, granting the wearer Fire Resistance I for 4s. 4s cooldown; allies and accessory retaliation are excluded.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **2** | **4** |
| `fireDuration` | **10** | **4** |
| `grantFireResistance` | **true** | — |
| `strikeChance` | **0.4** | — |

**Optional durability (disabled)** — section `[flame_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **120**.

### Thorn Pendant

**Item:** `artifacts:thorn_pendant` · **Section:** `[thorn_pendant]`

50% chance to retaliate for 1 damage plus 10% wearer armor, capped at 4 damage. Fixed 4s interval; excludes allies and cannot trigger retaliation chains.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **2** | **4** |
| `maxDamage` | **6** | **3** |
| `minDamage` | **2** | **1** |
| `strikeChance` | **0.5** | — |

**Optional durability (disabled)** — section `[thorn_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **150**.

### Charm of Sinking

**Item:** `artifacts:charm_of_sinking` · **Section:** `[charm_of_sinking]`

Toggleable **SINKING** in water, **+1.5 oxygen bonus attribute**, and prevention of underwater fall damage. Oxygen is a raw attribute bonus, not a verified direct breath-duration multiplier.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `oxygenBonus` | **1.5** | — |
| `underwaterFallDamage` | **false** | — |

**Optional durability (disabled)** — section `[charm_of_sinking.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Charm of Shrinking

**Item:** `artifacts:charm_of_shrinking` · **Section:** `[charm_of_shrinking]`

Toggleable **75% original size**, **1.2x movement speed** and refreshing **Weakness II for60s** while active. Native Artifacts effect padding adds19ticks to the refreshed duration; unequipping uses native cleanup.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `scaleModifier` | **-0.5** | **-0.25** |
| Added movement multiplier (new behavior; no existing key) | None | **×1.2** |
| Added Weakness (new behavior; no existing key) | None | **II, 60 seconds, refreshed while active** |

**Optional durability (disabled)** — section `[charm_of_shrinking.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

## Belt

### Cloud in a Bottle

**Item:** `artifacts:cloud_in_a_bottle` · **Section:** `[cloud_in_a_bottle]`

**DOUBLE JUMP**, **+3 blocks safe fall distance**. Double jump resets accumulated fall distance; default multiplier **0.0** avoids damage for the fall accumulated before that jump. The subsequent fall still matters. Sprint double jump: **+25% vertical launch velocity**, **+0.25 horizontal velocity**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `fallDamageMultiplier` | **0.0** | — |
| `safeFallDistanceBonus` | **3.0** | — |
| `sprintJumpHorizontalVelocity` | **0.25** | — |
| `sprintJumpVerticalVelocity` | **0.25** | — |

**Optional durability (disabled)** — section `[cloud_in_a_bottle.durability]`: `damagePerDoubleJump` **1**; `maxDamage` **120**.

### Obsidian Skull

**Item:** `artifacts:obsidian_skull` · **Section:** `[obsidian_skull]`

After fire damage, grants **Fire Resistance II for30s** with a **60s cooldown**. Vanilla levelII gives no stronger fire protection than levelI; it does not retroactively cancel the triggering hit.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **60** | **60** |
| `fireResistanceDuration` | **30** | **30** |
| Effect level (item definition; no existing key) | **I** | **II** |

**Optional durability (disabled)** — section `[obsidian_skull.durability]`: `damagePerActivation` **1**; `maxDamage` **60**.

### Antidote Vessel

**Item:** `artifacts:antidote_vessel` · **Section:** `[antidote_vessel]`

Caps supported finite harmful-effect duration at **5 seconds**: **POISON**, **WITHER**, **WEAKNESS**, **SLOWNESS**, **BLINDNESS**, **NAUSEA**, **MINING FATIGUE**, **HUNGER**, **LEVITATION**. Internal cooldown **2 seconds**. Infinite effects are excluded. Cap **0** removes applicable finite effects.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `maxEffectDuration` | **5** | — |

**Optional durability (disabled)** — section `[antidote_vessel.durability]`: `damagePerActivation` **1**; `maxDamage` **100**.

### Universal Attractor

**Item:** `artifacts:universal_attractor` · **Section:** `[universal_attractor]`

Toggleable **Magnetism V** pulls dropped items. Search box extends **5 blocks in each axis**, capped at **10** at higher levels; not a spherical radius. Excludes pickup-delayed items and items marked as thrown by wearer.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `magnetismLevel` | **5** | — |

**Optional durability (disabled)** — section `[universal_attractor.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Crystal Heart

**Item:** `artifacts:crystal_heart` · **Section:** `[crystal_heart]`

Adds 10% of maximum HP before the three scaling health accessories and Health Boost, capped at +8 effective HP. No healing on equip or refresh; cannot amplify the other scaling health curios.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `healthBonus` | **10.0** | **Character scaling above replaces this legacy flat amount** |

**Optional durability (disabled)** — section `[crystal_heart.durability]`: `damageWhenHurt` **1**; `maxDamage` **600**.

### Helium Flamingo

**Item:** `artifacts:helium_flamingo` · **Section:** `[helium_flamingo]`

**SWIM THROUGH AIR for 8 seconds**, **15-second recharge duration**, **3-second cooldown**. Limited flight.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **3** | — |
| `flightDuration` | **8** | — |
| `rechargeDuration` | **15** | — |

**Optional durability (disabled)** — section `[helium_flamingo.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **800**.

### Chorus Totem

**Item:** `artifacts:chorus_totem` · **Section:** `[chorus_totem]`

Consumed death protection: clears effects, attempts a teleport within 32 blocks and restores 25% of current maximum HP, capped at 16 HP. Native rescue consumption and other effects remain.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `healthRestored` | **9** | **Character scaling above replaces this legacy flat amount** |

### Warp Drive

**Item:** `artifacts:warp_drive` · **Section:** `[warp_drive]`

Ender-pearl use costs **2 hunger points (1 drumstick)** instead of consuming the pearl when enough food is available; otherwise normal pearl consumption applies. **Pearl teleport damage canceled** by default; extra artifact cooldown **0 seconds**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **0** | — |
| `enabled` | **true** | — |
| `hungerCost` | **2** | — |
| `nullifyEnderPearlDamage` | **true** | — |

**Optional durability (disabled)** — section `[warp_drive.durability]`: `damagePerActivation` **1**; `maxDamage` **128**.

## Hands

### Digging Claws

**Item:** `artifacts:digging_claws` · **Section:** `[digging_claws]`

**+30% base block-breaking speed**, with a **stone-tier tool upgrade** for applicable mining. Does not grant diamond/netherite harvesting capability.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `blockBreakSpeedBonus` | **0.3** | — |
| `toolTier` | **"stone"** | — |

**Optional durability (disabled)** — section `[digging_claws.durability]`: `damagePerBlockHarvested` **1**; `damagePerBlockMined` **0**; `damagePerOreMined` **0**; `maxDamage` **500**.

### Feral Claws

**Item:** `artifacts:feral_claws` · **Section:** `[feral_claws]`

+10% base attack speed.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `attackSpeedBonus` | **0.3** | **0.1** |

**Optional durability (disabled)** — section `[feral_claws.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Power Glove

**Item:** `artifacts:power_glove` · **Section:** `[power_glove]`

Adds 5% of melee power before Power Glove and Woundbearer, capped at +2 effective attack damage. Recalculates as outside equipment and buffs change.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `attackDamageBonus` | **4.0** | **Character scaling above replaces this legacy flat amount** |

**Optional durability (disabled)** — section `[power_glove.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Fire Gauntlet

**Item:** `artifacts:fire_gauntlet` · **Section:** `[fire_gauntlet]`

+4 seconds attack-burning duration.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `fireDuration` | **8.0** | **4.0** |

**Optional durability (disabled)** — section `[fire_gauntlet.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Pocket Piston

**Item:** `artifacts:pocket_piston` · **Section:** `[pocket_piston]`

**+0.3 attack knockback**; this is a raw attribute amount, not30% damage.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `attackKnockbackBonus` | **0.75** | **0.3** |

**Optional durability (disabled)** — section `[pocket_piston.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Vampiric Glove

**Item:** `artifacts:vampiric_glove` · **Section:** `[vampiric_glove]`

Basic melee hits heal 5% of actual health damage, capped at 2 HP, once per 1s. Overkill and absorption do not increase healing; allies and accessory retaliation cannot trigger it. Healing-received modifiers remain native.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `absorptionChance` | **1.0** | — |
| `absorptionRatio` | **0.2** | **0.05** |
| `maxHealingPerHit` | **6** | **2** |

**Optional durability (disabled)** — section `[vampiric_glove.durability]`: `damagePerActivation` **1**; `maxDamage` **120**.

### Golden Hook

**Item:** `artifacts:golden_hook` · **Section:** `[golden_hook]`

**+50% entity experience** from applicable kills, not every XP source.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `entityExperienceBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[golden_hook.durability]`: `damagePerKill` **1**; `maxDamage` **300**.

### Onion Ring

**Item:** `artifacts:onion_ring` · **Section:** `[onion_ring]`

After eating: **Haste II**, **6 seconds per hunger point actually restored**. Hunger that could not be restored does not count.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `hasteDurationPerFoodPoint` | **6** | — |
| `hasteLevel` | **2** | — |

**Optional durability (disabled)** — section `[onion_ring.durability]`: `damagePerActivation` **1**; `maxDamage` **640**.

### Pickaxe Heater

**Item:** `artifacts:pickaxe_heater` · **Section:** `[pickaxe_heater]`

**AUTO-SMELTS** applicable mined ore drops with matching recipes. Not an automatic furnace for every block.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[pickaxe_heater.durability]`: `damagePerOreMined` **1**; `maxDamage` **256**.

### Withered Bracelet

**Item:** `artifacts:withered_bracelet` · **Section:** `[withered_bracelet]`

Melee hits have a 30% chance to apply Wither I for 4s. 4s cooldown shared by the wearer; excludes allies and accessory retaliation.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cooldown` | **2** | **4** |
| `witherChance` | **0.3** | — |
| `witherDuration` | **8** | **4** |
| `witherLevel` | **2** | **1** |

**Optional durability (disabled)** — section `[withered_bracelet.durability]`: `damagePerActivation` **1**; `maxDamage` **450**.

## Feet

### Aqua-Dashers

**Item:** `artifacts:aqua_dashers` · **Section:** `[aqua_dashers]`

Allows **SPRINTING ON WATER**, not lava.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[aqua_dashers.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **1800**.

### Bunny Hoppers

**Item:** `artifacts:bunny_hoppers` · **Section:** `[bunny_hoppers]`

**+40% base jump strength**, **+10 blocks safe fall distance**, rabbit hurt sounds. Here `fallDamageMultiplier = 0.0` is an additive base modifier: **no extra multiplier change**, not complete fall immunity.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `fallDamageMultiplier` | **0.0** | — |
| `jumpStrengthBonus` | **0.4** | — |
| `modifyHurtSounds` | **true** | — |
| `safeFallDistanceBonus` | **10.0** | — |

**Optional durability (disabled)** — section `[bunny_hoppers.durability]`: `damagePerFallSurvived` **1**; `damagePerJump` **0**; `maxDamage` **200**.

### Kitty Slippers

**Item:** `artifacts:kitty_slippers` · **Section:** `[kitty_slippers]`

**REPELS CREEPERS** and **PHANTOMS**, plus cat hurt sounds. Does not repel every hostile mob.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `modifyHurtSounds` | **true** | — |
| `repelCreepers` | **true** | — |
| `repelPhantoms` | **true** | — |

**Optional durability (disabled)** — section `[kitty_slippers.durability]`: `damagePerCreeperScared` **1**; `damagePerPhantomScared` **1**; `maxDamage` **120**.

### Running Shoes

**Item:** `artifacts:running_shoes` · **Section:** `[running_shoes]`

+15% base movement speed while sprinting; retains +0.5 block step height.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `sprintingSpeedBonus` | **0.4** | **0.15** |
| `sprintingStepHeightBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[running_shoes.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Snowshoes

**Item:** `artifacts:snowshoes` · **Section:** `[snowshoes]`

**+30% base movement speed on snow**, **WALK ON POWDER SNOW**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `allowWalkingOnPowderedSnow` | **true** | — |
| `movementSpeedOnSnowBonus` | **0.3** | — |

**Optional durability (disabled)** — section `[snowshoes.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Steadfast Spikes

**Item:** `artifacts:steadfast_spikes` · **Section:** `[steadfast_spikes]`

+30% knockback resistance and +1 slip resistance. Fits the restored Feet slot, separate from Nameless Shoes.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `knockbackResistance` | **1.0** | **0.3** |
| `slipperinessReduction` | **1.0** | **1.0** |

**Optional durability (disabled)** — section `[steadfast_spikes.durability]`: `damageWhenAttacked` **1**; `maxDamage` **800**.

### Flippers

**Item:** `artifacts:flippers` · **Section:** `[flippers]`

**+70% base swim speed**.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `swimSpeedBonus` | **0.7** | — |

**Optional durability (disabled)** — section `[flippers.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Rooted Boots

**Item:** `artifacts:rooted_boots` · **Section:** `[rooted_boots]`

While grounded on supported grass and hungry: **1 hunger point per 10-second interval**, with saturation. Eating while grounded on supported grass can **GROW PLANTS** using bone-meal behavior.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `growPlantsAfterEating` | **true** | — |
| `hungerReplenishingDuration` | **10** | — |

**Optional durability (disabled)** — section `[rooted_boots.durability]`: `damagePerFoodPoint` **1**; `maxDamage` **400**.

### Strider Shoes

**Item:** `artifacts:strider_shoes` · **Section:** `[strider_shoes]`

**WALK ON LAVA while sneaking**; prevents hot-floor damage such as magma blocks. Not blanket immunity to every fire/lava damage source.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `cancelHotFloorDamage` | **true** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[strider_shoes.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **1800**.

## General curio

### Whoopee Cushion

**Item:** `artifacts:whoopee_cushion` · **Section:** `[whoopee_cushion]`

**+0.12 (12 percentage points) FLATULENCE chance** at fart-trigger checks. Cosmetic sound behavior; no configured poison cloud, reflection, or stun.

| Setting | Upstream default | Current pack value |
| --- | --- | --- |
| `fartChance` | **0.12** | — |

**Optional durability (disabled)** — section `[whoopee_cushion.durability]`: `damagePerActivation` **1**; `maxDamage` **250**.

## Mimic Spawn Egg

**Item:** `artifacts:mimic_spawn_egg`. Spawns a **MIMIC**. Not a wearable trinket; no item-specific `items.toml` section.

## Important keywords

| Keyword / attribute | Relevant artifacts |
| --- | --- |
| **POISON / harmful effects** | Antidote Vessel shortens supported effects; no offensive poison item in these settings |
| **WITHER** | Withered Bracelet applies it; Antidote Vessel shortens it |
| **REFLECT / RETALIATION** | Thorn Pendant deals fixed random damage; Flame Pendant ignites; Shock Pendant calls lightning |
| **STUN** | No dedicated stun effect or stun-duration setting in these item configurations |
| **LIFE STEAL / HEALING / HEALTH** | Vampiric Glove, Chorus Totem, Crystal Heart |
| **ATTACK DAMAGE / ATTACK SPEED / KNOCKBACK** | Power Glove, Feral Claws, Pocket Piston |
| **KNOCKBACK RESISTANCE** | Steadfast Spikes |
| **FIRE / FIRE RESISTANCE** | Fire Gauntlet, Flame Pendant, Obsidian Skull, Strider Shoes |
| **FORTUNE / LOOTING / FISHING / EXPERIENCE** | Lucky Scarf, Superstitious Hat, Angler’s Hat, Golden Hook |

## Current capped balance

Accessories complement class skills. Life steal has a wearer-level 1s interval and a 2-HP base-heal cap. Retaliation uses bounded damage, protects party allies/pets, and cannot activate other retaliation or life steal. Native defensive item cooldowns remain; the wearer-level retaliation timer also prevents stack-swapping bypasses. Ordinary attribute modifiers retain their native operations; there is no extra blanket accessory multiplier.

Shock Pendant uses visual lightning and one attributed, bounded hit, avoiding collateral lightning damage and ignition. Withered Bracelet keeps its native melee trigger with a wearer-level 4s interval. Shrinking retains 75% size, movement and Weakness II. Slots, other utility items and optional durability remain intact. See [all 87 retained wearable decisions](../accessory-rpg-balance/inventory.md).

## Sources

- Implemented pack archive SHA-256: `4288b5b14ce810e38e89fc5016cf4b792c8be376c2e33cce22028944f6227f62`.

- Installed archive: `mods/artifacts-neoforge-15.1.3+26.2.1.jar`; inspected metadata, English names, tags, and compiled configuration definitions.
- Upstream SHA-256: `3be92116769cbba850acfc0e03bd0f3688c459e2b17144d97da36679fe35c104`.
- [Official project](https://www.curseforge.com/minecraft/mc-mods/artifacts). Matching upstream **26.2 / 15.1.3** snapshot: [803fd1aea1cece1362d40ba626c64cc1c3cdabb7](https://github.com/ochotonida/artifacts/tree/803fd1aea1cece1362d40ba626c64cc1c3cdabb7).
- [Configuration definitions](https://github.com/ochotonida/artifacts/blob/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/config/ItemConfigs.java), [item definitions](https://github.com/ochotonida/artifacts/blob/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/registry/ModItems.java), [ability implementations](https://github.com/ochotonida/artifacts/tree/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/component/ability).

The 30 October capped-balance archives passed 171 minimal and 181 RPG/Better Party isolated-server checks. These include shared regression checks; they are not 352 unique tests. No new client visual or comparative class playtest was performed. See the combined verification record.

21 October slot repair: the missing Artifacts Feet assignment is restored. Steadfast Spikes fits Feet (not Nameless Shoes). Existing Charm remains; adding Feet brings the current pack to 21 functional slots.
