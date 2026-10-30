# Weapon and armor enchantments - installed attributes

Audited 30 October 2026 from the current `mods/` JAR resources, Minecraft 26.2 resources, and selected custom item/effect handlers. This is a read-only source audit; no enchantment balance or JAR was changed.

The scan finds **157 base enchantment definitions**: **137 weapon/armor/shared entries**, 13 additional tool/mount/harness entries, six internal structure markers and one hidden vanilla Sweeping Edge. These are definitions, not a claim that every entry is in a particular world's loot pool. Optional Penchant/Universal Enchants datapacks and per-world selections can change application rules. The raw snapshot preserves every definition and supported-item tag in `enchantments.json`.

`L` means enchantment level. Attribute percentages describe the modifier operation, not a blanket multiplier on final damage. One HP is half a heart. Enchantment protection uses a separate protection-point layer: normally 4% per point, capped at 20 points/80% across applicable protection effects; this is not the same as armor or a Paladin reduction buff. Values listed per armor piece do not imply unlimited stacking.

Many specialist enchantments change item behavior rather than adding an attribute. Where their item code was not fully traced, the table gives the installed English effect description and does not invent a numerical coefficient. The raw record includes exact data components/conditions.

## Melee weapons

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Sharpness (`minecraft:sharpness`) | V | sharp_weapon; mainhand | Adds 8% base attack damage per level; V = +40%. Penchant replaces vanilla flat Sharpness damage. |
| Smite (`minecraft:smite`) | V | weapon; mainhand | Adds 2.5 damage per level against the sensitive_to_smite tag; V = +12.5. |
| Bane of Arthropods (`minecraft:bane_of_arthropods`) | V | weapon; mainhand | Adds 2.5 damage per level against arthropods; also applies Slowness IV with level-scaled random duration. |
| Knockback (`minecraft:knockback`) | II | melee_weapon; mainhand | Adds 1 knockback-effect unit per level; II = +2. |
| Fire Aspect (`minecraft:fire_aspect`) | II | fire_aspect; mainhand | Ignites targets for 4 seconds per level; II = 8s. |
| Looting (`minecraft:looting`) | III | crossbow_extra; mainhand | Increases qualifying equipment-drop probability by 1 percentage point per level. Supported tag includes the pack's crossbow extensions; ordinary loot-table bonuses also depend on the loot table. |
| Impaling (`minecraft:impaling`) | V | trident; mainhand | Adds 2.5 damage per level against sensitive_to_impaling targets; V = +12.5. |
| Loyalty (`minecraft:loyalty`) | III | trident; mainhand | Adds 1 trident-return acceleration per level. |
| Riptide (`minecraft:riptide`) | III | trident; hand | Wet-use trident propulsion strength: 1.5 / 2.25 / 3.0; native movement conditions apply. |
| Channeling (`minecraft:channeling`) | I | trident; mainhand | Summons lightning on qualifying trident hits during thunder; also has lightning-rod hit handling. |
| Density (`minecraft:density`) | V | mace; mainhand | Adds 0.5 smash damage per fallen block per level; V = +2.5 per block. |
| Breach (`minecraft:breach`) | IV | mace; mainhand | Reduces target armor effectiveness by 15 percentage points per level; IV = 60 points. |
| Wind Burst (`minecraft:wind_burst`) | III | mace; mainhand | Smash-triggered wind explosion, radius 3.5; knockback multiplier 1.2 / 1.75 / 2.2. |
| Lunge (`minecraft:lunge`) | III | lunge; hand | Spear lunge: horizontal impulse magnitude 0.458 per level; exhaustion 4 per level and 1 durability cost, with native food/movement restrictions. |
| Attack Speed (`attack_speed_enchantment:attack_speed`) | III | minecraft:swords; mainhand | Adds 0.1 attack_speed attribute per level; III = +0.3 attacks/s. Flat, not +10% per level. |
| Critical Hit (`critical_strike:chance`) | V | critical_strike:enchantable/weapon; mainhand | Adds 4% of base critical_strike:chance per level; V = +20 base-percentage points with the normal base-100 attribute. |
| Critical Impact (`critical_strike:damage`) | V | critical_strike:enchantable/weapon; mainhand | Adds 10% of base critical_strike:damage per level; V = +50 critical-multiplier percentage points with the normal base-100 attribute. |
| Frost Aspect (`ice_enchantments:frost_aspect`) | II | fire_aspect; mainhand | Sets frozen ticks to 300. Both I and II pass a hardcoded effect level of 1, so II currently gives no stronger freeze. |
| Aerial's Bane (`nova_structures:aerials_bane`) | V | nova_structures:enchantable/combat_tools; mainhand | Adds 2 damage per level against qualifying airborne/aerial targets; excludes specified water, mounted and bow/crossbow cases. |
| Illagers Bane (`nova_structures:illagers_bane`) | V | nova_structures:enchantable/combat_weapons; mainhand | Adds 2.5 damage per level against the mod's illager tag; V = +12.5. |
| Wither Coated (`nova_structures:wither_coated`) | III | nova_structures:enchantable/wither_coated; mainhand | Applies Wither I for 12s / II for 6s / III for 3s on qualifying direct hits; +1 item durability damage. |
| Discharge (`dungeons_arise:discharge`) | III | weapon; any | 15% - level proc definition builds movement Speed through successive hits, then discharges at Speed V into a radius-2 explosion/lightning; can damage the wielder. Some conditional entries use legacy array syntax and require runtime confirmation. |
| Ensnaring (`dungeons_arise:ensnaring`) | IV | weapon; any | 10% / 20% / 30% / 40% chance to apply Slowness XI for 2s and summon surrounding evoker fangs. |
| Lolth's Curse (`dungeons_arise:lolths_curse`) | I | weapon; any | 30% chance for Regeneration II for 2-4s; definition also includes 20% cave-spider spawning, 4-8 reflected damage against arthropods and 10s Darkness on players. Some conditions use legacy array syntax. |
| Backstabbing (`farmersdelight:backstabbing`) | III | farmersdelight:enchantable/knife; mainhand | Knife hits from behind multiply damage by 1.4 / 1.6 / 1.8. |
| Efficiency (`minecraft:efficiency`) | V | mining; mainhand | Adds L? + 1 mining_efficiency; V = +26. Included because axes can also be weapons. |

