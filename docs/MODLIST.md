# Modpack Player Handbook Scaffold

Last audited: 2026-10-08  
Pack target: Minecraft 26.2 with NeoForge 26.2  
Current distribution: 278 synchronized JARs plus 4 optional client-store JARs

## About this scaffold

This document is the working content outline for a future player-facing PDF handbook. It is written for players who understand vanilla Minecraft but are new to this modpack. It prioritizes changed mechanics, controls, configuration, and relationships between systems rather than exhaustive lists of items, creatures, blocks, recipes, structures, or loot.

Libraries, APIs, compatibility bridges, and invisible server internals are omitted from the gameplay catalog. A support mod may still appear in the keybind reference when it exposes controls to players.

## How entries are organized

Each mod has one primary entry. Secondary topics link back to that entry instead of repeating it. Broad mods use an Integrated expansion type when they combine multiple substantial systems.

**Entry types:** Core system; Integrated expansion; Focused content; Utility; Interface feature; Optional client feature.

**Integrated-expansion subtypes:** Integrated world expansion; Integrated dimension expansion; Integrated adventure expansion; Integrated character-progression suite; Integrated survival or profession expansion; Integrated building expansion.

**Cross-reference tags:** Combat; Character; Skills; Magic; Equipment; Bosses; Events; World Generation; Structures; Creatures; Companions; Farming; Food; Building; Storage; Travel; Multiplayer; Controls; HUD.

## Part I — Start Here

### Essential keybinds and conflict resolution

Configure controls from the top of Minecraft's Controls screen to the bottom. The complete category-ordered tables in this chapter keep actual defaults, pack recommendations, and conflicts separate.

### Choosing a race

