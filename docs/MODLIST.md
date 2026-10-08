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

Race selection defines persistent character traits and belongs at the beginning of character setup.

### Classes and playstyles

- **RPG classes** — Archers, Paladins, Rogues & Warriors, and Wizards establish distinct ranged, holy, martial, and magical playstyles.
- **Runes** — Adds craftable ammunition consumed by class spells.
- **Skill Tree (RPG Series)** — Lets a player specialize their chosen class by unlocking class-specific abilities.
- **Puffish Skills** — Adds a separate configurable skill-progression system.
- **Skill Perks** — Turns experience levels into a branching set of survival and mobility perks.
- **Iourus Races** — Adds selectable fantasy races with different traits.

### Skill trees and character progression

Skill trees and perk systems extend class and general character progression. Canonical entries are consolidated during the catalog pass below.

### Abilities, spells, runes, and resources

Spellcasting classes use abilities, spell resources, and runes. This chapter will document activation, resource use, and controls without enumerating every spell.

### Combat controls and dodge rolling

- **Better Combat** — Replaces vanilla melee timing with animated attacks, weapon combos, dual-wield support, and improved hit detection.
- **Combat Roll** — Adds a dodge roll with its own attributes and enchantments.

### Weapons, armor, jewelry, and relics

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
- **Show My Recipes** — Unlocks crafting recipes in the recipe book so players can discover what is available.

### Death, revival, graves, and respawning

- **PlayerRevive** — Gives downed players a short rescue window before normal death handling completes.
- **Better Revive** — Adds long-term revival, graves, potion-based rescue, and a compass that leads back to lost items.
- **Better Respawn** — Respawns a player near their death location instead of relying only on the vanilla flow.
- **Better Party** — Adds public and private parties, roles, shared nearby experience, friendly-fire protection, party chat, and a party HUD.
- **Market Board** — Provides a shared multiplayer marketplace for player trading.

### Parties and multiplayer cooperation

Party creation, roles, protection, shared experience, communication, and group travel are documented here and summarized in Start Here.

## Part III — Adventure and Challenge

### Bosses and major encounters

- **World Bosses** — Adds boss shrines, summoning rituals, raid caches, large encounters, and Worldbreaker equipment.
- **Bosslike Ender Dragon** — Reworks the dragon into a staged fight that scales with the number of players.
- **Ultimate Warden** — Gives the Warden a dedicated dungeon and expanded boss encounter.
- **Gateway of Doom** — Opens configurable, wave-based combat gateways with timed challenges, bosses, and rewards.
- **Invasion** — Starts increasingly difficult base-defense events whose enemies can build, dig, and adapt.

### Invasions and wave events

Invasions and gateway encounters escalate through organized waves and reward preparation, group coordination, and defensive building.

### Dangerous nights and environmental threats

- **Blood Moon** — Creates occasional nights with much heavier danger.
- **The Darkness Will Find You** — Makes Deep Dark exploration more punishing and less predictable.
- **Withered Lands** — Adds hostile early-game encounters and wither-themed threats that reward preparation.
- **The Graveyard** — Adds graveyard-themed structures, enemies, and atmosphere.
- **Illager Invasion** — Expands the hostile illager roster.
- **It Takes a Pillage** — Adds pillager camps, fortresses, encounters, and loot.
- **Armored Foes** — Allows a wider range of hostile mobs to spawn with equipment.

- **Somnora** — Lets time pass naturally during sleep instead of instantly skipping the night.

### Travel, maps, compasses, and teleportation

- **Waystones** — Adds discoverable and craftable fast-travel points.
- **Bifrost Teleport** — Adds named-marker teleportation using mythic weapons and cinematic Bifrost effects.
- **Explorer's Compass** — Locates structures.
- **Nature's Compass** — Locates biomes.
- **Xaero's World Map** — Adds a self-writing, full-screen world map.
- **Xaero's Map Multiplayer** — Adds multiplayer-oriented features to Xaero's maps.
- **Seaworthy Boats** — Adds faster, tougher boat tiers and a shipyard repair loop.
- **Vehicle Upgrade** — Improves the reliability and capabilities of mounts and other rideable vehicles.

### Integrated world and dimension expansions

- **Alex's Caves** — Hides rare cave biomes with unique creatures, resources, and equipment beneath the Overworld.
- **Biomes O' Plenty** — Adds more than 50 biomes plus matching plants and blocks.
- **Terralith** — Rebuilds Overworld terrain around almost 100 realistic and light-fantasy biomes made from vanilla blocks.
- **William Wythers' Overhauled Overworld** — Reinterprets vanilla biomes with larger, more realistic, and atmospheric terrain.
- **Better Nether** — Expands Nether terrain, biomes, vegetation, and exploration.
- **Dungeons Dimensions: Nether** — Brings Minecraft Dungeons-inspired detail and encounters to the Nether.
- **Nullscape** — Reworks End terrain and island generation.
- **Stellarity** — Extensively expands the End with biomes, structures, enemies, equipment, and progression.
- **Clear End City** — Adds cinematic End structures, void gardens, crashed citadels, and new loot locations.
- **Serene Seasons** — Adds seasonal color, temperature, and environmental changes.
- **Abyssal Ocean** — Generates rare, extremely deep offshore ocean regions that become darker toward bedrock.

### Structures and dungeons

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
- **Shroomcraft** — Adds mushroom creatures, crops, colorful shroomwood, and related blocks.
- **Kingdom Cats Replacer** — Replaces vanilla cat visuals with seven animated models while preserving vanilla cat behavior.

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