| Thunder Strike (`thunder_strike_enchantment:thunder_strike`) | III | mace; mainhand | Falling mace hits (fall distance at least 1 block) summon lightning on 1 / up to 2 / up to 3 targets; extra targets are within 2/3 blocks. Its separate 15% per-level roll controls durability loss. |

## Bows and crossbows

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Power (`minecraft:power`) | V | bow; mainhand | Adds 0.5 per level to arrow damage calculation; V = +2.5. This is an arrow damage input, not a guaranteed +2.5 final HP per hit. |
| Punch (`minecraft:punch`) | II | bow; mainhand | Adds 1 arrow knockback-effect unit per level; II = +2. |
| Flame (`minecraft:flame`) | I | bow; mainhand | Ignites fired arrows; native projectile fire duration is 100 seconds, distinct from the victim's burn time. |
| Infinity (`minecraft:infinity`) | I | bow; mainhand | Prevents ammo use for qualifying normal arrows; special-arrow rules remain. |
| Quick Charge (`minecraft:quick_charge`) | III | crossbow; mainhand, offhand | Reduces crossbow charge time by 0.25s per level; III = -0.75s. |
| Piercing (`minecraft:piercing`) | IV | crossbow; mainhand, offhand | Adds 1 projectile-piercing unit per level, with CrossArms projectile tagging. |
| Multishot (`minecraft:multishot`) | III | crossbow; mainhand, offhand | Adds 2 projectiles per level: 3 / 5 / 7 total. Added spread 5 / 8 / 11 degrees; CrossArms function hooks also apply. |
| Frost (`ice_enchantments:frost`) | I | bow; mainhand | Sets vanilla frozen ticks to 240 on projectile hits. This is vanilla freezing buildup, not RPG Frozen hard control. |
| Tri-Shot (`nova_structures:multishot`) | I | bow; mainhand | Tri-Shot: adds 2 bow projectiles (3 total) with 10 degrees additional spread. |
| Blowthrough (`nova_structures:piercing`) | IV | bow; mainhand | Blowthrough: +1 bow projectile-piercing unit per level; IV = +4. |
| Might (`nova_structures:power`) | V | crossbow; mainhand | Might: adds 1 / 1.5 / 2 / 2.5 / 3 to qualifying crossbow arrow damage input. |
| Gravity (`nova_structures:gravity`) | III | crossbow; mainhand | Arrow impacts create pulling explosions: negative knockback multiplier -0.75 / -1 / -1.25. Entity-hit radius 6; block hits use several offset bursts, including radius 10. |
| Acceleration (`crossarms:acceleration`) | V | crossbow; mainhand, offhand | Projectile launch impulse magnitude 2 / 3 / 4 / 5 / 6, plus CrossArms orientation correction. |
| Ricochet (`crossarms:ricochet`) | IV | crossbow; mainhand, offhand | Stores the enchantment level as the arrow's bounce count: 1-4. |
| Zero Drop (`crossarms:zero_drop`) | I | crossbow; mainhand, offhand | Removes arrow gravity; suppresses vanilla and RPG critical hits through the installed repair. |
| Frenzy (`crossarms:frenzy`) | I | crossbow; mainhand, offhand | Automatic crossbow firing with heat/spread and reload rules. Heat damage is 20/30/40/50% normal; slows the shooter and applies high-heat self-damage. See the current CrossArms patch for the complete firing state machine. |
| Spectral (`spectral_enchantment:spectral`) | I | bow; mainhand | Applies Glowing for 10s on hit. |
| Sniper (`sniper_enchantment:sniper`) | V | bow; mainhand | Distance-tiered arrow damage: at V, +1 / +2.5 / +4 / +5.5 / +7 for 10-20 / 20-30 / 30-40 / 40-50 / 50+ blocks. The definition uses array-shaped requirements; runtime validity needs confirmation. |

