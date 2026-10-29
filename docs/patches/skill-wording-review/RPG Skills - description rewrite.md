# RPG Skills - individual description rewrite

24 October 2026. **Approved and implemented: English tooltip wording installed in the four pack JARs. Gameplay is unchanged.**

## Direction from brainstorming

The goal is to understand a node at a glance: what it does and why to choose it. Use direct, simple English. Keep a useful damage value, buff duration or upgrade amount; remove repeated ranges, calculations and casting filler. Priest and Paladin are excluded.

The chosen draft style is **brief effect text with selective numbers**. Fully numberless descriptions hide useful upgrade benefits; copying every statistic recreates the current clutter. Most proposed entries are one sentence, occasionally two. Berserker keeps extra sentences because its damage penalty and fatal-hit consequences cannot be hidden safely.

Rules for this review:

- Start with the result: ?Deal,? ?Gain,? ?Summon,? ?Freeze,? or the affected skill's name.
- Describe an upgrade's added benefit. Do not repeat the parent skill's radius, damage or other upgrades.
- Keep meaningful percentages such as Battle Shout's damage-school power bonus. Remove unnecessary formulas, intermediate increments and repeated totals.
- Use short times such as `6s`. Let the existing statistics/activation panel show current cast time, cooldown, range and passive trigger whenever it already does.
- Keep material risks and exceptions: damage taken, movement penalties, limited hits, first-target stopping, excluded cooldown resets and ultimate drawbacks.
- Keep exact ward/pet scaling formulas and thresholds in the raw-attributes references, rather than cramming them into every tooltip.
- Keep names, IDs, stats, cooldowns, trigger chances, eligibility and party rules unchanged. If current wording is factually stale, correct the wording to match installed behavior.

**Battle Shout example**

> Demoralizing Shout grants you and party allies 8% more melee, ranged and magic power for 6s.

The radius sentence is removed. ?Power? is retained because the effect raises the corresponding attributes; it is not a blanket multiplier on every damage source, such as flat Bleed.

## Coverage

Every one of the **196 installed description keys** in scope has an individual before/after entry:

- **180 class entries:** six active skills, eighteen upgrades and six passives for each of the six covered paths.
- **7 wand/staff attacks:** included separately so Wizard descriptions are not missed.
- **9 internal helpers:** documented separately; not promoted into selectable nodes.

Ordinary attribute-only nodes already display their bonus in their title and are not additional free-text skill descriptions. Skill names, advancement descriptions, status-effect text, Priest and Paladin text are outside this draft. Existing yellow trigger/cooldown lines remain intact.

Percentages below are shown as players read them (`%`). A later language-file implementation must use the engine's required `%%` escaping. Full IDs and source hashes are in the accompanying `wording-review.json`, allowing an exact, reviewable language-only overlay later.

## Arcane

### Tier I - Arcane Missiles / Arcane Explosion

| Skill / node | Current description | Proposed description |
|---|---|---|
| Arcane Missiles | Channel 12 homing missiles, each dealing 26% Arcane damage. | Channel 12 homing missiles, each dealing 26% Arcane damage. |
| Improved Arcane Missiles | Arcane Missiles gains 10% more spell power. | Arcane Missiles gains 10% more power. |
| Conjured Missile | Arcane Missiles fires 1 additional missile with every third channel hit, starting with the first. A full channel fires 4 extra missiles. | Arcane Missiles fires 4 extra missiles during a full channel. |
| Crippling Missiles | Arcane Missiles slows enemies by 10% base movement speed per stack, up to 30% at 3 stacks. Lasts 4s, refreshed by hits. | Arcane Missiles slows enemies by 10% per stack, up to 30% for 4s. |
| Arcane Explosion | Create an explosion centered on the targeted enemy, dealing 260% Arcane damage within 7 blocks before upgrades. Echoing Blast lowers the damage and resets the cooldown on a hit. | Explode around your target, dealing 260% Arcane damage within 7 blocks. |
| Improved Arcane Explosion | Arcane Explosion radius increases from 7 to 9 blocks around the targeted enemy. | Arcane Explosion reaches 2 blocks farther. |
| Echoing Blast | Arcane Explosion deals 160% Arcane damage. Hitting an enemy resets its cooldown. | Arcane Explosion deals 160% damage, but hitting an enemy resets its cooldown. |
| Chain Detonation | Each Arcane Explosion hit causes another explosion after 0.5s, dealing 30% Arcane damage. | Arcane Explosion causes another blast after 0.5s, dealing 30% Arcane damage. |
| Fissile Magic | Explode for up to 40% Arcane damage within 5 blocks, excluding the struck enemy. Damage decreases with distance. | Explode for up to 40% Arcane damage near the target, without hitting it again. |
| Arcane Radiance | Restore health equal to 10% Arcane power. | Heal for 10% of your Arcane power. |

### Tier II - Arcane Beam / Arcane Barrage

