# Accessory balance: complete wearable inventory

Offensive proc power means the highest current melee, ranged or offensive spell attribute; Healing Power is excluded. Health scaling excludes all three scaling accessory bonuses and Health Boost; melee scaling excludes Power Glove and Woundbearer. Effective stat caps hold under outside attribute multipliers. Equipping or refreshing health items does not heal. Damage caps apply before defenses; heal caps are before healing-received modifiers.

## Nameless Trinkets - 42 available

| Item | Decision | Effective details |
| --- | --- | --- |
| Amphibious Hands (`nameless_trinkets:amphibious_hands`) | Retained | Underwater mining x1.25 (+25%). Does not provide underwater breathing. |
| Blaze Nucleus (`nameless_trinkets:blaze_nucleus`) | Adjusted | Reduces fire damage by 50%; qualifying enemy hits ignite for 4s and extinguish the wearer. |
| Blindfold (`nameless_trinkets:blindfold`) | Retained | Enabled; no configurable numeric strength in its Stats class. Not a guarantee that every hostile mob loses its target or stops attacking. |
| Broken Ankh (`nameless_trinkets:broken_ankh`) | Retained | 36,000-tick cooldown = 30 min. Has a configurable recharge cooldown; it is not continuously available. |
| Callus (`nameless_trinkets:callus`) | Retained | 10% fall-damage reduction; 5% other damage reduction; listed hazard damage is zeroed. These are separate branches, not a combined 15% fall reduction. Damage reductions are configurable; this is not general invulnerability. |
| Creeper Sense (`nameless_trinkets:creeper_sense`) | Adjusted | Sneaking triggers a power-3 explosion on a 12s cooldown. No block destruction; the wearer can still be hurt. Party allies and their pets are protected. |
| Dark Nelumbo (`nameless_trinkets:dark_nelumbo`) | Retained | Lava-damage cancellation enabled by default. Its configuration includes a lava-damage cancellation option; do not assume this protects against every fire source. |
| Dragon's Eye (`nameless_trinkets:dragons_eye`) | Retained | 20-block search range; blindness enabled. The configuration also enables blindness by default. This can explain an unexpected darkened view. |
| Eclipse Ashes (`nameless_trinkets:eclipse_ashes`) | Adjusted | Taking an enemy hit summons phantom wolves for 5s, spaced 1s apart. Each deals 1 damage plus 5% of your strongest offensive power, capped at 3. New sessions have a 12s interval; hits do not extend the session. |
| Experience Battery (`nameless_trinkets:experience_battery`) | Retained | +50% qualifying kill XP, approximately x1.5 total before rounding. Does not promise a bonus to every XP source, such as mining or furnaces. |
| Experience Magnet (`nameless_trinkets:experience_magnet`) | Retained | 25-block configured attraction range. Does not attract dropped items by itself. |
| Explosion-proof Jacket (`nameless_trinkets:explosion_proof_jacket`) | Adjusted | Reduces explosion damage by 20%; awards at most 1 TNT every 20s from qualifying explosion events. |
| Fate Emerald (`nameless_trinkets:fate_emerald`) | Adjusted | Trade discount boost 25 (raw value, not a guaranteed 25% discount). Retains 60s removal lock and 0.1 exhaustion per second. |
| Fertilizer (`nameless_trinkets:fertilizer`) | Retained | Growth interval 100 ticks = 5 s. Uses a configurable interval; it is not instantaneous growth for every plant. |
| Four Leaf Clover (`nameless_trinkets:four_leaf_clover`) | Adjusted | Eligible non-player kills have a 25% chance of one extra loot-table roll. Not guaranteed duplication of original drops. |
| Fractured Nullstone (`nameless_trinkets:fractured_nullstone`) | Adjusted | Reduces recognized magic damage by 15%, leaving 85% before other defenses. |
| Fragile Cloud (`nameless_trinkets:fragile_cloud`) | Retained | Gravity modifier -0.07 while qualifying descent is active; resets fall distance. No configurable numeric Stats fields. Its handler yields when Moon Stone is equipped; elytra flight, grounded movement and water have separate handling. |
| Ghast Eye (`nameless_trinkets:ghast_eye`) | Adjusted | Adds 7.5% of maximum HP before the three scaling health accessories and Health Boost, capped at +6 HP. Kills refresh Regeneration I for 5s without adding duration; stronger unrelated regeneration remains. |
| Gills (`nameless_trinkets:gills`) | Retained | Air decreases by 7 per tick outside suitable water; choking hit 2 HP (1 heart); choking blindness enabled; underwater air resets to 300. Air drains outside water and can cause choking and blindness. Normal removal requires the wearer's eyes to be underwater. |
| Ice Cube (`nameless_trinkets:ice_cube`) | Retained | Slowness I for 100 ticks (5 s); Frost Walker setting 1; unchanged freezing-immunity option enabled. Slowness strength/duration and freezing behavior are configurable; the source also includes a freezing-immunity setting. |
| Light Gloves (`nameless_trinkets:light_gloves`) | Retained | Mining x1.5 (+50%) while unable to see the sky. A roof can satisfy this check; the condition is not simply being below a fixed Y level. |
| Lucky Rock (`nameless_trinkets:lucky_rock`) | Retained | 5% configured reward chance; eligible blocks and rewards are configured lists. Both eligible blocks and reward items are configured lists; not every mined block qualifies. |
| Miner's Soul (`nameless_trinkets:miners_soul`) | Retained | +1 Fortune level for eligible block loot; skips Silk Touch. The handler skips Silk Touch and block-entity blocks; it is not an extra Looting level for mob kills. |
| Missing Page (`nameless_trinkets:missing_page`) | Adjusted | Taking sourced enemy damage releases a 5-block burst for 1 damage plus 10% of your strongest offensive power, capped at 6 per target. 4s interval; no boss-HP scaling. |
| Moon Stone (`nameless_trinkets:moon_stone`) | Adjusted | Gently lowers gravity and prevents fall damage. Gravity reduction is 3/8 of the old effect (-0.02625; ordinary gravity becomes 0.05375). |
| Nelumbo (`nameless_trinkets:nelumbo`) | Retained | Water-walking enabled; no configurable numeric strength in Stats. Does not supply breathing or faster underwater mining. |
| Puffer Fish Liver (`nameless_trinkets:puffer_fish_liver`) | Retained | 100% poison application on eligible targets; Poison I for 60 ticks (3 s). Poison-immune creatures remain immune. Poison chance, strength and duration are configurable; poison immunity is not immunity to all negative effects. |
| Rage Mind (`nameless_trinkets:rage_mind`) | Adjusted | Deal +10% damage against the marked creature type; existing marking and range conditions remain. |
| Resonant Heart (`nameless_trinkets:resonant_heart`) | Adjusted | Enemy hits have exactly a 10% chance to trigger a sonic retaliation for 2 damage plus 15% of your strongest offensive power, capped at 8. 6s interval. |
| Reverse Card (`nameless_trinkets:reverse_card`) | Adjusted | Enemy hits have exactly a 25% chance to reflect 25% of incoming damage. Reflected damage is capped at 2 plus 10% of your strongest offensive power, never above 6. 4s interval; does not cancel the incoming hit. |
| Scarab Amulet (`nameless_trinkets:scarab_amulet`) | Adjusted | Movement on sand +30% (1.3x); husk targeting protection retained. |
| Shrinking Veil (`nameless_trinkets:shrinking_veil`) | Retained | Movement x1.25 (+25%); scale x0.75 (-25%); Weakness II refreshed to 1,200 ticks (60 s). Weakness can remain for up to 60 seconds after unequipping. It does not provide invisibility. |
| Sigil Of Baphomet (`nameless_trinkets:sigil_of_baphomet`) | Adjusted | Kills add 0.5s of damage immunity, banked up to 2s. Stored legacy counts are clamped to the current cap. |
| Sleeping Pills (`nameless_trinkets:sleeping_pills`) | Retained | Phantom range 2 blocks; night-vision and weakness durations 1,200 ticks (60 s), refreshed by equipped behavior; bed sleeping disabled. Also applies Weakness and, by default, prevents sleeping in beds. |
| Spider Legs (`nameless_trinkets:spider_legs`) | Retained | Climb-speed setting 0.1 (raw movement value, not 10% speed). Climb speed is configurable; this is not free flight. |
| Tear Of The Sea (`nameless_trinkets:tear_of_the_sea`) | Retained | +0.5 water-movement-efficiency attribute (configured as 50%); not a guaranteed +50% final swim speed. Does not supply underwater breathing by itself. |
| Tick (`nameless_trinkets:tick`) | Adjusted | Once per second, drains eligible non-player enemies within an 8-block sphere for 1 damage plus 10% strongest offensive power, capped at 6 per target. Targets must have over 50 maximum HP and be above half HP. Each successful pulse costs the wearer 5% maximum HP, or exhaustion equal to 10% current food when below 5 HP. Own and party pets are protected. Normal removal still requires being on fire; creative bypass retained. |
| Titan's Mark (`nameless_trinkets:titans_mark`) | Adjusted | Size +25%, attack damage +5%. Adds 15% of maximum HP before the three scaling health accessories and Health Boost, capped at +10 HP. Refreshes Resistance I for 5s and Slowness I for 60s; no healing on equip or refresh. |
| True Heart Of The Sea (`nameless_trinkets:true_heart_of_the_sea`) | Retained | Underwater mining x1.25 (+25%); other water and land-breathing effects unchanged: +1 water-movement efficiency, 1.5-HP choking damage, air drain 5/tick. The wearer loses air on land and can choke and receive blindness. Treat it as a water-specialist item with a land-breathing drawback. |
| Vampire Blood (`nameless_trinkets:vampire_blood`) | Adjusted | Attack damage +10%; movement speed +10%. Enemy kills heal 2 HP plus 5% of wearer maximum HP, capped at 4 HP, once per 2s. Sunlight damage remains 2 HP. Removal stays locked for 48,000 equipped ticks (two full Minecraft days); offline time and sleep skips do not advance it. |
| Wooden Stick (`nameless_trinkets:wooden_stick`) | Retained | Recharge 200 ticks = 10 s. Recharge cooldown prevents it from blocking every incoming hit. |
| Woundbearer (`nameless_trinkets:woundbearer`) | Adjusted | Stores 1% of damage taken as extra attack damage. Storage limit is 10% of melee power before Power Glove and Woundbearer, capped at +4 effective attack damage. Decay starts after 3s with base decay 0.01 per tick; legacy storage is clamped. |