## Casting weapons

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Spell Power (`spell_power:spell_power`) | V | spell_power:enchantable/spell_power_generic; mainhand | Adds 5% generic Spell Power per level; V = +25%. Generic power contributes to healing as well as offensive schools. |
| Spell Haste (`spell_power:haste`) | V | spell_power:enchantable/haste; mainhand | Adds 4% base spell haste per level; V = +20%. |
| Spell Volatility (`spell_power:critical_chance`) | V | spell_power:enchantable/critical_chance; mainhand | Spell Volatility: +4% base spell-critical chance per level; V = +20 points with the normal base-100 attribute. |
| Amplify Spell (`spell_power:critical_damage`) | V | spell_power:enchantable/critical_damage; mainhand | Amplify Spell: +10% base spell-critical damage/healing multiplier per level; V = +50 points with the normal base-100 attribute. |
| Spell Infinity (`spell_engine:spell_infinity`) | I | spell_engine:enchantable/spell_infinity; mainhand | Prevents consumption of rune ammunition for supported spells; does not waive cooldowns, health costs or every other spell cost. |

## Armor, shields and elytra

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Protection (`minecraft:protection`) | IV | armor; armor | Adds 0.25 protection points per level: 1% general reduction per level per piece; IV = 4% per piece. |
| Projectile Protection (`minecraft:projectile_protection`) | IV | armor; armor | Adds 0.25 projectile-protection points per level: 1% per level per piece; IV = 4%. |
| Fire Protection (`minecraft:fire_protection`) | IV | armor; armor | Adds 0.25 fire-protection points per level: 1% per level per piece; IV = 4%. Burning duration remains -15% base per level. |
| Blast Protection (`minecraft:blast_protection`) | IV | armor; armor | Adds 0.25 blast-protection points per level: 1% per level per piece; IV = 4%. Explosion knockback resistance remains +0.15 per level. |
| Feather Falling (`minecraft:feather_falling`) | IV | foot_armor; armor | Adds 1.5 fall-protection points per level: 6% reduction per level, IV = 24% in the enchantment layer. |
| Thorns (`minecraft:thorns`) | III | armor; any | 15% proc chance per level; deals random 1-5 damage and costs 2 item durability when it procs. |
| Respiration (`minecraft:respiration`) | III | head_armor; head | Adds 1 oxygen_bonus per level; improves underwater breathing through the native oxygen calculation. |
| Aqua Affinity (`minecraft:aqua_affinity`) | I | head_armor; head | Multiplies submerged mining speed by 5 at level I, removing the usual underwater mining penalty. |
| Depth Strider (`minecraft:depth_strider`) | III | foot_armor; feet | Adds 1/3 water movement efficiency per level; III = +1. |
| Frost Walker (`minecraft:frost_walker`) | II | foot_armor; feet | Freezes nearby water with radius 2 + level, maximum 16; grants immunity to the defined hot-floor/campfire sources while active. |
| Soul Speed (`minecraft:soul_speed`) | III | foot_armor; feet | On soul-speed terrain: movement_speed +0.0405 / +0.051 / +0.0615 and movement efficiency +1. Has a 4% qualifying durability-loss chance. |
| Swift Sneak (`minecraft:swift_sneak`) | III | leg_armor; legs | Adds 0.15 sneaking_speed per level; III = +0.45. |
| Curse of Binding (`minecraft:binding_curse`) | I | equippable; armor | Prevents ordinary removal of equipped cursed armor. |
| Magic Protection (`spell_power:magic_protection`) | IV | armor; armor | Adds 0.25 protection points per level against c:is_magic damage: 1% per level per piece; IV = 4%. |
| Energize (`spell_power:energize`) | V | spell_power:enchantable/spell_power_energize; armor | Per armor piece: +3% base Healing and Lightning power per level; V = +15% to both. |
| Sunfire (`spell_power:sunfire`) | V | spell_power:enchantable/spell_power_sunfire; armor | Per armor piece: +3% base Arcane and Fire power per level; V = +15% to both. |
| Soulfrost (`spell_power:soulfrost`) | V | spell_power:enchantable/spell_power_soulfrost; armor | Per armor piece: +3% base Soul and Frost power per level; V = +15% to both. |
| Acrobat (`combat_roll:acrobat`) | V | chest_armor; chest | Chest: +20% base roll recharge rate per level; V = +100%. This changes recharge rate, not a flat -100% cooldown. |
| Longfooted (`combat_roll:longfooted`) | IV | foot_armor; feet | Boots: +1 roll-distance attribute per level; IV = +4. |
| Multi-Roll (`combat_roll:multi_roll`) | III | head_armor; head | Helmet: +1 available roll per level; III = +3. |
| Fangs (`fangs_enchantment:fangs`) | III | leg_armor; legs | Leggings: retaliatory functions summon 1 / up to 2 / up to 3 fangs; extra targets within 2/3 blocks. The 15% - level roll applies to durability loss, not the separately defined fang summons. |
| Downfall (`downfall_enchantment:downfall`) | IV | foot_armor; feet | Boot landing attack: 4 / 6 / 8 / 10 damage within 1.5 / 2 / 2.5 / 3 blocks, plus upward motion and Slowness III for 1s. Falling thresholds 3 / 4.5 / 5 / 6 blocks; user gets brief Slowness II/mining-fatigue marker. |
| Spiky (`spiky_enchantment:spiky`) | III | minecraft:shield; hand | Held shield retaliates for 2 / 4 / 6 damage through its advancement/function handler; conditional shield durability use. |
| Repellent (`enderzoology:repellent`) | IV | armor; any | 15% proc chance per level to teleport an attacker 8 / 12 / 16 / 20 blocks; costs 2 item durability on proc. |
| Purification (`dungeons_arise:purification`) | III | armor; armor | Defined chance 0.5% per level per tick to clear poison/wither and make a radius-2 magic explosion; conditional array syntax needs runtime confirmation. |
| Outreach (`nova_structures:outreach`) | IV | chest_armor; chest | Chest armor adds 0.5 block interaction range per level; IV = +2 blocks. This is block reach, not melee reach. |
| End Veil (`betterend:end_veil`) | I | head_armor; head | Helmet: looking at Endermen does not anger them. |
| Wax Wings (`nova_structures:wax_wings`) | I | minecraft:elytra; chest | Elytra water-related durability/flight behavior; conditional, not a flat armor bonus. |
| Soaring Winds (`betterarcheology:soaring_winds`) | I | betterarcheology:soaring_winds_items; any | Elytra takeoff boost; no flat armor stat in the definition. |