| Skill / node | Current description | Proposed description |
|---|---|---|
| Arcane Beam | Channel 25 hits, each dealing 20% Arcane damage. Consumes all Arcane Charges. Charged Beam changes the channel and damage. | Channel a beam that deals 20% Arcane damage per hit. Consumes all Arcane Charges. |
| Improved Arcane Beam | +5% Arcane Beam critical chance. | +5% Arcane Beam critical chance. |
| Beam Exposure | Beam hits apply Arcane Exposure for 3s: +4% Arcane damage taken per level, up to 25 levels. | Beam hits make enemies take 4% more Arcane damage per stack, up to 100%, for 3s. |
| Charged Beam | Channel 50 hits. Damage grows from 10% to 15% Arcane power, gaining 0.15% per hit. Beam width starts at 130% and grows by 10% per hit, up to 500%. | Channel twice as long. The beam grows wider, and its damage rises from 10% to 15% per hit. |
| Arcane Barrage | Summon 3 Arcane Emitters that fire at your target or where you aim. Each lasts 6s. Bolts deal 40% of the emitter's own Arcane power; emitter power grows with your Arcane power. | Summon 3 emitters for 6s. They fire bolts at your target or aim, dealing 40% of their own Arcane power. |
| Improved Arcane Barrage | Arcane Barrage emitters gain 10% increased critical strike chance. | Arcane Emitters gain 10% critical chance. |
| Arcane Battery | Conjures an additional Arcane Emitter. | Summon one extra Arcane Emitter. |
| Attuned Emitters | Arcane Emitters fire faster, matching your spell haste. | Arcane Emitters fire faster with spell haste. |
| Arcane Trap | Leave a trap for 5s. Deals 40% Arcane damage and applies Weakness II for 6s within 1.5 blocks. | Leave a trap that deals 40% Arcane damage and applies Weakness II for 6s. |
| Phase Shift | Become immune to damage for 0.3s. | Become immune to damage for 0.3s. |

### Tier III - Burst Blink / Evocation

| Skill / node | Current description | Proposed description |
|---|---|---|
| Burst Blink | Blink forward up to 10 blocks before upgrades. Deal 50% Arcane damage within 5 blocks at departure immediately and at arrival after 0.25s. | Blink forward and explode at both ends, dealing 50% Arcane damage at each location. |
| Improved Blink | Increases Burst Blink distance by 5 blocks, to 15 blocks. | Burst Blink travels 5 blocks farther. |
| Slipstream | Reduces Burst Blink cooldown by 3s. | Reduce Burst Blink's cooldown by 3s. |
| Purge | Burst Blink grants Speed I (+20% movement speed) for 4s. | Burst Blink grants Speed I for 4s. |
| Evocation | Channel up to 10 stacks lasting 10s. Each grants +5% base spell critical chance, +3% spell haste and +10% damage taken. | Gain up to 10 stacks for 10s: 5% spell critical chance, 3% haste and 10% more damage taken per stack. |
| Improved Evocation | Reduce Evocation's base cooldown by 5s, from 35 to 30s at full channel. | Reduce Evocation's cooldown by 5s. |
| Rapid Evocation | Evocation channels 12 times and can reach 12 stacks. | Evocation can build 12 stacks instead of 10. |
| Lasting Evocation | Evocation lasts 15s. | Evocation lasts 15s. |
| Presence of Mind | Ready your next Arcane spell for 5s. Its hits make enemies take 20% more Arcane damage for 6s. | Your next Arcane spell makes enemies take 20% more Arcane damage for 6s. Use within 5s. |
| Arcane Ward | Gain an absorption shield for 8s: 1 heart plus 1 heart per 5 Arcane power. | Gain a shield for 8s. Your Arcane power makes it stronger. |

## Fire

### Tier I - Fire Breath / Flame Slash

| Skill / node | Current description | Proposed description |
|---|---|---|
| Fire Breath | Breathe fire in front of you for 13% Fire damage per hit, with no distance falloff. | Breathe flames in front of you, dealing 13% Fire damage per hit. |
| Improved Fire Breath | +5% Fire Breath critical chance. | +5% Fire Breath critical chance. |
| Explosive Breath | Fire Breath hits have a 10% chance to trigger an explosion, dealing up to 30% Fire damage within 2.5 blocks. Damage decreases with distance. | Fire Breath hits have a 10% chance to explode for up to 30% Fire damage. |
| Flame Throwing | Increase Fire Breath range by 4 blocks, from 8 to 12 blocks. | Fire Breath reaches 4 blocks farther. |
| Flame Slash | Launch a wide wave of flame after a 0.1-second cast, dealing 100% Fire damage to enemies in its path. | Send out a wide wave of flame that deals 100% Fire damage. |
| Improved Flame Slash | Increase Flame Slash range by 5 blocks. | Flame Slash travels 5 blocks farther. |
| Towering Slash | Flame Slash is 45% larger. | Flame Slash is 45% larger. |
| Wave after Wave | Flame Slash hits reduce its own remaining cooldown by 1s. | Flame Slash hits reduce its remaining cooldown by 1s. |
| Scorching Flames | Enemies take +4% Fire damage per stack, up to 20%. Lasts 2s; further applications refresh it. | Enemies take 4% more Fire damage per stack, up to 20%, for 2s. |
| Hot Impact | Stun the target for 2s. | Stun the target for 2s. |

### Tier II - Meteor / Firestorm

| Skill / node | Current description | Proposed description |
|---|---|---|
| Meteor | Drop 3 meteors on the target. Each deals 150% Fire damage within 6 blocks, with no distance falloff. | Drop 3 meteors on your target. Each deals 150% Fire damage in a 6-block blast. |
| Improved Meteor | Reduce Meteor's base cooldown by 2s, from 20 to 18s. | Reduce Meteor's cooldown by 2s. |
| Meteor Shower | Meteor drops 1 additional meteor, for 4 total. | Drop one extra meteor. |
| Meteor Splash | Meteor impacts create a burning area after 0.25s. It lasts 6s within 5 blocks, reaches 2 blocks high and deals 10% Fire damage per hit. | Meteor impacts leave burning ground for 6s, dealing 10% Fire damage per hit. |
| Firestorm | Channel flames around you, dealing 40% Fire damage per hit within 15 blocks. | Channel flames within 15 blocks, dealing 40% Fire damage per hit. |
| Improved Firestorm | Firestorm gains 10% more spell power. | Firestorm gains 10% more power. |
| Flame Whirlpool | Firestorm pulls enemies inward across its 15-block radius and applies Slowness II for 1s, refreshed on hit. | Firestorm pulls enemies inward and slows them. |
| Raging Firestorm | Firestorm hits 2 additional times and gains 100% extra base knockback, pushing enemies away. | Firestorm hits 2 extra times and knocks enemies away. |
| Flame Trap | Leave a trap for 5s. Deals 30% Fire damage and burns enemies for 4s within 1.5 blocks. | Leave a trap that deals 30% Fire damage and burns enemies for 4s. |
| Blazing Speed | +50% base movement speed for 2s. | Move 50% faster for 2s. |