## Artifacts - 45 wearables

| Item | Decision | Effective details |
| --- | --- | --- |
| Plastic Drinking Hat (`artifacts:plastic_drinking_hat`) | Retained | **+150% drinking speed** and **+50% eating speed**. These are speed increases, not equivalent reductions in consumption time. |
| Novelty Drinking Hat (`artifacts:novelty_drinking_hat`) | Retained | **+150% drinking speed** and **+50% eating speed**. Same default bonuses as Plastic Drinking Hat; extra flavor text is not another combat bonus. |
| Snorkel (`artifacts:snorkel`) | Retained | **Water Breathing I for 30 seconds**, replenished while recovering air. **Infinite underwater breathing is disabled** by default. |
| Night Vision Goggles (`artifacts:night_vision_goggles`) | Retained | Toggleable **NIGHT VISION**, reduced to brightness strength **0.15 (15%)**. This is visual strength, not activation chance. |
| Villager Hat (`artifacts:villager_hat`) | Retained | **+75 villager reputation points**, affecting reputation-based trading. This is **not a guaranteed 75% discount**. |
| Superstitious Hat (`artifacts:superstitious_hat`) | Retained | **+1 Looting level** for applicable drops, not generic Luck. |
| Cowboy Hat (`artifacts:cowboy_hat`) | Retained | **+40% base mount movement speed** while riding. |
| Angler's Hat (`artifacts:anglers_hat`) | Retained | **+1 Luck of the Sea** and **+1 Lure level** for fishing. |
| Lucky Scarf (`artifacts:lucky_scarf`) | Retained | **+1 Fortune level** for applicable block drops, not generic Luck. |
| Scarf of Invisibility (`artifacts:scarf_of_invisibility`) | Retained | Toggleable **INVISIBILITY**. Defaults do not hide its own model when invisible or hide effect particles. |
| Cross Necklace (`artifacts:cross_necklace`) | Adjusted | Adds 10 invulnerability ticks (0.5s) after taking damage; native cooldown 5s. |
| Panic Necklace (`artifacts:panic_necklace`) | Retained | After being hurt, grants **Speed I for5s**; **7s cooldown**. |
| Shock Pendant (`artifacts:shock_pendant`) | Adjusted | 25% chance to retaliate against an eligible attacker under open sky. Visual lightning accompanies 2 damage plus 10% of strongest offensive power, capped at 6. 6s cooldown. Retains lightning immunity; no collateral lightning damage or fire. |
| Flame Pendant (`artifacts:flame_pendant`) | Adjusted | 40% chance to ignite an eligible attacker for 4s, granting the wearer Fire Resistance I for 4s. 4s cooldown; allies and accessory retaliation are excluded. |
| Thorn Pendant (`artifacts:thorn_pendant`) | Adjusted | 50% chance to retaliate for 1 damage plus 10% wearer armor, capped at 4 damage. Fixed 4s interval; excludes allies and cannot trigger retaliation chains. |
| Charm of Sinking (`artifacts:charm_of_sinking`) | Retained | Toggleable **SINKING** in water, **+1.5 oxygen bonus attribute**, and prevention of underwater fall damage. Oxygen is a raw attribute bonus, not a verified direct breath-duration multiplier. |
| Charm of Shrinking (`artifacts:charm_of_shrinking`) | Retained | Toggleable **75% original size**, **1.2x movement speed** and refreshing **Weakness II for60s** while active. Native Artifacts effect padding adds19ticks to the refreshed duration; unequipping uses native cleanup. |
| Cloud in a Bottle (`artifacts:cloud_in_a_bottle`) | Retained | **DOUBLE JUMP**, **+3 blocks safe fall distance**. Double jump resets accumulated fall distance; default multiplier **0.0** avoids damage for the fall accumulated before that jump. The subsequent fall still matters. Sprint double jump: **+25% vertical launch velocity**, **+0.25 horizontal velocity**. |
| Obsidian Skull (`artifacts:obsidian_skull`) | Retained | After fire damage, grants **Fire Resistance II for30s** with a **60s cooldown**. Vanilla levelII gives no stronger fire protection than levelI; it does not retroactively cancel the triggering hit. |
| Antidote Vessel (`artifacts:antidote_vessel`) | Retained | Caps supported finite harmful-effect duration at **5 seconds**: **POISON**, **WITHER**, **WEAKNESS**, **SLOWNESS**, **BLINDNESS**, **NAUSEA**, **MINING FATIGUE**, **HUNGER**, **LEVITATION**. Internal cooldown **2 seconds**. Infinite effects are excluded. Cap **0** removes applicable finite effects. |
| Universal Attractor (`artifacts:universal_attractor`) | Retained | Toggleable **Magnetism V** pulls dropped items. Search box extends **5 blocks in each axis**, capped at **10** at higher levels; not a spherical radius. Excludes pickup-delayed items and items marked as thrown by wearer. |
| Crystal Heart (`artifacts:crystal_heart`) | Adjusted | Adds 10% of maximum HP before the three scaling health accessories and Health Boost, capped at +8 effective HP. No healing on equip or refresh; cannot amplify the other scaling health curios. |
| Helium Flamingo (`artifacts:helium_flamingo`) | Retained | **SWIM THROUGH AIR for 8 seconds**, **15-second recharge duration**, **3-second cooldown**. Limited flight. |
| Chorus Totem (`artifacts:chorus_totem`) | Adjusted | Consumed death protection: clears effects, attempts a teleport within 32 blocks and restores 25% of current maximum HP, capped at 16 HP. Native rescue consumption and other effects remain. |
| Warp Drive (`artifacts:warp_drive`) | Retained | Ender-pearl use costs **2 hunger points (1 drumstick)** instead of consuming the pearl when enough food is available; otherwise normal pearl consumption applies. **Pearl teleport damage canceled** by default; extra artifact cooldown **0 seconds**. |
| Digging Claws (`artifacts:digging_claws`) | Retained | **+30% base block-breaking speed**, with a **stone-tier tool upgrade** for applicable mining. Does not grant diamond/netherite harvesting capability. |
| Feral Claws (`artifacts:feral_claws`) | Adjusted | +10% base attack speed. |
| Power Glove (`artifacts:power_glove`) | Adjusted | Adds 5% of melee power before Power Glove and Woundbearer, capped at +2 effective attack damage. Recalculates as outside equipment and buffs change. |
| Fire Gauntlet (`artifacts:fire_gauntlet`) | Adjusted | +4 seconds attack-burning duration. |
| Pocket Piston (`artifacts:pocket_piston`) | Retained | **+0.3 attack knockback**; this is a raw attribute amount, not30% damage. |
| Vampiric Glove (`artifacts:vampiric_glove`) | Adjusted | Basic melee hits heal 5% of actual health damage, capped at 2 HP, once per 1s. Overkill and absorption do not increase healing; allies and accessory retaliation cannot trigger it. Healing-received modifiers remain native. |
| Golden Hook (`artifacts:golden_hook`) | Retained | **+50% entity experience** from applicable kills, not every XP source. |
| Onion Ring (`artifacts:onion_ring`) | Retained | After eating: **Haste II**, **6 seconds per hunger point actually restored**. Hunger that could not be restored does not count. |
| Pickaxe Heater (`artifacts:pickaxe_heater`) | Retained | **AUTO-SMELTS** applicable mined ore drops with matching recipes. Not an automatic furnace for every block. |
| Withered Bracelet (`artifacts:withered_bracelet`) | Adjusted | Melee hits have a 30% chance to apply Wither I for 4s. 4s cooldown shared by the wearer; excludes allies and accessory retaliation. |
| Aqua-Dashers (`artifacts:aqua_dashers`) | Retained | Allows **SPRINTING ON WATER**, not lava. |
| Bunny Hoppers (`artifacts:bunny_hoppers`) | Retained | **+40% base jump strength**, **+10 blocks safe fall distance**, rabbit hurt sounds. Here `fallDamageMultiplier = 0.0` is an additive base modifier: **no extra multiplier change**, not complete fall immunity. |
| Kitty Slippers (`artifacts:kitty_slippers`) | Retained | **REPELS CREEPERS** and **PHANTOMS**, plus cat hurt sounds. Does not repel every hostile mob. |
| Running Shoes (`artifacts:running_shoes`) | Adjusted | +15% base movement speed while sprinting; retains +0.5 block step height. |
| Snowshoes (`artifacts:snowshoes`) | Retained | **+30% base movement speed on snow**, **WALK ON POWDER SNOW**. |
| Steadfast Spikes (`artifacts:steadfast_spikes`) | Adjusted | +30% knockback resistance and +1 slip resistance. Fits the restored Feet slot, separate from Nameless Shoes. |
| Flippers (`artifacts:flippers`) | Retained | **+70% base swim speed**. |
| Rooted Boots (`artifacts:rooted_boots`) | Retained | While grounded on supported grass and hungry: **1 hunger point per 10-second interval**, with saturation. Eating while grounded on supported grass can **GROW PLANTS** using bone-meal behavior. |
| Strider Shoes (`artifacts:strider_shoes`) | Retained | **WALK ON LAVA while sneaking**; prevents hot-floor damage such as magma blocks. Not blanket immunity to every fire/lava damage source. |
| Whoopee Cushion (`artifacts:whoopee_cushion`) | Retained | **+0.12 (12 percentage points) FLATULENCE chance** at fart-trigger checks. Cosmetic sound behavior; no configured poison cloud, reflection, or stun. |

## Other entries

The 12 legacy Nameless relics stay disabled and removable. Six supporting items, Umbrella, Everlasting Beef, Eternal Steak and the Mimic Spawn Egg retain their roles. Wooden Stick retains its hit-negation effect.