## Shared durability, curses and special material equipment

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Unbreaking (`minecraft:unbreaking`) | X | penchant:unbreaking_items; any | Armor: ignores 8% of durability loss per level; X = 80% avoidance (about 5x lifespan). Non-armor: L/(L+1) avoidance; X = 90.91%. Max level X. |
| Mending (`minecraft:mending`) | I | durability; any | XP repairs 2 durability per XP point through the native repair effect. |
| Curse of Vanishing (`minecraft:vanishing_curse`) | I | vanishing; any | Prevents the enchanted item from dropping on death. |
| Photosynthesis (`nova_structures:photosynthesis`) | I | durability; any | Repairs 1 item durability on qualifying 1%-chance ticks under specified sky/day or light conditions; no flat combat stat. |
| Curse of Conductivity (`nova_structures:conductivity_curse`) | I | nova_structures:enchantable/metal; any | Conditional lightning-attraction behavior; inspect the saved raw tick conditions for its weather/sky requirements. |
| Curse of Starvation (`withered_lands:curse_of_starvation`) | I | vanishing; any | No effect component in the installed definition; any hunger behavior depends on an external handler and is not established by this data audit. |
| Rubys' Fire (`betternether:ruby_fire`) | I | betternether:flaming_ruby_enchantable; any | Flaming Ruby equipment: +1 knockback when attacking; 50% retaliatory chance for 2-7 magic damage, ignition and pushback, costing 1 durability. Also smelts supported mined ores. |