Choose a race before settling on a build. See [Races and racial traits](#races-and-racial-traits) for the canonical system entry.

### Choosing a class

Classes establish the player's main combat style. See [Classes and playstyles](#classes-and-playstyles).

### Skills, abilities, spells, and runes

Class skills, general perks, spell resources, and runes are separate but related systems. See [Skill trees and character progression](#skill-trees-and-character-progression) and [Abilities, spells, runes, and resources](#abilities-spells-runes-and-resources).

### First-session checklist

Resolve conflicting keys, choose a race and class, open the relevant skill screens, confirm the recommended HUD positions, and learn the party and revival flow before extended exploration.

### Parties, downed players, and death

Party play changes friendly fire, shared experience, group information, and travel. Downed and dead players follow multiple rescue and recovery systems; see [Death, revival, graves, and respawning](#death-revival-graves-and-respawning).

## Part II — Character and Core Systems

### Races and racial traits

- **Iourus Races** — Adds selectable fantasy races with different traits.

Race selection defines persistent character traits and belongs at the beginning of character setup.

### Classes and playstyles

#### RPG classes

- **Type:** Integrated character-progression suite
- **Primary topic:** Classes and playstyles
- **Also affects:** Combat, Character, Skills, Magic, Equipment
- **Core idea:** Combines Archer, Paladin, Priest, Rogue, Warrior, and Wizard playstyles with class equipment and related progression.
- **Future guide:** Explain class selection, resources, abilities, equipment expectations, and related controls.


### Skill trees and character progression

- **Skill Tree (RPG Series)** — Lets a player specialize their chosen class by unlocking class-specific abilities.
- **Puffish Skills** — Adds a separate configurable skill-progression system.
- **Skill Perks** — Turns experience levels into a branching set of survival and mobility perks.

Skill trees and perk systems extend class and general character progression. Canonical entries are consolidated during the catalog pass below.

### Abilities, spells, runes, and resources

- **Runes** — Adds craftable ammunition consumed by class spells.

Spellcasting classes use abilities, spell resources, and runes. This chapter will document activation, resource use, and controls without enumerating every spell.

### Combat controls and dodge rolling

- **Better Combat** — Replaces vanilla melee timing with animated attacks, weapon combos, dual-wield support, and improved hit detection.
- **Combat Roll** — Adds a dodge roll with its own attributes and enchantments.

### Weapons, armor, jewelry, and relics

- **Alex's Caves equipment** — Exploration rewards support new combat options. See [Alex's Caves](#alexs-caves).
- **Stellarity equipment** — End progression includes additional equipment systems. See [Stellarity](#stellarity).

- **Arsenal** — Places legendary RPG weapons behind exploration and combat rewards instead of ordinary crafting.
- **Armory** — Adds RPG armor sets with distinct designs and set bonuses.
- **Jewelry** — Adds equippable magical jewelry.
- **Relics** — Adds powerful trinkets intended to change character builds.
- **Too Many Bows** — Expands bow choices and ranged combat equipment.
- **Arrow+** — Adds additional arrow types.
- **Shield Upgrades** — Adds stronger shields with special behavior.
- **Enchantments collection** — Adds Ice, Air Jump, Attack Speed, Critical Strike, Double Hit, Downfall, Life Steal, Fangs, Spiky, Thunder Strike, and True Shot mechanics.

### Enchanting and equipment improvement

- **Enchanting Infuser** — Replaces random enchanting with more direct enchantment selection.
- **Easy Anvils** — Improves anvil behavior and interaction.
- **Easy Magic** — Improves the enchanting-table interface and workflow.
- **Universal Enchants** — Broadens which tools and weapons can accept existing enchantments.
- **DarkSmithing** — Adds custom ways to craft smithing templates.
- **OneKeyMiner** — Adds vein mining, crop harvesting, and automatic replanting.
- **Armor Quick Swap** — Swaps complete armor sets quickly from an inventory or armor stand.
- **Target Dummy** — Provides a craftable target for testing damage and character builds.
- **Elytra Trims** — Extends armor trim customization to elytra.
- **Naturally Trimmed** — Makes trimmed equipment appear through ordinary world and loot progression.
- **Armor Trim Item Fix** — Makes inventory icons reflect the actual trim applied to armor.
- **Fletching Recipe** — Makes the fletching table usable for crafting ordinary and special explosive arrows.

### Death, revival, graves, and respawning

- **PlayerRevive** — Gives downed players a short rescue window before normal death handling completes.
- **Better Revive** — Adds long-term revival, graves, potion-based rescue, and a compass that leads back to lost items.
- **Better Respawn** — Respawns a player near their death location instead of relying only on the vanilla flow.

### Parties and multiplayer cooperation

- **Better Party** — Adds public and private parties, roles, shared nearby experience, friendly-fire protection, party chat, and a party HUD.

Party creation, roles, protection, shared experience, communication, and group travel are documented here and summarized in Start Here.

## Part III — Adventure and Challenge

### Bosses and major encounters

- **Stellarity encounters** — The End expansion includes major combat progression. See [Stellarity](#stellarity).

- **World Bosses** — Adds boss shrines, summoning rituals, raid caches, large encounters, and Worldbreaker equipment.
- **Bosslike Ender Dragon** — Reworks the dragon into a staged fight that scales with the number of players.
- **Ultimate Warden** — Gives the Warden a dedicated dungeon and expanded boss encounter.
- **Gateway of Doom** — Opens configurable, wave-based combat gateways with timed challenges, bosses, and rewards.

### Invasions and wave events

- **Invasion** — Starts increasingly difficult base-defense events whose enemies can build, dig, and adapt.
- **Illager Invasion** — Expands the hostile illager roster.
- **It Takes a Pillage** — Adds pillager camps, fortresses, encounters, and loot.

Invasions and gateway encounters escalate through organized waves and reward preparation, group coordination, and defensive building.

### Dangerous nights and environmental threats

#### Withered Lands

- **Type:** Integrated adventure expansion
- **Primary topic:** Dangerous nights and environmental threats
- **Also affects:** Combat, Events, Creatures, Structures, Equipment
- **Core idea:** Makes exploration more hostile through wither-themed enemies, behaviors, encounters, and rewards.
- **Future guide:** Explain threat escalation, preparation, encounters, and rewards without enumerating every enemy.
#### The Graveyard

- **Type:** Integrated adventure expansion
- **Primary topic:** Dangerous nights and environmental threats
- **Also affects:** Structures, Creatures, Combat, Equipment
- **Core idea:** Adds graveyard-themed locations, enemies, atmosphere, and adventure rewards.
- **Future guide:** Explain discovery, hazards, encounters, and notable mechanics.

- **Blood Moon** — Creates occasional nights with much heavier danger.
- **The Darkness Will Find You** — Makes Deep Dark exploration more punishing and less predictable.
- **Armored Foes** — Allows a wider range of hostile mobs to spawn with equipment.

- **Somnora** — Lets time pass naturally during sleep instead of instantly skipping the night.

### Travel, maps, compasses, and teleportation

- **Waystones** — Adds discoverable and craftable fast-travel points.
- **Bifrost Teleport** — Adds named-marker teleportation using mythic weapons and cinematic Bifrost effects.
- **Explorer's Compass** — Locates structures.
- **Nature's Compass** — Locates biomes.
- **Xaero's World Map** — Adds a self-writing, full-screen world map.
- **Xaero's Map Multiplayer** — Adds multiplayer-oriented features to Xaero's maps.

### Integrated world and dimension expansions

#### Alex's Caves

- **Type:** Integrated world expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** Combat, Equipment, World Generation, Creatures
- **Core idea:** Adds rare underground ecosystems with their own creatures, materials, equipment, and progression.
- **Future guide:** Explain discovery, preparation, major mechanics, controls, configuration, and relationships to other systems.
#### Better Nether

- **Type:** Integrated dimension expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** World Generation, Structures, Creatures, Equipment
- **Core idea:** Expands Nether terrain, biomes, vegetation, structures, creatures, and exploration rewards.
- **Future guide:** Explain dimension changes, major hazards, navigation, and progression-relevant mechanics.
#### Stellarity

- **Type:** Integrated dimension expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** World Generation, Structures, Creatures, Equipment, Bosses
- **Core idea:** Expands the End with new terrain, structures, enemies, equipment, and progression.
- **Future guide:** Explain access, exploration rules, major encounters, and important system interactions.

- **Biomes O' Plenty** — Adds more than 50 biomes plus matching plants and blocks.
- **Terralith** — Rebuilds Overworld terrain around almost 100 realistic and light-fantasy biomes made from vanilla blocks.
- **William Wythers' Overhauled Overworld** — Reinterprets vanilla biomes with larger, more realistic, and atmospheric terrain.
- **Dungeons Dimensions: Nether** — Brings Minecraft Dungeons-inspired detail and encounters to the Nether.
- **Nullscape** — Reworks End terrain and island generation.
- **Clear End City** — Adds cinematic End structures, void gardens, crashed citadels, and new loot locations.
- **Serene Seasons** — Adds seasonal color, temperature, and environmental changes.
- **Abyssal Ocean** — Generates rare, extremely deep offshore ocean regions that become darker toward bedrock.

### Structures and dungeons

- **Integrated-expansion structures** — Several world expansions add structures as part of a larger ecosystem. See [Alex's Caves](#alexs-caves), [Better Nether](#better-nether), [Stellarity](#stellarity), and [The Graveyard](#the-graveyard).

- **Dungeons and Taverns** — Adds taverns, ruins, camps, and dungeons throughout the world.
- **Dungeons and Taverns overhauls** — Rebuilds ancient cities, mineshafts, pillager outposts, and strongholds.
- **Awesome Dungeon collection** — Adds combat dungeons to the Overworld, oceans, and End.
- **Moog's structure collection** — Adds bountiful structures, End and Nether landmarks, ocean structures, reimagined temples, and voyager structures.
- **Towns and Towers** — Expands villages, pillager outposts, and ships while keeping a vanilla-like style.
- **Explorify** — Adds lightweight exploration structures designed to blend with vanilla generation.
- **ATi Structures: Vanilla Edition** — Adds a collection of custom structures and dungeons built with vanilla materials.
- **Much More Dungeons** — Adds more dungeon layouts and exploration targets.
- **Towers of Chambers** — Turns trial chambers into large, vertical combat towers.
- **Underground Villages** — Generates functioning villages below ground.
- **Ocean Lily Pad Village** — Generates villages built across the ocean surface.
- **Antique Trading Ship** — Places a discoverable villager trading ship in ocean biomes.
- **Hopo Better Underwater Ruins** — Replaces small underwater ruins with larger and more interesting sites.
- **Improved Village Placement** — Chooses terrain that produces more natural village layouts.
- **Gazebos** — Adds village gazebos that contain small spell libraries.

### Creatures and hostile mobs

- **Alex's Caves creatures** — Cave-specific creatures belong to the broader cave ecosystem. See [Alex's Caves](#alexs-caves).
- **Better Nether creatures** — Nether creatures are documented with their dimension systems. See [Better Nether](#better-nether).
- **Stellarity creatures** — End creatures are documented with their dimension progression. See [Stellarity](#stellarity).

#### Shroomcraft

- **Type:** Integrated adventure expansion
- **Primary topic:** Creatures and hostile mobs
- **Also affects:** Creatures, Farming, Food, Building
- **Core idea:** Adds mushroom creatures, crops, colorful shroomwood, and connected survival content.
- **Future guide:** Explain its ecosystem, cultivation loop, creature interactions, and building uses.

- **Alex's Mobs** — Adds a large roster of real and fantasy creatures with distinct behaviors and rewards.
- **Animal Garden collection** — Adds bull sharks, capybaras, fennec foxes, harp seals, hippopotamuses, lions, narwhals, owls, prairie dogs, red pandas, red river hogs, spotted hyenas, springhares, and yellow mongooses.
- **Ender Zoology** — Adds hostile mobs designed to feel like extensions of the vanilla roster.
- **Lullaby's Mobs** — Adds additional custom hostile creatures.
- **Mutants and Zombies** — Adds stronger mutant zombie enemies.
- **Moblets** — Adds small and baby variants of existing mobs.
- **Mounts and Monsters** — Adds rideable creatures and new monsters.
- **Craftable Creatures Evolution** — Makes mobs and mob spawners obtainable through crafting-based progression.
- **Ribbits** — Adds social frog-like villagers to swamps.
- **Guard Ribbits** — Adds armed protectors for Ribbit settlements.
- **Guard Villagers** — Adds armed defenders to ordinary villages.
- **Goblin Traders** — Adds wandering goblins with unusual trades.

### Companions, pets, villagers, and settlements

- **Companions: Dogfolk** — Adds dogfolk companions.
- **Wild Pets** — Makes polar bears, pandas, and foxes tameable; tamed bears can also be ridden.
- **Puffsprout** — Adds a small companion that learns over time.
- **Animal Pen** — Stores groups of farm animals in a single block to simplify husbandry and reduce entity load.
- **Compact Villagers** — Lets players carry villagers and trade through compact booths.
- **Name Tag Upgrade** — Allows on-the-go renaming and adds convenience improvements to name-tag use.
- **Pet Vault** — Gives pets portable storage.
- **Pet Status** — Exposes useful health and status information for owned pets.
- **Iron Wolf Armor** — Adds protective equipment for wolves.
- **Party Creepers** — Replaces destructive creeper damage with a decorative party-style explosion.

## Part IV — Survival and Creation

### Farming, food, and cooking

- **Shroomcraft cultivation** — Mushroom crops connect to a wider creature and building ecosystem. See [Shroomcraft](#shroomcraft).
- **Starcatcher food and collection** — Fishing rewards connect collection systems with food and equipment. See [Starcatcher](#starcatcher).

#### Farmer's Delight

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Farming, food, and cooking
- **Also affects:** Farming, Food, Equipment, Building
- **Core idea:** Expands farming, cooking tools, food preparation, meals, and kitchen-centered survival.
- **Future guide:** Explain the cooking loop, important workstations, food mechanics, and integrations.

- **Kaleidoscope Cookery** — Adds more ingredients and recipes around the cooking loop.
- **Better Rotten Flesh** — Adds useful ways to process and consume rotten flesh and new zombie-feeding behavior.
- **Universal Bone Meal** — Makes bone meal work on a wider range of plants.
- **No Crop Destruction** — Prevents farmland and crops from being trampled during normal play.

### Fishing and collection systems

#### Starcatcher

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Fishing and collection systems
- **Also affects:** Food, Equipment, Events, Multiplayer
- **Core idea:** Adds collectible fish, fishing minigames, equipment, trophies, tournaments, and a guidebook.
- **Future guide:** Explain the fishing loop, minigame controls, collections, tournaments, and configuration.

Fishing-focused progression and collection systems receive their canonical entries here.

### Potions and alchemy

#### Alchemia

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Potions and alchemy
- **Also affects:** Magic, Equipment, Food
- **Core idea:** Reimagines potion brewing as a simplified, Potion Craft-inspired alchemy system.
- **Future guide:** Explain the alchemy loop, interaction controls, outputs, and relationship to ordinary potion systems.
- **Potion Time Stacker** — Lets repeated potion effects extend their remaining duration.
- **Potions Stack** — Allows ordinary potions to stack in small groups.

Potion crafting, effect duration, stacking, and alchemy mechanics receive their canonical entries here.

### Building and decoration

#### Macaw's building collection

- **Type:** Integrated building expansion
- **Primary topic:** Building and decoration
- **Also affects:** Building, Storage
- **Core idea:** Adds coordinated bridges, doors, fences, furniture, decorations, lights, paintings, paths, roofs, stairs, trapdoors, and windows.
- **Future guide:** Explain the collection at a family level, focusing on building workflows rather than block-by-block listings.

- **Display Delight** — Adds decorative ways to display food and related items.
- **Functional Sculptures** — Adds a collection of craftable statues, monuments, and memorials for decorative builds.
- **Armor Statues** — Unlocks detailed posing and customization for armor stands.
- **Stoneworks** — Adds building variants for vanilla stone families.
- **Effortless Building** — Adds tools for placing and editing large shapes and repeated structures quickly.
- **Pro Placer** — Improves precise placement, reach-based building, and bridging.
- **Stonecutting Upgrade** — Expands the stonecutter interface, remembers recipes, and supports quick material refills.
- **Arcane Lanterns** — Uses catalysts to give lanterns different magical area effects.
- **Barricades** — Adds defensive barricades, contact-damage obstacles, and resettable traps.

### Storage and inventory management

- **Sophisticated Backpacks** — Adds upgradeable portable storage with functional upgrade slots.
- **Tom's Simple Storage** — Joins ordinary containers into one searchable storage network.
- **Linked Chests** — Lets separate chests share an inventory.
- **Easy Shulker Boxes** — Allows shulker boxes to be used directly from the inventory.
- **Locked In Slots** — Protects chosen inventory slots from accidental movement or replacement.
- **Tool Belt** — Moves frequently used tools out of the main inventory while keeping them accessible.

### Item transport and logistics

- **Golden Hopper** — Adds a hopper with configurable filtering behavior.
- **Hopper Gadgetry** — Adds more capable hopper controls and item-routing tools.
- **Sophisticated Inventory Interactions** — Brings search, sorting, and transfer controls to eligible container screens.
- **Sophisticated Item Actions** — Finds nearby inventories containing an item and supports direct restocking or depositing.

Storage transfer, hoppers, filtering, restocking, and deposit mechanics receive their canonical entries here.

### Vehicles and travel equipment

- **Seaworthy Boats** — Adds faster, tougher boat tiers and a shipyard repair loop.
- **Vehicle Upgrade** — Improves the reliability and capabilities of mounts and other rideable vehicles.

Vehicle and boat upgrades are cross-referenced here from the travel chapter.

### Trading and multiplayer economy

- **Market Board** — Provides a shared multiplayer marketplace for player trading.

Player markets and unusual traders are cross-referenced here from their canonical multiplayer or creature entries.

## Part V — Interface and Reference

### Information overlays and tooltips

- **AppleSkin** — Shows hunger and saturation information in the HUD.
- **Jade** — Identifies blocks and entities being looked at and shows relevant state information.
- **Effect Insights** — Explains active and available status effects.
- **Enchantment Insights** — Explains enchantments and their behavior.
- **Food Effect Tooltips** — Shows food-related effects before an item is eaten.
- **Floating Damage Indicators** — Displays combat damage numbers above affected entities.
- **Controlling** — Makes keybinds searchable and easier to diagnose.

### Recipe and loot information

- **Just Enough Items** — Provides searchable item, recipe, and usage views.
- **JEI Trades** — Adds villager trade information to the recipe browser.
- **Just Enough Filters** — Adds more ways to filter the item browser.
- **Advanced Loot Info** — Shows detailed loot-table and trade information in the recipe browser.
- **Advanced Worldgen Info** — Shows where world-generation features and resources can appear.
- **Show My Recipes** — Unlocks crafting recipes in the recipe book so players can discover what is available.

Recipe browsing, uses, trades, loot tables, and world-generation lookup tools receive their canonical entries here.

### HUD changes and customization

- **Hovering Hotbar** — Shifts the hotbar upward slightly so it appears to float above the screen edge.
- **Overflowing Bars** — Keeps health, armor, and similar HUD values readable beyond vanilla limits.
- **Paper Doll** — Adds a small on-screen view of the player's character and equipment.

### Cosmetic and animation changes

- **Eating Animation** — Adds visible first-person eating and drinking animations.
- **Distinct Potions** — Makes potion types easier to distinguish visually.
- **Quick Skin** — Makes changing player skins faster.
- **PatPat** — Adds a friendly player-to-player and player-to-creature pat interaction.
- **Kingdom Cats Replacer** — Replaces vanilla cat visuals with seven animated models while preserving vanilla cat behavior.

Cosmetic replacements, equipment appearance, and first-person animation changes receive their canonical entries here.

### Optional client features

- **Coordinates Display** — Shows the player's coordinates and related navigation information on a configurable HUD.
- **Iris** — Enables shader-pack support.
- **Sodium** — Replaces the renderer to improve frame rate and reduce rendering stutter.

### Alphabetical mod index

- [Abyssal Ocean](#integrated-world-and-dimension-expansions)
- [Advanced Loot Info](#recipe-and-loot-information)
- [Advanced Worldgen Info](#recipe-and-loot-information)
- [Alchemia](#alchemia)
- [Alex's Caves](#alexs-caves)
- [Alex's Mobs](#creatures-and-hostile-mobs)
- [Animal Garden collection](#creatures-and-hostile-mobs)
- [Animal Pen](#companions-pets-villagers-and-settlements)
- [Antique Trading Ship](#structures-and-dungeons)
- [AppleSkin](#information-overlays-and-tooltips)
- [Arcane Lanterns](#building-and-decoration)
- [Armor Quick Swap](#enchanting-and-equipment-improvement)
- [Armor Statues](#building-and-decoration)
- [Armor Trim Item Fix](#enchanting-and-equipment-improvement)
- [Armored Foes](#dangerous-nights-and-environmental-threats)
- [Armory](#weapons-armor-jewelry-and-relics)
- [Arrow+](#weapons-armor-jewelry-and-relics)
- [Arsenal](#weapons-armor-jewelry-and-relics)
- [ATi Structures: Vanilla Edition](#structures-and-dungeons)
- [Awesome Dungeon collection](#structures-and-dungeons)
- [Barricades](#building-and-decoration)
- [Better Combat](#combat-controls-and-dodge-rolling)
- [Better Nether](#better-nether)
- [Better Party](#parties-and-multiplayer-cooperation)
- [Better Respawn](#death-revival-graves-and-respawning)
- [Better Revive](#death-revival-graves-and-respawning)
- [Better Rotten Flesh](#farming-food-and-cooking)
- [Bifrost Teleport](#travel-maps-compasses-and-teleportation)
- [Biomes O' Plenty](#integrated-world-and-dimension-expansions)
- [Blood Moon](#dangerous-nights-and-environmental-threats)
- [Bosslike Ender Dragon](#bosses-and-major-encounters)
- [Clear End City](#integrated-world-and-dimension-expansions)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [Compact Villagers](#companions-pets-villagers-and-settlements)
- [Companions: Dogfolk](#companions-pets-villagers-and-settlements)
- [Controlling](#information-overlays-and-tooltips)
- [Coordinates Display](#optional-client-features)
- [Craftable Creatures Evolution](#creatures-and-hostile-mobs)
- [DarkSmithing](#enchanting-and-equipment-improvement)
- [Display Delight](#building-and-decoration)
- [Distinct Potions](#cosmetic-and-animation-changes)
- [Dungeons and Taverns](#structures-and-dungeons)
- [Dungeons and Taverns overhauls](#structures-and-dungeons)
- [Dungeons Dimensions: Nether](#integrated-world-and-dimension-expansions)
- [Easy Anvils](#enchanting-and-equipment-improvement)
- [Easy Magic](#enchanting-and-equipment-improvement)
- [Easy Shulker Boxes](#storage-and-inventory-management)
- [Eating Animation](#cosmetic-and-animation-changes)
- [Effect Insights](#information-overlays-and-tooltips)
- [Effortless Building](#building-and-decoration)
- [Elytra Trims](#enchanting-and-equipment-improvement)
- [Enchanting Infuser](#enchanting-and-equipment-improvement)
- [Enchantment Insights](#information-overlays-and-tooltips)
- [Enchantments collection](#weapons-armor-jewelry-and-relics)
- [Ender Zoology](#creatures-and-hostile-mobs)
- [Explorer's Compass](#travel-maps-compasses-and-teleportation)
- [Explorify](#structures-and-dungeons)
- [Farmer's Delight](#farmers-delight)
- [Fletching Recipe](#enchanting-and-equipment-improvement)
- [Floating Damage Indicators](#information-overlays-and-tooltips)
- [Food Effect Tooltips](#information-overlays-and-tooltips)
- [Functional Sculptures](#building-and-decoration)
- [Gateway of Doom](#bosses-and-major-encounters)
- [Gazebos](#structures-and-dungeons)
- [Goblin Traders](#creatures-and-hostile-mobs)
- [Golden Hopper](#item-transport-and-logistics)
- [Guard Ribbits](#creatures-and-hostile-mobs)
- [Guard Villagers](#creatures-and-hostile-mobs)
- [Hopo Better Underwater Ruins](#structures-and-dungeons)
- [Hopper Gadgetry](#item-transport-and-logistics)
- [Hovering Hotbar](#hud-changes-and-customization)
- [Illager Invasion](#invasions-and-wave-events)
- [Improved Village Placement](#structures-and-dungeons)
- [Invasion](#invasions-and-wave-events)
- [Iourus Races](#races-and-racial-traits)
- [Iris](#optional-client-features)
- [Iron Wolf Armor](#companions-pets-villagers-and-settlements)
- [It Takes a Pillage](#invasions-and-wave-events)
- [Jade](#information-overlays-and-tooltips)
- [JEI Trades](#recipe-and-loot-information)
- [Jewelry](#weapons-armor-jewelry-and-relics)
- [Just Enough Filters](#recipe-and-loot-information)
- [Just Enough Items](#recipe-and-loot-information)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Kingdom Cats Replacer](#cosmetic-and-animation-changes)
- [Linked Chests](#storage-and-inventory-management)
- [Locked In Slots](#storage-and-inventory-management)
- [Lullaby's Mobs](#creatures-and-hostile-mobs)
- [Macaw's building collection](#macaws-building-collection)
- [Market Board](#trading-and-multiplayer-economy)
- [Moblets](#creatures-and-hostile-mobs)
- [Moog's structure collection](#structures-and-dungeons)
- [Mounts and Monsters](#creatures-and-hostile-mobs)
- [Much More Dungeons](#structures-and-dungeons)
- [Mutants and Zombies](#creatures-and-hostile-mobs)
- [Name Tag Upgrade](#companions-pets-villagers-and-settlements)
- [Naturally Trimmed](#enchanting-and-equipment-improvement)
- [Nature's Compass](#travel-maps-compasses-and-teleportation)
- [No Crop Destruction](#farming-food-and-cooking)
- [Nullscape](#integrated-world-and-dimension-expansions)
- [Ocean Lily Pad Village](#structures-and-dungeons)
- [OneKeyMiner](#enchanting-and-equipment-improvement)
- [Overflowing Bars](#hud-changes-and-customization)
- [Paper Doll](#hud-changes-and-customization)
- [Party Creepers](#companions-pets-villagers-and-settlements)
- [PatPat](#cosmetic-and-animation-changes)
- [Pet Status](#companions-pets-villagers-and-settlements)
- [Pet Vault](#companions-pets-villagers-and-settlements)
- [PlayerRevive](#death-revival-graves-and-respawning)
- [Potion Time Stacker](#potions-and-alchemy)
- [Potions Stack](#potions-and-alchemy)
- [Pro Placer](#building-and-decoration)
- [Puffish Skills](#skill-trees-and-character-progression)
- [Puffsprout](#companions-pets-villagers-and-settlements)
- [Quick Skin](#cosmetic-and-animation-changes)
- [Relics](#weapons-armor-jewelry-and-relics)
- [Ribbits](#creatures-and-hostile-mobs)
- [RPG classes](#rpg-classes)
- [Runes](#abilities-spells-runes-and-resources)
- [Seaworthy Boats](#vehicles-and-travel-equipment)
- [Serene Seasons](#integrated-world-and-dimension-expansions)
- [Shield Upgrades](#weapons-armor-jewelry-and-relics)
- [Show My Recipes](#recipe-and-loot-information)
- [Shroomcraft](#shroomcraft)
- [Skill Perks](#skill-trees-and-character-progression)
- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Sodium](#optional-client-features)
- [Somnora](#dangerous-nights-and-environmental-threats)
- [Sophisticated Backpacks](#storage-and-inventory-management)
- [Sophisticated Inventory Interactions](#item-transport-and-logistics)
- [Sophisticated Item Actions](#item-transport-and-logistics)
- [Starcatcher](#starcatcher)
- [Stellarity](#stellarity)
- [Stonecutting Upgrade](#building-and-decoration)
- [Stoneworks](#building-and-decoration)
- [Target Dummy](#enchanting-and-equipment-improvement)
- [Terralith](#integrated-world-and-dimension-expansions)
- [The Darkness Will Find You](#dangerous-nights-and-environmental-threats)
- [The Graveyard](#the-graveyard)
- [Tom's Simple Storage](#storage-and-inventory-management)
- [Too Many Bows](#weapons-armor-jewelry-and-relics)
- [Tool Belt](#storage-and-inventory-management)
- [Towers of Chambers](#structures-and-dungeons)
- [Towns and Towers](#structures-and-dungeons)
- [Ultimate Warden](#bosses-and-major-encounters)
- [Underground Villages](#structures-and-dungeons)
- [Universal Bone Meal](#farming-food-and-cooking)
- [Universal Enchants](#enchanting-and-equipment-improvement)
- [Vehicle Upgrade](#vehicles-and-travel-equipment)
- [Waystones](#travel-maps-compasses-and-teleportation)
- [Wild Pets](#companions-pets-villagers-and-settlements)
- [William Wythers' Overhauled Overworld](#integrated-world-and-dimension-expansions)
- [Withered Lands](#withered-lands)
- [World Bosses](#bosses-and-major-encounters)
- [Xaero's Map Multiplayer](#travel-maps-compasses-and-teleportation)
- [Xaero's World Map](#travel-maps-compasses-and-teleportation)

### Gameplay-tag index

#### Combat

- [Better Combat](#combat-controls-and-dodge-rolling)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [RPG classes](#rpg-classes)
- [World Bosses](#bosses-and-major-encounters)
- [Invasion](#invasions-and-wave-events)

#### Character

- [Iourus Races](#races-and-racial-traits)
- [RPG classes](#rpg-classes)
- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Puffish Skills](#skill-trees-and-character-progression)
- [Skill Perks](#skill-trees-and-character-progression)

#### Skills

- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Puffish Skills](#skill-trees-and-character-progression)
- [Skill Perks](#skill-trees-and-character-progression)

#### Magic

- [RPG classes](#rpg-classes)
- [Runes](#abilities-spells-runes-and-resources)
- [Alchemia](#alchemia)
- [Arcane Lanterns](#building-and-decoration)

#### Equipment

- [Arsenal](#weapons-armor-jewelry-and-relics)
- [Armory](#weapons-armor-jewelry-and-relics)
- [Jewelry](#weapons-armor-jewelry-and-relics)
- [Relics](#weapons-armor-jewelry-and-relics)
- [Alex's Caves](#alexs-caves)
- [Stellarity](#stellarity)

#### Bosses

- [World Bosses](#bosses-and-major-encounters)
- [Bosslike Ender Dragon](#bosses-and-major-encounters)
- [Ultimate Warden](#bosses-and-major-encounters)
- [Gateway of Doom](#bosses-and-major-encounters)

#### Events

- [Invasion](#invasions-and-wave-events)
- [Blood Moon](#dangerous-nights-and-environmental-threats)
- [Illager Invasion](#invasions-and-wave-events)
- [It Takes a Pillage](#invasions-and-wave-events)
- [Starcatcher](#starcatcher)

#### World Generation

- [Alex's Caves](#alexs-caves)
- [Biomes O' Plenty](#integrated-world-and-dimension-expansions)
- [Terralith](#integrated-world-and-dimension-expansions)
- [Better Nether](#better-nether)
- [Nullscape](#integrated-world-and-dimension-expansions)
- [Stellarity](#stellarity)

#### Structures

- [Dungeons and Taverns](#structures-and-dungeons)
- [Moog's structure collection](#structures-and-dungeons)
- [Towns and Towers](#structures-and-dungeons)
- [The Graveyard](#the-graveyard)

#### Creatures

- [Alex's Mobs](#creatures-and-hostile-mobs)
- [Animal Garden collection](#creatures-and-hostile-mobs)
- [Shroomcraft](#shroomcraft)
- [Ender Zoology](#creatures-and-hostile-mobs)

#### Companions

- [Companions: Dogfolk](#companions-pets-villagers-and-settlements)
- [Wild Pets](#companions-pets-villagers-and-settlements)
- [Puffsprout](#companions-pets-villagers-and-settlements)
- [Pet Vault](#companions-pets-villagers-and-settlements)
- [Pet Status](#companions-pets-villagers-and-settlements)

#### Farming

- [Farmer's Delight](#farmers-delight)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Shroomcraft](#shroomcraft)
- [Universal Bone Meal](#farming-food-and-cooking)

#### Food

- [Farmer's Delight](#farmers-delight)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Starcatcher](#starcatcher)
- [Better Rotten Flesh](#farming-food-and-cooking)

#### Building

- [Macaw's building collection](#macaws-building-collection)
- [Effortless Building](#building-and-decoration)
- [Stoneworks](#building-and-decoration)
- [Pro Placer](#building-and-decoration)

#### Storage

- [Sophisticated Backpacks](#storage-and-inventory-management)
- [Tom's Simple Storage](#storage-and-inventory-management)
- [Linked Chests](#storage-and-inventory-management)
- [Tool Belt](#storage-and-inventory-management)

#### Travel

- [Waystones](#travel-maps-compasses-and-teleportation)
- [Bifrost Teleport](#travel-maps-compasses-and-teleportation)
- [Xaero's World Map](#travel-maps-compasses-and-teleportation)
- [Seaworthy Boats](#vehicles-and-travel-equipment)
- [Vehicle Upgrade](#vehicles-and-travel-equipment)

#### Multiplayer

- [Better Party](#parties-and-multiplayer-cooperation)
- [Market Board](#trading-and-multiplayer-economy)
- [PlayerRevive](#death-revival-graves-and-respawning)
- [Better Revive](#death-revival-graves-and-respawning)

#### Controls

- [Better Combat](#combat-controls-and-dodge-rolling)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [OneKeyMiner](#enchanting-and-equipment-improvement)
- [Effortless Building](#building-and-decoration)
- [Tool Belt](#storage-and-inventory-management)

#### HUD

- [Paper Doll](#hud-changes-and-customization)
- [Coordinates Display](#optional-client-features)
- [Better Party](#parties-and-multiplayer-cooperation)
- [Hovering Hotbar](#hud-changes-and-customization)
- [Overflowing Bars](#hud-changes-and-customization)