### Tier III - Wall of Flames / Fire Hydra

| Skill / node | Current description | Proposed description |
|---|---|---|
| Wall of Flames | Place up to 4 walls within 5s. Each lasts 8s, deals 20% Fire damage per hit and reaches 2 blocks high. The fourth wall or placement timeout starts the cooldown. | Place up to 4 flame walls within 5s. Each lasts 8s and deals 20% Fire damage per hit. |
| Improved Wall of Flames | Wall of Flames lasts 2s longer, for 10s total. | Flame walls last 2s longer. |
| Great Wall | Adds 2 wall segments. Segments have 1.2-block radius and 3-block height, and block living entities while active. Projectiles pass through. | Flame walls grow wider and taller and gain 2 segments. They block players and mobs, but not projectiles. |
| Healing Flames | Wall of Flames heals you and party allies for 0.5% of your Fire power each hit. | Flame walls heal you and party allies for 0.5% of your Fire power per hit. |
| Fire Hydra | Summon hydras for 20s (+5s with Improved Hydra).. | Summon Fire Hydras for 20s. |
| Improved Fire Hydra | Fire Hydra lasts 5s longer. | Fire Hydras last 5s longer. |
| Hydra Brood | Conjures an additional Fire Hydra head. | Summon one extra Fire Hydra head. |
| Ancient Hydra | Summon one giant hydra. | Summon one giant hydra. |
| Eruption | Explode for up to 30% Fire damage within 5 blocks. Damage decreases with distance. | Explode around you for up to 30% Fire damage. |
| Flame Ward | Gain an absorption shield for 8s: 1 heart plus 1 heart per 10 Fire power. Attackers take 30% Fire damage. | Gain a shield for 8s that grows with your Fire power. Attackers take 30% Fire damage. |

## Frost

### Tier I - Frost Nova / Frost Spikes

| Skill / node | Current description | Proposed description |
|---|---|---|
| Frost Nova | Deal 180% Frost damage to enemies within 10 blocks and freeze them for 2s before upgrades. Frozen enemies cannot move and are vulnerable to Frost magic. | Deal 180% Frost damage within 10 blocks and freeze enemies for 2s. |
| Improved Frost Nova | Frost Nova freezes enemies for 0.5s longer, for 2.5s total. | Frost Nova freezes enemies for 0.5s longer. |
| Frost Splinters | Frost Nova hits cause secondary explosions, dealing 20% Frost damage within 3 blocks. | Frost Nova causes extra blasts that deal 20% Frost damage. |
| Deep Freeze | Frost Nova applies 1 additional Frozen stack to each enemy. | Frost Nova applies one extra Frozen stack. |
| Frost Spikes | Raise 8 frost spikes along an 8-block row before upgrades. Each hit deals 80% Frost damage and freezes enemies for 2s. Frozen enemies cannot move and are vulnerable to Frost magic. | Raise a row of 8 ice spikes. Each hit deals 80% Frost damage and freezes enemies for 2s. |
| Improved Frost Spikes | Frost Spikes gains 10% more spell power. | Frost Spikes gains 10% more power. |
| Glacial Ridge | Frost Spikes raises 12 additional spikes, extending the row to 20 blocks. | Frost Spikes raises 12 extra spikes, reaching 20 blocks. |
| Frost Fan | Frost Spikes raises two additional rows at +/-10 degrees, dealing 25% damage per hit with Improved Frost Spikes. | Frost Spikes adds two weaker side rows, each dealing 25% Frost damage per hit. |
| Winter's Chill | Enemies take +8% Frost critical damage per stack, up to 40%. Lasts 2s; further applications refresh it. | Enemies take 8% more Frost critical damage per stack, up to 40%, for 2s. |
| Frostbite | Freeze the target for 3s. | Freeze the target for 3s. |

### Tier II - Frost Shield / Ice Lance

| Skill / node | Current description | Proposed description |
|---|---|---|
| Frost Shield | Block all incoming damage for 3s, with -100% base movement speed. When the shield expires, freeze enemies within 5 blocks for 2s. | Become immune to damage for 3s but move much slower. Freeze nearby enemies for 2s when the shield expires. |
| Improved Frost Shield | Reduce Frost Shield's base cooldown by 3s, from 12 to 9s. | Reduce Frost Shield's cooldown by 3s. |
| Nimble Shield | Gain 50% base movement speed for 3s while using Frost Shield, offsetting half of its base movement penalty. | Move faster while using Frost Shield, reducing its movement penalty. |
| Durable Shield | Frost Shield lasts 1s longer, for 4s total. | Frost Shield lasts 1s longer. |
| Ice Lance | Launch an ice lance dealing 200% Frost damage. Freezes hit enemies for 4s after a 0.5s delay. | Launch an ice lance for 200% Frost damage. After a short delay, freeze enemies hit for 4s. |
| Improved Ice Lance | Reduce Ice Lance's base cooldown by 2s, from 12 to 10s. | Reduce Ice Lance's cooldown by 2s. |
| Colossal Lance | Ice Lance is 50% larger and gains +15% critical chance. | Ice Lance is 50% larger and gains 15% critical chance. |
| Shattering Lance | Ice Lance stops at its first enemy. The direct hit retains 200% damage and 4s delayed Frozen; its 8-block splash deals 50% damage and 1s delayed Frozen without damage falloff. | Ice Lance keeps its direct hit but explodes on the first enemy, dealing 50% splash damage and freezing nearby enemies for 1s. |
| Frost Trap | Leave a trap for 5s. Freeze enemies within 1.5 blocks for 4s. | Leave a trap that freezes nearby enemies for 4s. |
| Arctic Reflex | Your next Frost spell deals 20% more damage. Use within 5s. | Your next Frost spell deals 20% more damage. Use within 5s. |