## Alex's Caves specialist weapons

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Arrow Inducting (`alexscaves:arrow_inducting`) | I | resistor_shield; mainhand, offhand | Resistor Shield transforms any deflected arrows into seeking arrows |
| Astral Transferring (`alexscaves:astral_transferring`) | I | totem_of_possession; mainhand | Mobs possessed by the Totem of Possession can swap possessed status with other, healthier mobs they fight |
| Bonking (`alexscaves:bonking`) | I | primitive_club; mainhand | Primitive Club has a chance to drop heads from slain mobs, as if a charged Creeper killed them |
| Bouncing Bolt (`alexscaves:bouncing_bolt`) | I | sea_staff; mainhand | Sea Staff water bolt can bounce to a second target after a direct hit |
| Bouncy Ball (`alexscaves:bouncy_ball`) | III | shot_gum; mainhand | Gumballs bounce an additional two times for each level |
| Charting Call (`alexscaves:charting_call`) | IV | magic_conch; mainhand | Magic Conch summons additional Deep Ones to aide in combat |
| Chomping Spirit (`alexscaves:chomping_spirit`) | II | extinction_spear; mainhand | Extinction Spear Tremorsaurus attack deals increased damage |
| Crystallization (`alexscaves:crystallization`) | I | galena_gauntlet; mainhand, offhand | Galena Gauntlet can move items made of crystal, such as diamond tools |
| Dark Nock (`alexscaves:dark_nock`) | III | dreadbow; mainhand | Dreadbow charge time = 40 - 8L ticks: 32 / 24 / 16 ticks at I-III, versus 40 without it. |
| Dazing Sweep (`alexscaves:dazing_sweep`) | II | primitive_club; mainhand | Adds a club stun sweep in a box of size L + 1.2: 2.2 / 3.2 blocks at I-II (box size, not radius). |
| Detonating Death (`alexscaves:detonating_death`) | I | totem_of_possession; mainhand | Possessed-mob death explosion strength = 2 + floor(mob width). |
| Double Stab (`alexscaves:double_stab`) | I | desolate_dagger; mainhand | Desolate Dagger summons two ghostly daggers instead of one. |
| Energy Efficiency (`alexscaves:energy_efficiency`) | III | raygun; mainhand | Raygun uses up charge at a slower rate |
| Enveloping Bubble (`alexscaves:enveloping_bubble`) | I | sea_staff; mainhand | Sea Staff water bolt can envelop the target in a drowning bubble of water |
| Explosive Flavor (`alexscaves:explosive_flavor`) | I | shot_gum; mainhand | Shotgum fires a single slow, explosive gumball |
| Far Flung (`alexscaves:far_flung`) | III | candy_cane_hook; mainhand | Hook launches even further when swung |
| Ferrous Haste (`alexscaves:ferrous_haste`) | I | galena_gauntlet; mainhand, offhand | Galena Gauntlet items attack and mine much faster |
| Field Extension (`alexscaves:field_extension`) | IV | galena_gauntlet; mainhand, offhand | Galena Gauntlet can move items further away |
| Flinging (`alexscaves:flinging`) | III | ortholance; mainhand | Ortholance launch moves the player much further |
| Gamma Ray (`alexscaves:gamma_ray`) | I | raygun; mainhand | Raygun beam inflicts deadly blue Irradiated effect |
| Heavy Slam (`alexscaves:heavy_slam`) | III | resistor_shield; mainhand, offhand | Resistor Shield first slam attack deals additional damage |
| Herd Phalanx (`alexscaves:herd_phalanx`) | III | extinction_spear; mainhand | Extinction Spear summons 3 + L armored heads: 4 / 5 / 6 at I-III. |
| Humungous Hex (`alexscaves:humungous_hex`) | II | sugar_staff; mainhand | Hex scale = 1 + 0.25L: 1.25x / 1.5x at I-II. |
| Impending Stab (`alexscaves:impending_stab`) | III | desolate_dagger; mainhand | Ghostly dagger attack is delayed and deals increased damage |
| Lasting Morale (`alexscaves:lasting_morale`) | III | magic_conch; mainhand | Deep Ones summoned by Magic Conch will stay around longer |
| Multiple Mint (`alexscaves:multiple_mint`) | II | sugar_staff; mainhand | Sugar Staff summons 3 + L peppermints: 4 / 5 at I-II. |
| Peppermint Punting (`alexscaves:peppermint_punting`) | I | sugar_staff; mainhand | Peppermints bounce directly ahead instead of around the attacker |
| Plummeting Flight (`alexscaves:plummeting_flight`) | III | extinction_spear; mainhand | Extinction Spear Subterranodon attack carries foes higher and for longer |
| Precise Volley (`alexscaves:precise_volley`) | I | dreadbow; mainhand | Dreadbow arrows rain down from the sky to a more central position |
| Rapid Possession (`alexscaves:rapid_possession`) | III | totem_of_possession; mainhand | Possessed-mob navigation speed factor = 1.25 + 0.35L: 1.6 / 1.95 / 2.3 at I-III. |
| Relentless Darkness (`alexscaves:relentless_darkness`) | I | dreadbow; mainhand | Dreadbow fires arrows directly ahead in a continuous stream |
| Sated Blade (`alexscaves:sated_blade`) | II | desolate_dagger; mainhand | Ghostly dagger attack increases the food saturation level of the attacker |
| Sea Swing (`alexscaves:sea_swing`) | I | ortholance; mainhand | Ortholance swing attack summons a single, forwards-facing wave |
| Seapairing (`alexscaves:seapairing`) | I | sea_staff; mainhand | Sea Staff can occasionally regain durability when in the inventory of a submerged entity |
| Second Wave (`alexscaves:second_wave`) | I | ortholance; mainhand | Ortholance summons a second, delayed wave after launching the player |
| Seekcandy (`alexscaves:seekcandy`) | I | sugar_staff; mainhand | Peppermints created by the staff seek out any entity the attacker looks at |
| Shaded Respite (`alexscaves:shaded_respite`) | I | dreadbow; mainhand | Dreadbow will not consume normal arrows or durability if fired in darkness |
| Sharp Cane (`alexscaves:sharp_cane`) | II | candy_cane_hook; mainhand | Hook deals three damage per level when attaching to a target |
| Sightless (`alexscaves:sightless`) | I | totem_of_possession; mainhand | Mobs possessed by the Totem of Possession can be controlled without a direct line of sight |
| Soak Seeking (`alexscaves:soak_seeking`) | III | sea_staff; mainhand | Sea Staff water bolts now home-in on targets more accurately |
| Solar (`alexscaves:solar`) | I | raygun; mainhand | Raygun recharges over time when user is under direct sunlight |
| Spell Lasting (`alexscaves:spell_lasting`) | III | sugar_staff; mainhand | Hex lifespan = 100 + 60L ticks: 8 / 11 / 14 seconds at I-III. |
| Straight Hook (`alexscaves:straight_hook`) | I | candy_cane_hook; mainhand | Hook launches directly ahead and is not hindered by gravity |
| Swiftwood (`alexscaves:swiftwood`) | III | primitive_club; mainhand | Primitive Club can be swung much faster |
| Targeted Ricochet (`alexscaves:targeted_ricochet`) | I | shot_gum; mainhand | Gumballs will bounce to the nearest target after hitting a wall or entity |
| Taxing Bellow (`alexscaves:taxing_bellow`) | I | magic_conch; mainhand | Using the Magic Conch lowers the opinion held by Deep Ones, but does not decrease durability |
| Triple Splash (`alexscaves:triple_splash`) | I | sea_staff; mainhand | Sea Staff fires two additional water bolts |
| Triple Split (`alexscaves:triple_split`) | I | shot_gum; mainhand | Gumballs split into three additional Gumballs after hitting their first target |
| Tsunami (`alexscaves:tsunami`) | I | ortholance; mainhand | Ortholance launch summons a single, gigantic wave |
| Twilight Perfection (`alexscaves:twilight_perfection`) | III | dreadbow; mainhand | Dreadbow arrows glow red and deal double damage if fired at the precise bow charge time |
| X-Ray (`alexscaves:x_ray`) | I | raygun; mainhand | Raygun beam pierces through walls |