- **Farmer's Delight** — Expands farming, cooking tools, meals, and food preparation.
- **Kaleidoscope Cookery** — Adds more ingredients and recipes around the cooking loop.
- **Alchemia** — Reimagines potion brewing as a simplified, Potion Craft-inspired alchemy system.
- **Starcatcher** — Adds collectible fish, fishing minigames, equipment, trophies, tournaments, and a guidebook.
- **Better Rotten Flesh** — Adds useful ways to process and consume rotten flesh and new zombie-feeding behavior.
- **Universal Bone Meal** — Makes bone meal work on a wider range of plants.
- **No Crop Destruction** — Prevents farmland and crops from being trampled during normal play.
- **Potion Time Stacker** — Lets repeated potion effects extend their remaining duration.
- **Potions Stack** — Allows ordinary potions to stack in small groups.

### Fishing and collection systems

Fishing-focused progression and collection systems receive their canonical entries here.

### Potions and alchemy

Potion crafting, effect duration, stacking, and alchemy mechanics receive their canonical entries here.

### Building and decoration

- **Macaw's building collection** — Adds bridges, doors, fences, furniture, holiday decorations, lights, paintings, paths, roofs, stairs, trapdoors, and windows.
- **Display Delight** — Adds decorative ways to display food and related items.
- **Functional Sculptures** — Adds a collection of craftable statues, monuments, and memorials for decorative builds.
- **Armor Statues** — Unlocks detailed posing and customization for armor stands.
- **Stoneworks** — Adds building variants for vanilla stone families.
- **Effortless Building** — Adds tools for placing and editing large shapes and repeated structures quickly.
- **Pro Placer** — Improves precise placement, reach-based building, and bridging.
- **Stonecutting Upgrade** — Expands the stonecutter interface, remembers recipes, and supports quick material refills.
- **Golden Hopper** — Adds a hopper with configurable filtering behavior.
- **Hopper Gadgetry** — Adds more capable hopper controls and item-routing tools.
- **Arcane Lanterns** — Uses catalysts to give lanterns different magical area effects.
- **Barricades** — Adds defensive barricades, contact-damage obstacles, and resettable traps.

### Storage and inventory management

- **Sophisticated Backpacks** — Adds upgradeable portable storage with functional upgrade slots.
- **Sophisticated Inventory Interactions** — Brings search, sorting, and transfer controls to eligible container screens.
- **Sophisticated Item Actions** — Finds nearby inventories containing an item and supports direct restocking or depositing.
- **Tom's Simple Storage** — Joins ordinary containers into one searchable storage network.
- **Linked Chests** — Lets separate chests share an inventory.
- **Easy Shulker Boxes** — Allows shulker boxes to be used directly from the inventory.
- **Locked In Slots** — Protects chosen inventory slots from accidental movement or replacement.
- **Tool Belt** — Moves frequently used tools out of the main inventory while keeping them accessible.

### Item transport and logistics

Storage transfer, hoppers, filtering, restocking, and deposit mechanics receive their canonical entries here.

### Vehicles and travel equipment

Vehicle and boat upgrades are cross-referenced here from the travel chapter.

### Trading and multiplayer economy

Player markets and unusual traders are cross-referenced here from their canonical multiplayer or creature entries.

## Part V — Interface and Reference

### Information overlays and tooltips

- **AppleSkin** — Shows hunger and saturation information in the HUD.
- **Jade** — Identifies blocks and entities being looked at and shows relevant state information.
- **Just Enough Items** — Provides searchable item, recipe, and usage views.
- **JEI Trades** — Adds villager trade information to the recipe browser.
- **Just Enough Filters** — Adds more ways to filter the item browser.
- **Advanced Loot Info** — Shows detailed loot-table and trade information in the recipe browser.
- **Advanced Worldgen Info** — Shows where world-generation features and resources can appear.
- **Effect Insights** — Explains active and available status effects.
- **Enchantment Insights** — Explains enchantments and their behavior.
- **Food Effect Tooltips** — Shows food-related effects before an item is eaten.
- **Floating Damage Indicators** — Displays combat damage numbers above affected entities.
- **Controlling** — Makes keybinds searchable and easier to diagnose.

### Recipe and loot information

Recipe browsing, uses, trades, loot tables, and world-generation lookup tools receive their canonical entries here.

### HUD changes and customization

- **Hovering Hotbar** — Shifts the hotbar upward slightly so it appears to float above the screen edge.
- **Overflowing Bars** — Keeps health, armor, and similar HUD values readable beyond vanilla limits.
- **Paper Doll** — Adds a small on-screen view of the player's character and equipment.
- **Eating Animation** — Adds visible first-person eating and drinking animations.
- **Distinct Potions** — Makes potion types easier to distinguish visually.
- **Quick Skin** — Makes changing player skins faster.
- **PatPat** — Adds a friendly player-to-player and player-to-creature pat interaction.

### Cosmetic and animation changes

Cosmetic replacements, equipment appearance, and first-person animation changes receive their canonical entries here.

### Optional client features

- **Coordinates Display** — Shows the player's coordinates and related navigation information on a configurable HUD.
- **Iris** — Enables shader-pack support.
- **Sodium** — Replaces the renderer to improve frame rate and reduce rendering stutter.

### Alphabetical mod index

The alphabetical index links every canonical user-facing mod or mod-family entry.

### Gameplay-tag index

The tag index links canonical entries through the approved gameplay vocabulary.