### Tier III - Blizzard / Frost Elemental

| Skill / node | Current description | Proposed description |
|---|---|---|
| Blizzard | Rain frost shards on the target and nearby enemies. Each deals about 26.7% Frost damage within 3 blocks and slows enemies. No distance falloff. | Rain ice shards that damage and slow enemies around your target. |
| Improved Blizzard | Blizzard hits 3 additional times, for 15 channel hits total. | Blizzard hits 3 extra times. |
| Hail Storm | Blizzard gains 20% more spell power. | Blizzard gains 20% more power. |
| Snow Storm | Blizzard slows enemies by 20% base movement speed per stack, up to 60% at 3 stacks. Lasts 3s, refreshed by hits. | Blizzard hits slow enemies by 20% per stack, up to 60%, for 3s. |
| Frost Elemental | Summon a Frost Elemental for 20s. Its health, armor and melee damage grow with your Frost power. It has 20% of your Frost power; its Frost Shard deals 100% and Super Nova deals 180% of its own power.. | Summon a Frost Elemental for 20s. Your Frost power strengthens its body and attacks. |
| Improved Frost Elemental | Frost Elemental lasts 5s longer. | Frost Elemental lasts 5s longer. |
| Elemental Legion | Summon three normal-sized Frost Elementals, each with 80% of the base attributes. Each Super Nova reaches 6 blocks and freezes for 0.5s. | Summon 3 Elementals, each with 80% of normal stats. Their novas freeze nearby enemies for 0.5s. |
| Elemental Colossus | Summon one Frost Colossus at 1.7x size with double base attributes, inheriting your knockback resistance. | Summon a larger Elemental with double stats and your knockback resistance. |
| Cold Snap | Reset Frost spell cooldowns, except Blizzard and Frost Elemental. | Reset Frost spell cooldowns, except Blizzard and Frost Elemental. |
| Frost Ward | Gain an absorption shield for 8s: 1 heart plus 1 heart per 10 Frost power. Slows attackers. | Gain a shield for 8s that grows with your Frost power. Slows attackers. |

## Archer

### Tier I - Power Shot / Entangling Roots

| Skill / node | Current description | Proposed description |
|---|---|---|
| Power Shot | Prepares your next 3 arrows for 8s. Hits apply Hunter's Mark for 6s, refreshed, increasing damage taken by 7% per stack, up to 21%. | Empower your next 3 arrows. Each hit marks enemies to take 7% more damage, stacking up to 21% for 6s. |
| Improved Power Shot | Power Shot prepares arrows for 10s instead of 8s. Hunter's Mark lasts 6s, refreshed by hits. | Power Shot stays ready 2s longer. |
| Improved Hunter's Mark | Prepares 4 arrows instead of 3. Hunter's Mark stacks up to 4 times, increasing damage taken by 7% per stack, up to 28%. | Power Shot prepares 4 arrows and lets Hunter's Mark reach 4 stacks. |
| Charged Shot | Marked arrow hits deal 50% of your Ranged Damage as splash to other enemies within 5 blocks, without distance falloff. | Marked arrow hits splash other nearby enemies for 50% ranged damage. |
| Entangling Roots | Creates roots in a 6-block radius for 6s, reducing enemy movement speed and jump strength by 60%. Party allies and owned pets are protected. | Create a 6-block patch of roots for 6s, slowing enemy movement and jumps by 60%. |
| Improved Entangling Roots | Entangling Roots cooldown is reduced by 2s, to 10s. | Reduce Entangling Roots' cooldown by 2s. |
| Nettle Sprouts | Every root pulse adds stacking Poison for 5s. The maximum Poison level scales with 20% of your melee power. | Root pulses apply stacking Poison for 5s. |
| Nature's Grasp | Every root pulse immobilizes enemies for 1s. | Root pulses hold enemies for 1s. |
| Concussive Shot | Stun the target for 1s. | Stun the target for 1s. |
| Rhythm | +3% ranged attack speed per stack, up to 15%. Lasts 4s; hits refresh it. | Hits build 3% ranged attack speed per stack, up to 15%, for 4s. |

### Tier II - Barrage / Spirit Wolf Pack

| Skill / node | Current description | Proposed description |
|---|---|---|
| Barrage | Fire 3 accurate arrows, spaced 0.15s apart. Each deals 60% of normal shot damage. | Fire 3 arrows that each deal 60% normal shot damage. |
| Improved Barrage | Barrage cooldown is reduced by 1s, to 7s. | Reduce Barrage's cooldown by 1s. |
| Extensive Barrage | Barrage fires 1 additional arrow, for 4 total. Each deals 60% normal shot damage. | Barrage fires one extra arrow. |
| Blood Barrage | Barrage arrow hits heal you for 6% of your Ranged Damage. | Barrage hits heal you for 6% of your ranged power. |
| Spirit Wolf Pack | Summons 2 Spirit Wolves for 20s. Each has 20 HP plus 40% of your Ranged Damage, and deals 3 damage plus 15% of your Ranged Damage per melee hit. Pet bonuses apply. | Summon 2 Spirit Wolves for 20s. Your ranged power and pet bonuses make them stronger. |
| Improved Spirit Wolf | Spirit Wolf health is 20 HP plus 50% of your Ranged Damage, instead of 40%. | Spirit Wolves gain extra health equal to 10% of your ranged power. |
| Leader of the Pack | Summons 1 additional Spirit Wolf, for 3 total. | Summon one extra Spirit Wolf. |
| Enduring Bond | Spirit Wolves remain active 10s longer, for 30s total. | Spirit Wolves last 10s longer. |
| Momentum | Gain 2 Rhythm stacks for 4s. | Gain 2 Rhythm stacks for 4s. |
| Tactical Maneuver | +200% base roll recharge rate for 4s. | Combat roll recharges faster for 4s. |