## Other tools, mounts and harnesses (outside player weapon/armor balance)

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Fortune (`minecraft:fortune`) | III | mining_loot; mainhand | No direct attribute: tool loot tables use the enchantment level to change drops. |
| Silk Touch (`minecraft:silk_touch`) | I | mining_loot; mainhand | Special block-drop selection; suppresses normal block XP. No combat attribute. |
| Lure (`minecraft:lure`) | III | fishing; mainhand | Fishing-time reduction +5 per level in the native fishing calculation. |
| Luck of the Sea (`minecraft:luck_of_the_sea`) | III | fishing; mainhand | Adds 1 fishing_luck_bonus per level. |
| Tunneling (`betterarcheology:tunneling`) | I | betterarcheology:tunneling_items; hand | Mining digs an additional block below; no combat attribute. |
| Resonance (`betterend:resonance`) | II | wover:tools/hammers; mainhand | Hammer mining breaks surrounding blocks; sneaking limits the effect. No direct combat attribute. |
| Obsidian Breaker (`betternether:obsidian_breaker`) | V | mining; mainhand | Mining-only obsidian breaking-speed behavior; no fixed combat attribute in the definition. |
| Hydro Veil (`nova_structures:hydro_veil`) | IV | minecraft:harnesses; body | Happy-Ghast harness enchantment: environmental defenses, rain-dependent movement/healing; not player armor. Full conditional attributes are in the raw record. |
| Swift Soar (`nova_structures:swift_soar`) | III | minecraft:harnesses; body | Happy-Ghast harness flying behavior; not player armor or a player combat stat. |
| Returning Board (`alexsmobs:board_return`) | I | alexsmobs:straddleboard_enchantable; mainhand | Straddleboard returns instantly to the rider's inventory when broken. |
| Lavawaxed (`alexsmobs:lavawax`) | I | alexsmobs:straddleboard_enchantable; mainhand | Gives the rider of the Straddleboard Fire Resistance. |
| Serpent Charmer (`alexsmobs:serpentfriend`) | I | alexsmobs:straddleboard_enchantable; mainhand | Makes Bone Serpents neutral towards the rider of the Straddleboard. |
| Straddle Jump (`alexsmobs:straddle_jump`) | III | alexsmobs:straddleboard_enchantable; mainhand | Increases the height of Straddleboard jumps. |

