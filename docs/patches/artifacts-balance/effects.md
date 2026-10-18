# Artifacts — individual effects and adjustment reference

Inspected **2026-10-18**. Installed **Artifacts 15.1.3+26.2.1**, NeoForge / Minecraft 26.2. **45 wearables + 3 handheld/food items**, plus the separate Mimic Spawn Egg.

**Implemented 2026-10-18:** the approved values below are installed in the pack JAR. The **Default** column preserves upstream values; **Planned value** now records the implemented pack value for approved entries. Editing this document alone does not change the game. See `docs/patches/artifacts-balance/README.md` for the implementation and verification.

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

| Setting | Default | Planned value |
| --- | --- | --- |
| `isGlider` | **true** | — |
| `isShield` | **true** | — |

**Optional durability (disabled)** — section `[umbrella.durability]`: `damagePerAttack` **1**; `damagePerBlockedAttackBase` **1**; `damagePerBlockedAttackFactor` **1.0**; `maxDamage` **672**.

### Everlasting Beef

**Item:** `artifacts:everlasting_beef` · **Section:** `[everlasting_beef]`

Reusable **RAW BEEF**, not consumed while enabled. **15-second cooldown**; qualifying cow/mooshroom player-kill drop chance **0.2% (1 in 500)**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **15** | — |
| `dropRate` | **0.002** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[everlasting_beef.durability]`: `damageWhenConsumed` **1**; `maxDamage` **320**.

### Eternal Steak

**Item:** `artifacts:eternal_steak` · **Section:** `[eternal_steak]`

Reusable **COOKED BEEF**, not consumed while enabled. **15-second cooldown**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **15** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[eternal_steak.durability]`: `damageWhenConsumed` **1**; `maxDamage` **320**.

## Head

### Plastic Drinking Hat

**Item:** `artifacts:plastic_drinking_hat` · **Section:** `[plastic_drinking_hat]`

**+150% drinking speed** and **+50% eating speed**. These are speed increases, not equivalent reductions in consumption time.

| Setting | Default | Planned value |
| --- | --- | --- |
| `drinkingSpeedBonus` | **1.5** | — |
| `eatingSpeedBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[plastic_drinking_hat.durability]`: `damagePerItemDrunk` **1**; `damagePerItemEaten` **1**; `maxDamage` **256**.

### Novelty Drinking Hat

**Item:** `artifacts:novelty_drinking_hat` · **Section:** `[novelty_drinking_hat]`

**+150% drinking speed** and **+50% eating speed**. Same default bonuses as Plastic Drinking Hat; extra flavor text is not another combat bonus.

| Setting | Default | Planned value |
| --- | --- | --- |
| `drinkingSpeedBonus` | **1.5** | — |
| `eatingSpeedBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[novelty_drinking_hat.durability]`: `damagePerItemDrunk` **1**; `damagePerItemEaten` **1**; `maxDamage` **256**.

### Snorkel

**Item:** `artifacts:snorkel` · **Section:** `[snorkel]`

**Water Breathing I for 30 seconds**, replenished while recovering air. **Infinite underwater breathing is disabled** by default.

| Setting | Default | Planned value |
| --- | --- | --- |
| `isInfinite` | **false** | — |
| `waterBreathingDuration` | **30** | — |

**Optional durability (disabled)** — section `[snorkel.durability]`: `damagePerSecondActive` **0.2**; `maxDamage` **900**.

### Night Vision Goggles

**Item:** `artifacts:night_vision_goggles` · **Section:** `[night_vision_goggles]`

Toggleable **NIGHT VISION**, reduced to brightness strength **0.15 (15%)**. This is visual strength, not activation chance.

| Setting | Default | Planned value |
| --- | --- | --- |
| `strength` | **0.15** | — |