### Tier III - Rain of Arrows / Magic Arrows

| Skill / node | Current description | Proposed description |
|---|---|---|
| Rain of Arrows | Rains 40 arrows at 60% of your Ranged Damage per impact. Arrows fall every 0.25s throughout a random 6-block radius that follows the target, with 2.5-block splash per impact. | Rain 40 arrows around your target. Each deals 60% ranged damage with a small splash. |
| Improved Rain of Arrows | +5% Rain of Arrows critical chance. | +5% Rain of Arrows critical chance. |
| Torrential Arrows | Rains 40 arrows every 0.1s across a random 6-block radius. Each impact splashes within 2.5 blocks. | Rain of Arrows drops an arrow every 0.1s. |
| Endless Volley | Add 20 falling arrows, for 60 total, spaced 0.35s apart across a random 6-block radius. Each deals 60% ranged damage with a 2.5-block splash. | Rain of Arrows fires 20 extra arrows, spaced 0.35s apart. |
| Magic Arrows | Fire 4 seeking arrows, spaced 0.25s apart. Each deals up to 80% ranged damage at full charge. Partial charges deal less damage. Arrows seek the nearest enemy within 32 blocks, pass through walls and last 10s. Upgrades improve damage or tracking. | Fire 4 homing arrows that pass through walls. Each deals up to 80% ranged damage and lasts 10s. |
| Improved Magic Arrows | Magic Arrows deal 90% of your Ranged Damage per arrow at full charge instead of 80%. | Magic Arrows deal 90% ranged damage at full charge. |
| Conjured Arrow | Fires Magic Arrows upward in a wide spread above you. After 0.5s, they seek the nearest enemy within 32 blocks with medium homing. Hits reveal enemies for 10s. | Magic Arrows launch above you, turn toward enemies and reveal them for 10s when hit. |
| Magic Punch | Magic Arrows deal 100% of your Ranged Damage per arrow at full charge with Improved Magic Arrows, and gain 150% extra base knockback. | Magic Arrows deal 100% ranged damage at full charge and knock enemies back farther. |
| Supercharge | Empower your next normal arrow to deal 125% damage. Lasts 5s; ranged haste is reduced by 25% until fired. | Your next normal arrow deals 25% more damage, but takes longer to draw. Use within 5s. |
| Deflection | Block the next 3 eligible physical attacks within 10s. | Block the next 3 physical attacks within 10s. |

## Rogue

### Tier I - Shock Powder / Slice & Dice

| Skill / node | Current description | Proposed description |
|---|---|---|
| Shock Powder | Stuns enemies within 5 blocks for 3s, or 1s for high-health mobs and players at 24 HP or more. Improved adds 1s. Mob threshold: 50 HP plus twice your melee power; players below 24 current HP receive the longer stun. | Stun enemies within 4 blocks for 3s. Tougher targets are stunned for 1s. |
| Improved Shock Powder | Shock Powder stuns last 1s longer: 2 or 4s. | Shock Powder stuns last 1s longer. |
| Explosive Powder | Shock Powder causes a guaranteed explosion dealing 50% melee damage within 4 blocks, without distance falloff. | Shock Powder explodes for 50% melee damage around each enemy hit. |
| Smoke Screen | Shock Powder leaves a smoke screen for 8s, granting allies inside 50% evasion. | Shock Powder leaves smoke for 8s, granting you and allies inside 50% evasion. |
| Slice & Dice | For 10s, your next 10 successful melee hits deal 15% extra melee damage and gently pull enemies toward you. Each hit grants 5% evasion for 4s, stacking up to 50%. | Your next 10 melee hits add 15% melee damage, pull enemies closer and build evasion for 4s, up to 50%. |
| Improved Slice and Dice | Slice and Dice cooldown is reduced by 2 seconds. | Reduce Slice & Dice's cooldown by 2s. |
| Blades of Fury | Slice and Dice empowers 4 additional successful hits, for 14 total. | Slice & Dice empowers 4 extra hits. |
| Fleet Footed | Empowered Slice & Dice hits grant 3% base movement speed per stack, up to 12 stacks, for 4s. Further hits refresh the duration. | Slice & Dice hits build 3% movement speed per stack, up to 36%, for 4s. |
| Coated Blades | Apply stacking Poison for 8s. | Apply stacking Poison for 8s. |
| Fracture | Reduce armor by 2 plus 10% melee power per stack, up to 3 stacks. Lasts 6s; further applications refresh it. | Shred armor for 6s, stacking 3 times. Your melee power makes it stronger. |

### Tier II - Shadowstep / Bear Trap