## Hidden vanilla entry

| Enchantment | Max | Equipment / slots | Attribute or effect |
|---|---:|---|---|
| Sweeping Edge (`minecraft:sweeping_edge`) | III | sweeping; mainhand | HIDDEN. Registered vanilla enchantment; sweep-damage ratio bonus L/(L+1). Not normally obtainable. |

## Internal structure enchantments

These six entries are encounter/structure logic, not player enchantment choices:

- `nova_structures:boss_behaviour` - Bug Enchant
- `nova_structures:boss_nether` - Bug Enchant
- `nova_structures:librarian_level_checker` - Bug Enchant
- `nova_structures:shulker_boss` - Bug Enchant
- `nova_structures:shulker_miniboss` - Bug Enchant
- `nova_structures:tavern_level_checker` - Bug Enchant

## Retired enchantments and combination rules

Only vanilla Sweeping Edge remains registered but hidden by the pack's disabled tag. Toughness and its books are also removed. The previously hidden modded entries were removed: Protection Grace, Antidote, Traveler, Soulbound, Air Jump, Tense, Withering, Deflector, Bombard, Recycling, Ghasted, Penetrating Strike, Spiteful and Decay. Life Steal and True Shot are removed with their mods; Capacity is removed from CrossArms. They are not active balance candidates in this list.