**Optional durability (disabled)** — section `[night_vision_goggles.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Villager Hat

**Item:** `artifacts:villager_hat` · **Section:** `[villager_hat]`

**+75 villager reputation points**, affecting reputation-based trading. This is **not a guaranteed 75% discount**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `reputationBonus` | **75.0** | — |

**Optional durability (disabled)** — section `[villager_hat.durability]`: `damagePerTrade` **1**; `maxDamage` **240**.

### Superstitious Hat

**Item:** `artifacts:superstitious_hat` · **Section:** `[superstitious_hat]`

**+1 Looting level** for applicable drops, not generic Luck.

| Setting | Default | Planned value |
| --- | --- | --- |
| `lootingLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[superstitious_hat.durability]`: `damagePerKill` **1**; `maxDamage` **300**.

### Cowboy Hat

**Item:** `artifacts:cowboy_hat` · **Section:** `[cowboy_hat]`

**+40% base mount movement speed** while riding.

| Setting | Default | Planned value |
| --- | --- | --- |
| `mountSpeedBonus` | **0.4** | — |

**Optional durability (disabled)** — section `[cowboy_hat.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Angler's Hat

**Item:** `artifacts:anglers_hat` · **Section:** `[anglers_hat]`

**+1 Luck of the Sea** and **+1 Lure level** for fishing.

| Setting | Default | Planned value |
| --- | --- | --- |
| `luckOfTheSeaLevelBonus` | **1** | — |
| `lureLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[anglers_hat.durability]`: `damagePerItemFished` **1**; `maxDamage` **320**.

## Necklace

### Lucky Scarf

**Item:** `artifacts:lucky_scarf` · **Section:** `[lucky_scarf]`

**+1 Fortune level** for applicable block drops, not generic Luck.

| Setting | Default | Planned value |
| --- | --- | --- |
| `fortuneLevelBonus` | **1** | — |

**Optional durability (disabled)** — section `[lucky_scarf.durability]`: `damagePerOreMined` **1**; `maxDamage` **256**.

### Scarf of Invisibility

**Item:** `artifacts:scarf_of_invisibility` · **Section:** `[scarf_of_invisibility]`

Toggleable **INVISIBILITY**. Defaults do not hide its own model when invisible or hide effect particles.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `hideWhenInvisible` | **false** | — |
| `hidesEffectParticles` | **false** | — |

**Optional durability (disabled)** — section `[scarf_of_invisibility.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Cross Necklace

**Item:** `artifacts:cross_necklace` · **Section:** `[cross_necklace]`

After being hurt: **+20 invulnerability ticks (1 second)**; **2-second cooldown**. Temporary protection after damage.

**Planned:** keep the extra **20 invulnerability ticks**; increase cooldown to **5 seconds**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `bonusInvincibilityTicks` | **20.0** | — |
| `cooldown` | **2** | **5 seconds** |

**Optional durability (disabled)** — section `[cross_necklace.durability]`: `damagePerActivation` **1**; `maxDamage` **500**.

### Panic Necklace

**Item:** `artifacts:panic_necklace` · **Section:** `[panic_necklace]`

After being hurt: **Speed I for 8 seconds**; **2-second cooldown**.

**Planned:** **Speed I for 5 seconds**, with a **7-second cooldown**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **2** | **7 seconds** |
| `speedDuration` | **8** | **5 seconds** |
| `speedLevel` | **1** | **1 (Speed I)** |

**Optional durability (disabled)** — section `[panic_necklace.durability]`: `damagePerActivation` **1**; `maxDamage` **500**.

### Shock Pendant

**Item:** `artifacts:shock_pendant` · **Section:** `[shock_pendant]`

When attacked: **25% chance** of **LIGHTNING RETALIATION**, **2-second cooldown**. Attacker must have sky access. Wearer receives lightning-damage immunity by default. No separate **STUN** setting.

**Planned:** **3-second cooldown**; existing strike chance and lightning immunity retained.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cancelLightningDamage` | **true** | — |
| `cooldown` | **2** | **3 seconds** |
| `strikeChance` | **0.25** | — |

**Optional durability (disabled)** — section `[shock_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **50**.

### Flame Pendant

**Item:** `artifacts:flame_pendant` · **Section:** `[flame_pendant]`

When attacked: **40% chance** to **IGNITE** attacker for **10 seconds**, **2-second cooldown**. Successful applicable ignition also gives wearer **Fire Resistance I for 10 seconds**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **2** | — |
| `fireDuration` | **10** | — |
| `grantFireResistance` | **true** | — |
| `strikeChance` | **0.4** | — |

**Optional durability (disabled)** — section `[flame_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **120**.

### Thorn Pendant

**Item:** `artifacts:thorn_pendant` · **Section:** `[thorn_pendant]`

When attacked: **50% chance** to deal **2–6 damage (1–3 hearts)** to attacker, **2-second cooldown**. Fixed random **THORNS / RETALIATION**, not percentage **REFLECTION** of the incoming damage.

**Planned:** **3-second cooldown**; existing retaliation damage and chance retained.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **2** | **3 seconds** |
| `maxDamage` | **6** | — |
| `minDamage` | **2** | — |
| `strikeChance` | **0.5** | — |

**Optional durability (disabled)** — section `[thorn_pendant.durability]`: `damagePerActivation` **1**; `maxDamage` **150**.

### Charm of Sinking

**Item:** `artifacts:charm_of_sinking` · **Section:** `[charm_of_sinking]`

Toggleable **SINKING** in water, **+1.5 oxygen bonus attribute**, and prevention of underwater fall damage. Oxygen is a raw attribute bonus, not a verified direct breath-duration multiplier.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `oxygenBonus` | **1.5** | — |
| `underwaterFallDamage` | **false** | — |

**Optional durability (disabled)** — section `[charm_of_sinking.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Charm of Shrinking

**Item:** `artifacts:charm_of_shrinking` · **Section:** `[charm_of_shrinking]`

Toggleable **SIZE REDUCTION −50%**, a **×0.5 total scale multiplier**.

**Planned:** retain **half size**; add **×1.2 movement speed (+20%)** and **Weakness I for 60 seconds**, refreshed while the shrinking ability is active. Stop refreshing when inactive; use native Artifacts unequip cleanup. Effects include the native **19-tick refresh padding**. Implemented through item data components: the installed item config exposes only scale, not speed or Weakness.

| Setting | Default | Planned value |
| --- | --- | --- |
| `scaleModifier` | **-0.5** | **-0.5 (retain)** |
| Added movement multiplier (new behavior; no existing key) | None | **×1.2** |
| Added Weakness (new behavior; no existing key) | None | **I, 60 seconds, refreshed while active** |

**Optional durability (disabled)** — section `[charm_of_shrinking.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

## Belt

### Cloud in a Bottle

**Item:** `artifacts:cloud_in_a_bottle` · **Section:** `[cloud_in_a_bottle]`

**DOUBLE JUMP**, **+3 blocks safe fall distance**. Double jump resets accumulated fall distance; default multiplier **0.0** avoids damage for the fall accumulated before that jump. The subsequent fall still matters. Sprint double jump: **+25% vertical launch velocity**, **+0.25 horizontal velocity**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `fallDamageMultiplier` | **0.0** | — |
| `safeFallDistanceBonus` | **3.0** | — |
| `sprintJumpHorizontalVelocity` | **0.25** | — |
| `sprintJumpVerticalVelocity` | **0.25** | — |

**Optional durability (disabled)** — section `[cloud_in_a_bottle.durability]`: `damagePerDoubleJump` **1**; `maxDamage` **120**.

### Obsidian Skull

**Item:** `artifacts:obsidian_skull` · **Section:** `[obsidian_skull]`

After fire damage: **Fire Resistance I for 30 seconds**, **60-second cooldown**. Reactive protection, not permanent immunity to the triggering hit.

**Planned:** **Fire Resistance II**, keeping **30-second duration** and **60-second cooldown**. Interpreted as effect level II, not a 2-second duration. The level is fixed in the item definition, so it was changed in the item definition; there is no effect-level key in this config. Higher Fire Resistance levels do not provide stronger vanilla fire protection.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **60** | — |
| `fireResistanceDuration` | **30** | **30 (retain)** |
| Effect level (item definition; no existing key) | **I** | **II** |

**Optional durability (disabled)** — section `[obsidian_skull.durability]`: `damagePerActivation` **1**; `maxDamage` **60**.

### Antidote Vessel

**Item:** `artifacts:antidote_vessel` · **Section:** `[antidote_vessel]`

Caps supported finite harmful-effect duration at **5 seconds**: **POISON**, **WITHER**, **WEAKNESS**, **SLOWNESS**, **BLINDNESS**, **NAUSEA**, **MINING FATIGUE**, **HUNGER**, **LEVITATION**. Internal cooldown **2 seconds**. Infinite effects are excluded. Cap **0** removes applicable finite effects.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `maxEffectDuration` | **5** | — |

**Optional durability (disabled)** — section `[antidote_vessel.durability]`: `damagePerActivation` **1**; `maxDamage` **100**.

### Universal Attractor

**Item:** `artifacts:universal_attractor` · **Section:** `[universal_attractor]`

Toggleable **Magnetism V** pulls dropped items. Search box extends **5 blocks in each axis**, capped at **10** at higher levels; not a spherical radius. Excludes pickup-delayed items and items marked as thrown by wearer.

| Setting | Default | Planned value |
| --- | --- | --- |
| `magnetismLevel` | **5** | — |

**Optional durability (disabled)** — section `[universal_attractor.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Crystal Heart

**Item:** `artifacts:crystal_heart` · **Section:** `[crystal_heart]`

**+10 maximum health = +5 hearts**. Increases capacity, not an instant heal.

**Planned:** **+8 maximum health = +4 hearts**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `healthBonus` | **10.0** | **8.0 (+4 hearts)** |

**Optional durability (disabled)** — section `[crystal_heart.durability]`: `damageWhenHurt` **1**; `maxDamage` **600**.

### Helium Flamingo

**Item:** `artifacts:helium_flamingo` · **Section:** `[helium_flamingo]`

**SWIM THROUGH AIR for 8 seconds**, **15-second recharge duration**, **3-second cooldown**. Limited flight.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **3** | — |
| `flightDuration` | **8** | — |
| `rechargeDuration` | **15** | — |

**Optional durability (disabled)** — section `[helium_flamingo.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **800**.

### Chorus Totem

**Item:** `artifacts:chorus_totem` · **Section:** `[chorus_totem]`

Equipped **DEATH PROTECTION**: clears status effects, attempts random **TELEPORT within a 32-block range**, and applies **9 health points (4.5 hearts) of healing**. Uses the death-protection item mechanism, not timed regeneration.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `healthRestored` | **9** | — |

### Warp Drive

**Item:** `artifacts:warp_drive` · **Section:** `[warp_drive]`

Ender-pearl use costs **2 hunger points (1 drumstick)** instead of consuming the pearl when enough food is available; otherwise normal pearl consumption applies. **Pearl teleport damage canceled** by default; extra artifact cooldown **0 seconds**.

| Setting | Default | Planned value |
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

| Setting | Default | Planned value |
| --- | --- | --- |
| `blockBreakSpeedBonus` | **0.3** | — |
| `toolTier` | **"stone"** | — |

**Optional durability (disabled)** — section `[digging_claws.durability]`: `damagePerBlockHarvested` **1**; `damagePerBlockMined` **0**; `damagePerOreMined` **0**; `maxDamage` **500**.

### Feral Claws

**Item:** `artifacts:feral_claws` · **Section:** `[feral_claws]`

**+30% base attack speed**.

**Planned:** **+15% base attack speed**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `attackSpeedBonus` | **0.3** | **0.15 (+15% base)** |

**Optional durability (disabled)** — section `[feral_claws.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Power Glove

**Item:** `artifacts:power_glove` · **Section:** `[power_glove]`

**+4 flat attack damage**, **2 hearts nominal damage before mitigation**.

**Planned:** **+2 flat attack damage**, equivalent to **1 heart of nominal damage before mitigation**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `attackDamageBonus` | **4.0** | **2.0 (+2 damage)** |

**Optional durability (disabled)** — section `[power_glove.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Fire Gauntlet

**Item:** `artifacts:fire_gauntlet` · **Section:** `[fire_gauntlet]`

**+8 seconds BURNING duration** through the attack-burning-duration attribute.

| Setting | Default | Planned value |
| --- | --- | --- |
| `fireDuration` | **8.0** | — |

**Optional durability (disabled)** — section `[fire_gauntlet.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Pocket Piston

**Item:** `artifacts:pocket_piston` · **Section:** `[pocket_piston]`

**+0.75 raw attack knockback**, not a damage percentage.

**Planned:** **+0.3 raw attack knockback**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `attackKnockbackBonus` | **0.75** | **0.3** |

**Optional durability (disabled)** — section `[pocket_piston.durability]`: `damagePerAttack` **1**; `maxDamage` **1500**.

### Vampiric Glove

**Item:** `artifacts:vampiric_glove` · **Section:** `[vampiric_glove]`

Melee **LIFE STEAL**: **100% chance**, heals **20% of damage dealt**, maximum **6 health points (3 hearts) per hit**. Calculation caps damage at victim’s remaining health. Restores normal health, not yellow absorption hearts.

**Planned:** **10% life steal**, healing at most **4 health points (2 hearts) per melee hit**. Retain **100% activation chance**. No additional healing cooldown is approved.

| Setting | Default | Planned value |
| --- | --- | --- |
| `absorptionChance` | **1.0** | — |
| `absorptionRatio` | **0.2** | **0.1 (10%)** |
| `maxHealingPerHit` | **6** | **4 (2 hearts)** |

**Optional durability (disabled)** — section `[vampiric_glove.durability]`: `damagePerActivation` **1**; `maxDamage` **120**.

### Golden Hook

**Item:** `artifacts:golden_hook` · **Section:** `[golden_hook]`

**+50% entity experience** from applicable kills, not every XP source.

| Setting | Default | Planned value |
| --- | --- | --- |
| `entityExperienceBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[golden_hook.durability]`: `damagePerKill` **1**; `maxDamage` **300**.

### Onion Ring

**Item:** `artifacts:onion_ring` · **Section:** `[onion_ring]`

After eating: **Haste II**, **6 seconds per hunger point actually restored**. Hunger that could not be restored does not count.

| Setting | Default | Planned value |
| --- | --- | --- |
| `hasteDurationPerFoodPoint` | **6** | — |
| `hasteLevel` | **2** | — |

**Optional durability (disabled)** — section `[onion_ring.durability]`: `damagePerActivation` **1**; `maxDamage` **640**.

### Pickaxe Heater

**Item:** `artifacts:pickaxe_heater` · **Section:** `[pickaxe_heater]`

**AUTO-SMELTS** applicable mined ore drops with matching recipes. Not an automatic furnace for every block.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[pickaxe_heater.durability]`: `damagePerOreMined` **1**; `maxDamage` **256**.

### Withered Bracelet

**Item:** `artifacts:withered_bracelet` · **Section:** `[withered_bracelet]`

Attacks: **30% chance** of **Wither II for 8 seconds**, **2-second cooldown**. **WITHER**, not poison.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cooldown` | **2** | — |
| `witherChance` | **0.3** | — |
| `witherDuration` | **8** | — |
| `witherLevel` | **2** | — |

**Optional durability (disabled)** — section `[withered_bracelet.durability]`: `damagePerActivation` **1**; `maxDamage` **450**.

## Feet

### Aqua-Dashers

**Item:** `artifacts:aqua_dashers` · **Section:** `[aqua_dashers]`

Allows **SPRINTING ON WATER**, not lava.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[aqua_dashers.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **1800**.

### Bunny Hoppers

**Item:** `artifacts:bunny_hoppers` · **Section:** `[bunny_hoppers]`

**+40% base jump strength**, **+10 blocks safe fall distance**, rabbit hurt sounds. Here `fallDamageMultiplier = 0.0` is an additive base modifier: **no extra multiplier change**, not complete fall immunity.

| Setting | Default | Planned value |
| --- | --- | --- |
| `fallDamageMultiplier` | **0.0** | — |
| `jumpStrengthBonus` | **0.4** | — |
| `modifyHurtSounds` | **true** | — |
| `safeFallDistanceBonus` | **10.0** | — |

**Optional durability (disabled)** — section `[bunny_hoppers.durability]`: `damagePerFallSurvived` **1**; `damagePerJump` **0**; `maxDamage` **200**.

### Kitty Slippers

**Item:** `artifacts:kitty_slippers` · **Section:** `[kitty_slippers]`

**REPELS CREEPERS** and **PHANTOMS**, plus cat hurt sounds. Does not repel every hostile mob.

| Setting | Default | Planned value |
| --- | --- | --- |
| `modifyHurtSounds` | **true** | — |
| `repelCreepers` | **true** | — |
| `repelPhantoms` | **true** | — |

**Optional durability (disabled)** — section `[kitty_slippers.durability]`: `damagePerCreeperScared` **1**; `damagePerPhantomScared` **1**; `maxDamage` **120**.

### Running Shoes

**Item:** `artifacts:running_shoes` · **Section:** `[running_shoes]`

While sprinting: **+40% base movement speed**, **+0.5 block step height**.

**Planned:** **+20% base movement speed while sprinting**; retain **+0.5 block step height**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `sprintingSpeedBonus` | **0.4** | **0.2 (+20% base)** |
| `sprintingStepHeightBonus` | **0.5** | — |

**Optional durability (disabled)** — section `[running_shoes.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Snowshoes

**Item:** `artifacts:snowshoes` · **Section:** `[snowshoes]`

**+30% base movement speed on snow**, **WALK ON POWDER SNOW**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `allowWalkingOnPowderedSnow` | **true** | — |
| `movementSpeedOnSnowBonus` | **0.3** | — |

**Optional durability (disabled)** — section `[snowshoes.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **1800**.

### Steadfast Spikes

**Item:** `artifacts:steadfast_spikes` · **Section:** `[steadfast_spikes]`

**+1.0 knockback resistance**, **+1.0 slip resistance**. Normal knockback-resistance scale **0–1**: **1.0 = full resistance**. Default slip resistance removes extra ice slipperiness, not incoming damage.

**Planned:** **+0.5 knockback resistance (50%)**; retain **+1.0 slip resistance**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `knockbackResistance` | **1.0** | **0.5 (50%)** |
| `slipperinessReduction` | **1.0** | — |

**Optional durability (disabled)** — section `[steadfast_spikes.durability]`: `damageWhenAttacked` **1**; `maxDamage` **800**.

### Flippers

**Item:** `artifacts:flippers` · **Section:** `[flippers]`

**+70% base swim speed**.

| Setting | Default | Planned value |
| --- | --- | --- |
| `swimSpeedBonus` | **0.7** | — |

**Optional durability (disabled)** — section `[flippers.durability]`: `damagePerSecondActive` **0.1**; `maxDamage` **900**.

### Rooted Boots

**Item:** `artifacts:rooted_boots` · **Section:** `[rooted_boots]`

While grounded on supported grass and hungry: **1 hunger point per 10-second interval**, with saturation. Eating while grounded on supported grass can **GROW PLANTS** using bone-meal behavior.

| Setting | Default | Planned value |
| --- | --- | --- |
| `enabled` | **true** | — |
| `growPlantsAfterEating` | **true** | — |
| `hungerReplenishingDuration` | **10** | — |

**Optional durability (disabled)** — section `[rooted_boots.durability]`: `damagePerFoodPoint` **1**; `maxDamage` **400**.

### Strider Shoes

**Item:** `artifacts:strider_shoes` · **Section:** `[strider_shoes]`

**WALK ON LAVA while sneaking**; prevents hot-floor damage such as magma blocks. Not blanket immunity to every fire/lava damage source.

| Setting | Default | Planned value |
| --- | --- | --- |
| `cancelHotFloorDamage` | **true** | — |
| `enabled` | **true** | — |

**Optional durability (disabled)** — section `[strider_shoes.durability]`: `damagePerSecondActive` **1.0**; `maxDamage` **1800**.

## General curio

### Whoopee Cushion

**Item:** `artifacts:whoopee_cushion` · **Section:** `[whoopee_cushion]`

**+0.12 (12 percentage points) FLATULENCE chance** at fart-trigger checks. Cosmetic sound behavior; no configured poison cloud, reflection, or stun.

| Setting | Default | Planned value |
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

## Balance direction and approved recommendations

**Goal:** reduce excessive melee/ranged advantages so magic builds have more room. Confirmed changes are marked **Planned** in individual entries; upstream defaults remain in the Default column. These approved changes are now implemented in the replacement JAR.

The following **four recommendations are approved** and recorded in the individual planned-value tables. They are implemented; comparative combat-balance outcomes have not been measured in game.

| Artifact | Approved adjustment | Reason |
| --- | --- | --- |
| Power Glove | **+4 → +2 flat attack damage** | Reduces a direct melee damage advantage that also increases life-steal healing. |
| Vampiric Glove | **20% → 10% life steal**, retaining your **2-heart cap** | Reduces damage-scaled healing below the cap as well as on stronger hits. |
| Pocket Piston | **+0.75 → +0.3 attack knockback** | Keeps spacing utility while reducing repeated displacement. |
| Steadfast Spikes | **1.0 → 0.5 knockback resistance**, retain slip resistance | Retains a strong defensive bonus while allowing some counterplay. This affects all builds wearing it. |

**Conditional follow-ups, not approved:** an additional **1-second Vampiric Glove healing cooldown**, reducing Cross Necklace's extra invulnerability to **10 ticks**, and duplicate-artifact restrictions. Keep Cross Necklace at **20 extra ticks / 5-second cooldown** for the initial balance pass. Investigate duplicate stacking before deciding whether Artifacts needs its own restriction; the Nameless Trinkets rule does not automatically establish Artifacts behavior.

**Caster considerations:** these Artifacts changes mainly reduce melee power and shared survivability/mobility; they do not directly nerf bow damage or improve spell scaling. Avoid adding blanket magic bonuses until testing with the already planned **5% ordinary spell-power nodes** and **20% Magistrate school power**. Test representative melee, bow, and caster loadouts with comparable gear and skill investment.

**Obsidian Skull:** Fire Resistance II is recorded as requested, but vanilla level I already provides fire immunity. A higher level is not a meaningful protection upgrade; a duration/cooldown change would be the useful balance lever.

## Sources

- Implemented pack archive SHA-256: `567f61e66baa401c6d340a0183220276023404baa1f1e3d612935aeb03dfecb1`.

- Installed archive: `mods/artifacts-neoforge-15.1.3+26.2.1.jar`; inspected metadata, English names, tags, and compiled configuration definitions.
- Upstream SHA-256: `3be92116769cbba850acfc0e03bd0f3688c459e2b17144d97da36679fe35c104`.
- [Official project](https://www.curseforge.com/minecraft/mc-mods/artifacts). Matching upstream **26.2 / 15.1.3** snapshot: [803fd1aea1cece1362d40ba626c64cc1c3cdabb7](https://github.com/ochotonida/artifacts/tree/803fd1aea1cece1362d40ba626c64cc1c3cdabb7).
- [Configuration definitions](https://github.com/ochotonida/artifacts/blob/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/config/ItemConfigs.java), [item definitions](https://github.com/ochotonida/artifacts/blob/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/registry/ModItems.java), [ability implementations](https://github.com/ochotonida/artifacts/tree/803fd1aea1cece1362d40ba626c64cc1c3cdabb7/common/src/main/java/artifacts/component/ability).

Upstream reference established by static inspection. The implemented balance passed 64 checks on an isolated NeoForge dedicated server; no full client or comparative combat playtest was performed.