| Skill / node | Current description | Proposed description |
|---|---|---|
| Shadowstep | Teleport behind an enemy within 14 blocks. Disrupt nearby hostile mobs within 4 blocks for 1.5 seconds. Killing an enemy within 4 seconds of a successful Shadowstep resets its cooldown. | Teleport behind an enemy and briefly disrupt nearby mobs. Kill an enemy within 4s to reset Shadowstep's cooldown. |
| Improved Shadow Step | Shadowstep reaches 18 blocks instead of 14. | Shadowstep reaches 4 blocks farther. |
| Cloak of Shadows | Shadowstep grants immunity to incoming damage for 0.5 seconds. | Shadowstep makes you immune to damage for 0.5s. |
| Ambush | Shadowstep immediately deals 40% melee damage and applies Blindness I, Weakness I and Slowness I for 2 seconds. | Shadowstep deals 40% melee damage and blinds, weakens and slows the enemy for 2s. |
| Bear Trap | Place one trap 3 blocks ahead. After arming for 1s it becomes hidden. An enemy within 1 block triggers 40% dual-melee damage and 1s of root to enemies within 1.5 blocks, plus Slowness I for 3s. Jumping enemies can trigger it. | Place a hidden trap ahead. It deals 40% dual-wield damage, roots enemies for 1s and slows them for 3s. |
| Improved Bear Trap | Bear Trap deals 60% dual-melee damage instead of 40%. | Bear Trap deals 60% dual-wield damage. |
| Serrated Traps | Apply Bleed for 2.5 seconds. Every 1.25 seconds it deals 2 HP while stationary or 6 HP while moving. Bleed does not scale or stack levels. | Bear Trap causes Bleed for 2.5s. Bleed hurts more when the enemy moves. |
| Extensive Coverage | Bear Trap affects a 2.25-block radius and activates when enemies enter within 1.5 blocks. | Bear Trap triggers from farther away and catches enemies in a larger area. |
| Sidestep | +20% evasion for 2s. Taking damage does not end the buff. | Gain 20% evasion for 2s. Taking damage does not remove it. |
| Opportunist | After rolling, your next melee hit is guaranteed to critically strike. | After rolling, your next melee hit is guaranteed to critically strike. |

### Tier III - Vanish / Mutilate

| Skill / node | Current description | Proposed description |
|---|---|---|
| Vanish | Enter Stealth for 7s with 50% less base movement speed. Attacking, casting, using items or being hit ends Stealth. | Become hidden for 7s but move 50% slower. Attacking, casting, using items or taking a hit ends Stealth. |
| Improved Vanish | Vanish reduces base movement speed by 40% instead of 50%. Stealth still lasts 7s. | Vanish slows you by 40% instead of 50%. |
| Stealth Speed | Reduce the remaining Vanish movement penalty by another 20%, leaving 20% less base movement speed with Improved Vanish. | Move faster during Vanish, reducing its remaining movement penalty. |
| Shadow Stealth | Attacking, casting spells, using items and taking hits no longer end Vanish. Stealth still expires after 7s. | Actions and incoming hits no longer end Vanish. Stealth still expires normally. |
| Mutilate | Strike enemies in front of you for 100% combined dual-wield damage. | Strike enemies in front of you for 100% dual-wield damage. |
| Improved Mutilate | Reduce Mutilate's cooldown by 2s. | Reduce Mutilate's cooldown by 2s. |
| Crimson Strikes | Mutilate heals you for 10% of your melee damage per enemy struck. | Mutilate heals you for 10% of your dual-wield power per enemy hit. |
| Bloody Strikes | Apply Bleed for 7.5 seconds. Every 1.25 seconds it deals 2 HP while stationary or 6 HP while moving. Bleed does not scale or stack levels. | Mutilate causes Bleed for 7.5s. Bleed hurts more when the enemy moves. |
| Cheat Death | Survive a fatal hit with 3s damage immunity, Speed III and Jump Boost III. Gain +30% evasion for 6s. | Survive a fatal enemy hit with 3s immunity, Speed III and Jump Boost III. Gain 30% evasion for 6s. |
| Prepared Counter | After evading an enemy attack, your next basic melee hit adds 25% of melee power. One charge; melee skills cannot consume it. | After evading an enemy attack, your next basic melee hit adds 25% of your melee power. |

## Warrior

### Tier I - Throw Weapon / Throw Net

| Skill / node | Current description | Proposed description |
|---|---|---|
| Throw Weapon | Throw your weapon for 40%-80% melee damage, increasing with charge. Slows enemies for 1s and leaves you weakened for 2s. | Throw your weapon for 40-80% melee damage, depending on charge. Briefly slow the target and weaken yourself. |
| Improved Throw Weapon | Throw Weapon deals 50%-90% melee damage, increasing with charge. | Throw Weapon deals 50-90% melee damage, depending on charge. |
| Strong Throw | Throw Weapon pierces the first enemy and seeks one more with slight homing, hitting up to 2 enemies. | Throw Weapon pierces one enemy and seeks a second target. |
| Head Throw | Throw Weapon deals 60%-100% melee damage, increasing with charge, and stuns for 0.25s. | Throw Weapon deals 60-100% melee damage and briefly stuns the target. |
| Throw Net | Throw a net for 7.5%-15% melee damage, increasing with charge. Roots eligible enemies for 2s and pulls them toward you. | Throw a net that deals 7.5-15% melee damage, roots weaker enemies for 2s and pulls them closer. |
| Improved Throw Net | Throw Net gains 6 blocks of charged range, reaching 30 blocks at full charge. | Throw Net travels 6 blocks farther when charged. |
| Bigger Net | Throw Net also hits enemies within 5 blocks of impact. Each enemy's strength determines whether it can be rooted and pulled. | Throw Net catches enemies around its impact. |
| Multiple Nets | Throw Net has a 4-second base cooldown instead of 6s. Its root lasts 1.5s instead of 2. | Throw Net's cooldown is 2s shorter, but its root is 0.5s shorter. |
| Killing Spree | +2% melee damage and damage reduction per stack, up to 5 stacks. Lasts 8s; hits refresh it. | Hits build 2% melee damage and damage reduction per stack, up to 10%, for 8s. |
| Endured Vitality | Gain 1 armor plus 6% melee power and 1 maximum heart per stack, up to 10 stacks. Lasts 8s; hits refresh it. New hearts are healed; refreshing at maximum stacks does not heal. | Hits grant armor and 1 extra heart per stack for 8s, up to 10 stacks. Your melee power strengthens the armor; only new hearts heal. |