- Sharpness excludes Smite, Bane of Arthropods, Aerial's Bane, Illagers Bane and Ensnaring. The four specialist damage enchantments can combine with each other.
- Discharge excludes Fire Aspect and Frost Aspect.
- Flame, Frost and Spectral exclude each other; Tri-Shot excludes Power.
- Crossbow Multishot, Might, Acceleration and Frenzy exclude each other. Gravity excludes Frenzy. Piercing and Zero Drop have no pairwise conflicts under the current pack policy.
- Thorns excludes Fire Protection, Blast Protection and Fangs on the same piece. Frost Walker excludes Soul Speed on the same boots.
- Other item-support and native exclusions remain. Per-world optional compatibility packs should be checked before changing acquisition.

## Rework findings

1. Sharpness is **+8% base melee damage per level**, not vanilla flat damage. Power is a **+0.5-per-level arrow damage input**, not vanilla's familiar percentage description. Attack Speed is **+0.1 flat attack speed per level**.
2. Unbreaking is already defined through **X**. Its armor and non-armor durability probabilities differ.
3. Frost uses vanilla freezing buildup. Frost Aspect I/II both pass effect level 1, so both set **300 frozen ticks**. This is distinct from the RPG Frozen crowd-control effect.
4. Fangs and Thunder Strike have separately defined damage functions and durability-chance entries. Their 15%-per-level durability chance is not a global proc chance for all of their effects.
5. Some Sniper and Dungeons Arise conditional entries use array-shaped `requirements`. Their intended values are recorded, but their exact runtime decoding/trigger behavior needs an isolated registry test before relying on them for a rework.
6. Spell Infinity preserves rune ammunition; it does not remove every casting cost. Spell Power's generic attribute contributes to healing as well as damage schools.
7. Curse of Starvation has no effect component in its definition; external handler behavior remains unverified in this audit.

See the current retirement, armor, melee, ranged, and CrossArms patch READMEs for the historical installation/compatibility tests. This reference makes no new in-game verification claim.


Protection balance update: all five protection enchantments now grant 1% reduction per level per piece. Four IV pieces contribute 16% each; general and matching specialized protection add within the existing layer. The user's coexistence policy is preserved. See [the implementation](../protection-one-percent/README.md).