### Tier II - Wild Charge / Demoralizing Shout

| Skill / node | Current description | Proposed description |
|---|---|---|
| Wild Charge | +50% base movement speed and +25% knockback resistance for 2s. Break free of movement impairments and prevent new ones for 2s. Usable while controlled. | Move 50% faster and resist knockback for 2s. Break free of movement control and resist it for 2s. |
| Improved Wild Charge | Wild Charge lasts 1s longer: 3s total. Its control protection remains 2s. | Wild Charge's speed and knockback resistance last 1s longer. |
| Endurance | Wild Charge grants full knockback immunity for its entire duration. | Wild Charge makes you immune to knockback. |
| Concussion Blow | Your next melee hit after Wild Charge stuns for 2s. Use within 4s. | Your next melee hit after Wild Charge stuns for 2s. Use within 4s. |
| Demoralizing Shout | Weaken enemy melee, ranged and magic power by 8% and reveal them for 6s. Break free of movement impairments and prevent new ones for 3s. Usable while controlled. | Weaken enemy attack and magic power by 8% and reveal them for 6s. Break free of movement control and resist it for 3s. |
| Improved Shout | Demoralizing Shout and Battle Shout reach 4 blocks farther: 16 blocks total. | Shout reaches 4 blocks farther. |
| Battle Shout | Casting Demoralizing Shout grants you and party allies 8% base melee, ranged and offensive magic power for 6s. Uses the same 12-block radius, or 16 with Improved Shout. | Demoralizing Shout grants you and party allies 8% more melee, ranged and magic power for 6s. |
| Challenging Shout | Demoralizing Shout also deals 10% melee damage, makes hostile mobs target you and stuns enemies for 0.2s, including bosses. | Demoralizing Shout deals 10% melee damage, draws mobs to you and stuns enemies for 0.2s. |
| Warm Up | Restore 1 HP plus 6% melee power. | Restore 1 HP plus 6% melee power. |
| Momentum | Ready 5 melee hits for 6s. Each grants +2% attack and movement speed, up to 10% each, for 3s. Hits refresh the buffs. | Your next 5 melee hits build attack and movement speed, up to 10% each for 3s. |

### Tier III - Berserker / Last Stand

| Skill / node | Current description | Proposed description |
|---|---|---|
| Berserker | Each of 5 pulses grants +15% attack speed, +10% basic-attack damage, +15% knockback resistance and +20% damage taken for 12s. The final pulse fully heals and grants Speed IV for 2s. Expiry fully heals. A fatal hit ends the buff and fully heals, but puts early skills on cooldown and causes Nausea for 10s and stun for 5s. | Build attack speed, basic damage and knockback resistance for 12s, but take 20% more damage per stack. Full channel and expiry fully heal you. Full channel grants Speed IV. A fatal hit heals you but ends Berserker, locks early skills and causes stun and Nausea. |
| Improved Berserker | Berserker lasts 15s instead of 12s. | Berserker lasts 3s longer. |
| Brutality | +4% critical chance per Berserker pulse, up to 20%. Lasts until Berserker ends. | Berserker grants 4% critical chance per pulse, up to 20%. |
| Lethality | Melee hits restore 1 heart during Berserker. Lose 1 HP every 0.5s, ignoring defenses. | Melee hits heal 1 heart during Berserker, but you lose 1 HP every 0.5s. |
| Last Stand | Each pulse adds 20% maximum health and heals the added health, up to 100%. Grants brief damage immunity, Regeneration I and knockback immunity. | Channel up to 5 pulses, each adding 20% maximum health and healing the added health. Gain brief immunity, regeneration and knockback immunity. |
| Improved Last Stand | Last Stand buffs last 20s instead of 15s. Pulse damage immunity stays at 0.6s. | Last Stand lasts 5s longer. |
| Juggernaut | Each Last Stand pulse adds 10% size and 3% damage reduction, up to 50% size and 15% reduction. Lasts 20s with Improved Last Stand; pulses refresh it. | Last Stand builds size and damage reduction with each pulse, up to 50% size and 15% reduction. |
| Sentinel | +1% melee damage per direct hit taken during Last Stand, up to 10%. Refreshes the bonus and Regeneration I for 5s. | Hits taken during Last Stand build melee damage up to 10% and refresh regeneration for 5s. |
| Enrage | Gain +4% melee damage and +8% size at each health threshold, up to +24% damage and +48% size. Bonuses adjust as your health changes. | Gain more melee damage and size as your health falls, up to 24% damage and 48% size. |
| Tenacity | Gain 6 HP plus 30% melee power as absorption for 12s. Stun enemies within 5 blocks for 2s. | Gain a shield for 12s that grows with your melee power. Stun nearby enemies for 2s. |

## Wand and staff attacks

These are the covered Wizard schools' weapon attacks, separate from the three class tiers.

| Skill / node | Current description | Proposed description |
|---|---|---|
| Arcane Bolt | Shoots a bolt of energy, causing 70% Arcane damage. | Fire an energy bolt that deals 70% Arcane damage. |
| Arcane Blast | Blast the target for 80% Arcane damage and gain an Arcane Charge, up to 3 stacks before upgrades. | Deal 80% Arcane damage and gain an Arcane Charge, up to 3 charges. |
| Fireball | Launch a fireball, dealing 80% Fire damage and setting the target on fire. | Launch a fireball for 80% Fire damage and burn the target. |
| Scorch | Scorches the target, causing 60% Fire damage and setting it on fire. | Deal 60% Fire damage and set the target on fire. |
| Pyroblast | Launch an explosive fireball, dealing up to 100% Fire damage within 2.5 blocks. Splash damage decreases with distance. | Fire an explosive fireball for up to 100% Fire damage. Splash damage weakens with distance. |
| Frostbolt | Launch a frost projectile that ricochets to 2 additional nearby targets before upgrades, dealing 80% Frost damage and slowing enemies on impact. | Fire an ice bolt that slows enemies and jumps to 2 more targets, dealing 80% Frost damage per hit. |
| Frost Shard | Launch a frost shard that can bounce off walls, dealing 60% Frost damage on impact. | Fire an ice shard that bounces off walls and deals 60% Frost damage. |

## Internal helper descriptions

These support parent skills or summons. They are not new selectable nodes; visibility and tree grants stay unchanged.

| Skill / node | Current description | Proposed description |
|---|---|---|
| Frost Shield Expiry Burst | Freezes enemies within 5 blocks for 2s when Frost Shield naturally expires. Deals no damage. | Freeze nearby enemies for 2s when Frost Shield expires. |
| Arcane Emitter Bolt | Emitter projectile dealing 40% of emitter Arcane damage. Ordinary wand Arcane Bolt is unchanged. | Deal 40% of the emitter's Arcane power as damage. |
| Burst Blink Explosion | Deals 50% Arcane damage within 5 blocks at either Blink position. | Deal 50% Arcane damage around a Blink location. |
| Elemental Frost Shard | Deal 100% of the elemental's own Frost power as damage. | Deal 100% of the elemental's Frost power as damage. |
| Elemental Super Nova | Deal 180% of the elemental's own Frost power and freeze enemies for 2s. | Deal 180% of the elemental's Frost power and freeze enemies for 2s. |
| Legion Super Nova | Deal 180% of this elemental's own Frost power within 6 blocks, freezing enemies for 0.5s. | Deal 180% of the elemental's Frost power and freeze nearby enemies for 0.5s. |
| Ancient Hydra Fire Breath | Continuously breathe fire in a 40-degree cone, dealing 24% of own Fire power per hit. Targets the nearest visible enemy within 12 blocks. | Breathe fire at nearby enemies, dealing 24% of the hydra's Fire power per hit. |
| Hydra Fireball | Deal 55% of the hydra's own Fire power. | Deal 55% of the hydra's Fire power. |
| Explosive Powder | Companion marker; adds no extra stun duration. | No additional effect. |

## Careful-check notes

- **Shock Powder:** the installed language says five blocks, but its current spell resource uses four. The proposal corrects that stale description without changing the stun. Its exact mob maximum-health and player current-health thresholds remain in the Rogue raw reference.
- **Battle Shout:** removed ?Casting,? the repeated radius and the Improved Shout radius comparison. The bonus still affects you and party allies. It does not become a healing-power or flat-Bleed bonus.
- **Challenging Shout:** ?draws mobs to you? explains the taunt directly. The short stun and added melee damage remain; boss eligibility is a shared rule, not repeated on every stun tooltip.
- **Wild Charge:** movement speed is a buff, not an instant teleport or dash. Its control protection stays two seconds even when Improved extends the movement buff.
- **Shattering Lance:** the direct hit remains unchanged. The smaller damage/freeze values belong to the splash. The rewrite must not imply that the main hit was reduced.
- **Frost Shield / Nimble Shield:** avoid promising normal movement or total immobility after all equipment bonuses. Nimble reduces the movement penalty; it does not remove it.
- **Elemental Legion:** the three Elementals are weaker, not physically smaller. Colossus is larger with doubled stats. Do not confuse their own Frost power with the caster's full Frost power.
- **Crimson Strikes:** the healing uses combined dual-wield power, independently of Mutilate's damage multiplier. Its current wording's generic ?melee damage? is clarified.
- **Prepared Counter:** only the next basic melee hit gains its flat melee-power bonus. It does not immediately retaliate or buff spell damage.
- **Bleed:** both Rogue nodes use the same single-level effect: 2 raw HP stationary or 6 moving horizontally, every 1.25s. Their application durations differ. The proposed node text avoids repeating the full shared effect explanation.
- **Berserker / Last Stand:** preserve their tradeoffs, temporary health and fatal-hit consequences. Berserker retains its damage-taken penalty, stun and Nausea; Demoralizing Shout retains three seconds of control protection. Endured Vitality still states that melee power improves its armor. Compact wording is not permission to remove a gameplay penalty or healing condition.
- **No invented global rules:** do not claim every control affects bosses, every shield blocks environmental damage, or every summon inherits every player bonus unless that specific mechanic supports it.
- **Hidden helpers and the Explosive Powder companion:** do not put internal markers into the visible tree or imply that the companion grants extra stun.
- **Summon and ward formulas:** omitted from brief tooltip prose only. Their actual mechanics and detailed reference values remain unchanged.

## Implementation - 24 October 2026

The user approved this wording and all 196 entries now match the installed English descriptions: 176 rewrites and 20 retained entries. The before/after tables remain the audit record. Only `assets/*/lang/en_us.json` changed in Archers, Rogues, Wizards and Skill Tree; all other translations and archive entries are unchanged. Priest and Paladin remain excluded.

Rebuild and verification instructions are in [the implementation record](README.md). Previous active and fixed-pack copies are archived locally. No personal installation, combat data or code changed; no commit or push was requested. Verification covers exact packaged text and archive preservation, not a new in-game visual test.


### 29 October2026 Paladin support continuation

The approved [support balance plan](../../superpowers/plans/2026-10-29-paladin-support-and-vanish.md) is now implemented. Purge healing reads20%/25%/35%; Vanguard describes its periodic melee bonus/stun, and Guardian its2% baseline plus2%/3% shield stacks. Protection uses finite attack blocks and30%/40% reduction ceilings. Keep the full scaling formulas in the linked plan, native gold rows for cooldowns/triggers, and resolved effect details for received benefits. See [implementation and verification](../paladin-support-balance/README.md).
