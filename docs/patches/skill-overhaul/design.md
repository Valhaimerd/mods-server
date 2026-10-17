# Skill tree changes — planning draft

Status: requirements and recommendations only; no installed skill data changed.
Recorded: 2026-10-17. Updated: 2026-10-18.

## Scope

Plan adjustments to Adventurer Skills and the RPG Series skill trees. The requested changes below cover Adventurer Skills and RPG Series attribute rewards. The goal is to buff the magical path while adjusting melee and ranged progression, preserving Paladin defaults. Do not apply the Adventurer restrictions across RPG class/weapon categories without a separate decision.

Reference installed builds: Adventurer Skills 1.3 (local English/26.2 repair), Pufferfish's Skills 0.19.0, Pufferfish's Attributes 0.8.2 and Spell Power 1.6.1.

## Requested changes

### Ordinary-node amount policy ? updated 2026-10-18

Preserve the author's ordinary-node bonus for each stat type, including when that stat is assigned to a different position or to a new side node. Do not preserve an old position's numerical amount when its reward changes to a different stat. The requested ordinary physical-offense exceptions are **Melee/Attack Damage: 3% -> 1%**, **Ranged Damage: 3% -> 1%**, and **Attack Speed: 5% -> 3%** throughout Adventurer Skills.

Verified values in the installed author's Adventurer definitions:

| Ordinary reward | Planned amount |
| --- | --- |
| Maximum health / Heart | **+1 heart (+2 HP)** |
| Healing | **+3%** |
| Armor | **+1 Armor** |
| General Resistance | **+1%** |
| Magic Resistance | **+2%** |
| Ranged Resistance | **+2%** |
| Melee/Attack Damage | **+1%**, requested reduction from 3% |
| Ranged Damage | **+1%**, requested reduction from 3% |
| Attack Speed | **+3%**, requested reduction from 5% |
| Spell Haste | **+2%**, specified for the new ordinary caster nodes |
| Spell Critical Chance | **+2 percentage points**, specified for the new ordinary caster nodes |

Apply the same Heart and Healing amounts to the two new Colossus side nodes. Keep author point costs for existing ordinary nodes. Earlier explicit changes to terminal specialties and RPG school/healing-power rewards remain in force; this policy does not revert Magistrate, Colossus, Aegis or Bulwark, or the RPG 3% power targets. The Paladin-default exception also remains.

The author has no ordinary Spell Haste or Spell Critical Chance definitions in the installed Adventurer tree. The user has now specified **+2% Spell Haste** and **+2 percentage points Spell Critical Chance** for these ordinary caster nodes. These are separate from physical Attack Speed at 3%. Verify critical-chance attribute scale when implementing so the reward and tooltip express the requested 2% consistently.

### Choose a branch at its entrance

- Make the branches within Offensive and Defensive mutually exclusive from their first branch-specific nodes. Utilities is explicitly exempt and remains fully completable.
- Put the red exclusion connections at the start of each split. Choosing one entrance must prevent spending points in either alternative branch, rather than allowing investment down multiple paths until the final specialization.
- Preserve the common category root and the existing branch layout wherever possible.
- Keep the selected branch's internal paths usable. The restriction chooses a specialization; it should not arbitrarily remove choices inside that specialization.
- Treat this as one branch per category, not one category for the entire character. Selecting an Offensive branch does not automatically prohibit Defensive or Utilities investment.
- Replace Versatile/Versatility with Companion as specified below. Companion also requires a choice at its two branch entrances: only Packleader or Guardian Stead can be completed.

The installed Adventurer tree already has bidirectional exclusive connections between terminal specializations. The planned change moves the meaningful choice to the entrance. Implementation must also check for alternate routes through later nodes that could bypass the entrance restriction.

### Replace the Offensive middle branch with magic

- Preserve the middle branch's current shape, positions and general point-investment structure.
- Rework its rewards, titles, descriptions and icons into a dedicated caster path.
- Plan a magic specialization for its central/final specialty node. The screenshot shows Berserker at the middle terminal; its melee damage, armor penetration, attack speed and heart rewards should be reviewed rather than carried into the caster specialty automatically.
- The final specialty name and its three bonuses are now specified below. Ordinary caster-node tuning beyond the stated targets remains part of the plan.

## Recommended Offensive identity

| Branch | Proposed role | Main rewards |
| --- | --- | --- |
| Upper | Melee specialist | Melee damage and appropriate melee combat bonuses |
| Middle | Magic specialist | Arcane, Fire and Frost Spell Power; modest spell critical or casting bonuses |
| Lower | Ranged specialist | Ranged damage and appropriate projectile/bow bonuses |

Keep Adventurer's caster branch broadly useful across Arcane, Fire and Frost. Let the RPG Series class tree provide deeper school specialization. This gives the two systems different purposes and avoids forcing an elemental choice twice.

### Selected middle specialty: Magistrate

Use **Magistrate** as the replacement for Berserker. This is a general magic specialization. Avoid Arcanist as the specialty name because the RPG tree already has an Arcane Mage path and the name could suggest an Arcane-only focus.

Requested final-node rewards:

| Reward | Target |
| --- | --- |
| Spell Power | **+5%** to the branch's supported schools (Arcane, Fire and Frost only; exclude Priest Healing Power) |
| Spell Critical Chance | **+5 percentage points**, using the verified attribute encoding |
| Spell Haste | **+5%** |

These replace the earlier proposed Arcanist terminal and its +2-point critical bonus. Spell critical chance is an explicitly desired part of the magic specialty.

Retained design guidance:

- Ordinary caster power nodes: proposed **+3% Arcane, Fire and Frost Spell Power** each, replacing the relevant Magic Damage reward rather than stacking both rewards on the same node.
- Keep the branch's existing shape and make its icons and descriptions communicate general spellcasting.
- Replace the old terminal's melee armor penetration and attack speed with the specified caster rewards. Spell haste is distinct from attack speed.
- Magistrate and its ordinary Magic Power nodes cover **Arcane, Fire and Frost only**. Do not include Priest Healing Power or any additional school.
- Remove the existing one-time enchanted golden apple reward under the branch-completion item policy below. Remove the old heart bonus; Magistrate grants only its three specified caster stats.

### Offensive middle branch: selected node assignments

Keep the existing layout and connections. Read the main line from the Offensive root toward Magistrate:

**Magic Power -> Spell Haste -> Spell Critical Chance -> Magic Power -> upper/lower final approach -> Magistrate**

| Position in the supplied layout | Current reward | Planned reward |
| --- | --- | --- |
| First middle-branch node after Offensive (clock) | Attack Speed | **Magic Power** |
| Second main-line node (sword) | Melee Damage | **Spell Haste** |
| Third main-line node, where the two short side nodes split off | Armor Penetration | **Spell Critical Chance** |
| Short upper side node from that third node (blaze powder) | Magic Damage | **Magic Power** |
| Short lower side node from that third node (blaze powder) | Magic Damage | **Magic Power** |
| Fourth main-line node, before the final diamond (sword) | Melee Damage | **Magic Power** |
| Upper node of the final diamond (clock) | Attack Speed | **Spell Haste** |
| Lower node of the final diamond (clock) | Attack Speed | **Spell Critical Chance** |
| Final specialty, currently Berserker | Mixed melee specialty | **Magistrate: +5% Power, +5% Spell Critical Chance, +5% Spell Haste** |

This gives the eight ordinary nodes four Magic Power rewards, two Spell Haste rewards and two Spell Critical Chance rewards. Start with dependable power, introduce faster casting, then introduce critical chance. Both short side nodes reinforce power; the final diamond separates haste and critical chance before reaching the combined specialty. Preserve access to both internal routes as allowed by the current tree; the entrance restriction chooses this magic branch against its melee/ranged siblings, not haste against critical chance within it.

Magic Power here means direct Arcane, Fire and Frost Spell Power, following the existing three-school plan. Spell Critical means **critical chance**, not critical damage. Ordinary Magic Power nodes retain the proposed +3% per supported school; ordinary Spell Haste nodes grant **+2%** and ordinary Spell Critical Chance nodes grant **+2 percentage points**. Magistrate's specified 5%/5%/5% final rewards remain unchanged.

Implementation reference for the inspected installed layout: main-line node IDs are `3gpel3ejgsuvozax`, `v3lnj0ubt40kezxm`, `ro3sae58m8jzn4yz`, `0oz1hx0q08v2i8j5`; the short upper/lower nodes are `m1aqebilvun2ycc7` / `qosdkbl0x44scigj`; final upper/lower nodes are `k6s01bg1bgnsqrew` / `gs6jmtskmyhpbcnu`; the terminal is `zdfpatqypd5nnqx8`. Assign new caster definitions to these instances without changing shared melee/ranged definitions globally.

### Adventurer Skills: melee/ranged damage and attack speed

- Change **+3% Melee Damage** rewards to **+1% Melee Damage** throughout Adventurer Skills.
- Change **+3% Ranged Damage** rewards to **+1% Ranged Damage** throughout Adventurer Skills.
- Change **+5% Attack Speed** rewards to **+3% Attack Speed** throughout Adventurer Skills. This is physical attack speed, not spell haste; keep the separate RPG Rogue attack-speed target at +3% and preserve Paladin defaults.
- Apply this across ordinary nodes and any specialty rewards that retain those damage bonuses, not only the Offensive entrance nodes.
- Update reward data and visible titles/descriptions together. This reduction does not apply to magic power.

### Offensive specialties: remove defensive bonuses

- **Martial Prowess:** remove its **+3% General Resistance** reward and the corresponding tooltip/description line.
- **Hunter:** remove its **+4% Ranged Resistance** reward and the corresponding tooltip/description line.
- Preserve their other rewards except where the existing plan explicitly changes them: melee/ranged damage follows the planned 1% target, physical Attack Speed follows the planned 3% target, and all branch-completion item gifts are removed.
- These are reward removals from the Offensive specialties, not global changes to Resistance or Ranged Resistance. Keep the Defensive branch rewards as specified elsewhere in this plan.

### Offensive ranged branch: weapon focus instead of pets

Replace the ranged/Hunter branch's ordinary **Pet Damage**, **Pet Resistance** and **Movement Speed** rewards with bow/crossbow combat rewards, emphasizing Draw Speed. Preserve the current layout and connections. This request concerns the Offensive ranged branch only; do not change pet stats in other categories automatically.

| Position on the ranged route from Offensive toward Hunter | Current reward | Planned reward |
| --- | --- | --- |
| Second entry-line node, bone icon after the first ranged-damage/crossbow node | Pet Damage | **Draw Speed** |
| Downward-bend node, feather icon after the projectile-speed node | Movement Speed | **Draw Speed** |
| Node below that bend, cat icon where the final diamond starts | Pet Resistance | **+25% Projectile Speed**, using the author's ordinary projectile-speed amount |
| Upper approach to Hunter in the final diamond, bone icon | Pet Damage | **+1% Ranged Damage**, applying to bows and crossbows |

This replaces four ordinary nodes with **two Draw Speed nodes, one Projectile Speed node and one Ranged Damage node**. Keep the existing ordinary ranged-damage node at the planned +1%. Keep the existing Projectile Speed node at the author's +25%, giving two +25% Projectile Speed nodes in this branch; verify its reward affects both bows and crossbows when implementing. The two ordinary Draw Speed nodes each grant **+3% Draw Speed**, as recommended and now confirmed. This is distinct from physical Attack Speed and Spell Haste.

Inspected node IDs in table order: `8qdaale4k86t2ntn`, `02did3uvilfob8c2`, `ui0gusk2hniqwghe`, `k5g9ddvqzlzkxv9f`. Replace their reward definitions, labels and icons without globally changing shared pet or movement-speed definitions. Hunter's previously requested removal of Ranged Resistance and all completion gift items remains in force. No additional terminal changes are implied here.

#### Bow and crossbow draw/charge speed

- Use the installed RangedWeapon API's **`ranged_weapon:haste`** attribute as the integration candidate, not physical attack speed, spell haste or the display-only pull-time attribute.
- The installed `ranged_weapon_api-neoforge-4.0.0+26.2.jar` includes bow and crossbow handling in its haste-related LivingEntity mixin. Its crossbow path scales use progress with the HASTE attribute; this supports the intended bow draw/crossbow charge-speed design at the code level.
- [Official RangedWeapon API documentation](https://github.com/FabricExtras/RangedWeaponAPI) identifies `ranged_weapon:haste` as Draw Speed, with a base value of 100. Encode a percentage bonus using the correct modifier operation rather than mistaking an additive point for a percentage.
- Verify actual bow draw time and crossbow charging time in an isolated client/server test, including Quick Charge interaction and representative installed modded weapons. Do not assume every custom weapon honors the API merely because it looks like a bow or crossbow.
- Ensure the selected ranged-damage and projectile-speed attributes cover both weapon types. If the existing Projectile Speed reward targets only bows, add the corresponding crossbow modifier with the same intended percentage rather than silently excluding crossbows.

## Adventurer Skills: Defensive middle branch

### Health and healing focus

- Rework the middle Defensive branch to emphasize maximum health and healing. Preserve its main horizontal path and use the open space above it for the two additions below.
- Set the five ordinary nodes on the main horizontal line, in order from the Defensive root toward Colossus, to **Heart -> Healing -> Heart -> Healing -> Heart**. Colossus is the separate final specialty after these five nodes. Use +1 heart (+2 HP) for each ordinary Heart node and +3% for each ordinary Healing node, preserving the author's stat-specific values.
- Keep the previously requested entrance-level branch exclusivity for Defensive. The new nodes belong to the middle branch and must not provide a route into another Defensive branch.
- This is an Adventurer Skills change; it does not override the instruction to preserve RPG Paladin defaults.

### Final specialty: Colossus

| Reward | Requested target |
| --- | --- |
| Maximum health | **+5 hearts (+10 HP)** |
| Healing | **+6%** |

The supplied screenshot shows Colossus currently granting +4 hearts, +6% Healing and two regeneration potions. Increase its health reward from +4 to **+5 hearts** and retain **+6% Healing**. These are bonuses granted by this node, not the character's total health or healing limit.

Keep the healing bonus distinct from the RPG tree's Healing Power adjustment. Before implementation, verify the current Colossus healing attribute and whether it affects healing received or another part of healing; do not silently convert it into school-based Healing Power. Remove the two regeneration-potion reward under the branch-completion item policy below; retain the specified health and healing stat bonuses.

### Two additional nodes above the late path

- Count backward from Colossus as the final/last node. The second-last node is its immediate predecessor, and the third-last node is the node immediately before that.
- Add **one new node above the second-last node** and **one new node above the third-last node**: two new nodes total.
- Connect each new node to its corresponding existing node as an upward side node. Keep the existing horizontal progression intact; the additions do not replace either existing node.
- Assign the new node above the **second-last node** to **Heart** (maximum health). Its host is the last ordinary Heart node before Colossus.
- Assign the new node above the **third-last node** to **Healing**. Its host is the preceding ordinary Healing node.
- This places the extra health reward nearest Colossus and the extra healing reward one step earlier. Use the author's +1 heart (+2 HP) and +3% Healing amounts respectively. Icons, labels and new-node point costs remain to be finalized.
- Verify the host nodes' actual IDs and positions against the supplied screenshot before editing the tree data. Space the additions so they do not overlap the upper Defensive branch or its connections.

## Adventurer Skills: Defensive top branch — Aegis

### Magic and ranged protection

Rework the upper Defensive branch to focus on **Magic Resistance and Ranged Resistance**. Preserve its existing layout and connections, including its hanging side node. Rename the current **Endurance** terminal to **Aegis**. This gives the three Defensive branches distinct identities: Aegis protects against magic and ranged damage; Colossus emphasizes health and healing; Bulwark provides general durability and armor.

### Selected ordinary-node assignments

Read from the Defensive root along the upper route toward Aegis (right to left, then upward in the supplied screenshot):

| Position | Current reward/icon | Planned reward |
| --- | --- | --- |
| First upper-branch node after the Defensive root | General Resistance / iron ingot | **Magic Resistance** |
| Second node on the entry line | Magic Resistance / shard | **Ranged Resistance** |
| Third node on the entry line, before the upward bend | Health / heart | **Magic Resistance** |
| Node at the upward bend | Ranged Resistance / target block | **Ranged Resistance** |
| Node directly above that bend | Armor / shield | **Magic Resistance** |
| Last ordinary node before Aegis, along the top horizontal line | General Resistance / iron ingot | **Ranged Resistance** |
| Hanging side node below that last ordinary node | Ranged Resistance / target block | **Magic Resistance** |

The main progression alternates **Magic Resistance -> Ranged Resistance -> Magic Resistance -> Ranged Resistance -> Magic Resistance -> Ranged Resistance -> Aegis**. The hanging side node adds Magic Resistance. Across the seven ordinary nodes, this gives four magic-resistance and three ranged-resistance rewards. Every ordinary Magic Resistance node grants +2% and every ordinary Ranged Resistance node grants +2%, matching the author's values; no health, armor or general-resistance rewards remain in this branch under this assignment.

Implementation reference: main-route IDs are `xzau8wf7cmkahump`, `arcimlg0c3igwe0e`, `-7_-4`, `d67mbc3a2pg8781z`, `4zj68e0t9ythniot`, `2wtk419203if3s6n`; the hanging side node is `bxm9laus4f3lfqjk`; the terminal to rename is `6tinz5atn533lzo9`. Assign branch-specific definitions without altering shared rewards elsewhere.

### Final specialty: Aegis

| Reward | Requested target |
| --- | --- |
| Magic Resistance | **+10%** |
| Ranged Resistance | **+10%** |

These are Aegis's terminal stat rewards, replacing the old Endurance reward set. Remove any completion gift items under the global item-removal policy. Keep the planned entrance-level exclusions against Colossus and Bulwark; the side node must not provide a bypass into another Defensive branch. Verify resistance attribute stacking during implementation rather than treating these rewards as potion effects or guaranteed final damage-reduction percentages.

## Adventurer Skills: Defensive bottom branch

### General durability focus

Rework the bottom Defensive branch around **general Resistance, Armor and Ranged Resistance**. Preserve its existing shape, main progression, final split and two side nodes. Replace its health, movement-speed and magic-resistance rewards with the selected durability rewards below; the middle Defensive branch is the dedicated health/healing path.

General Resistance means the resistance attribute, not a Resistance potion/status effect. Ranged Resistance is protection against ranged damage, not increased ranged damage. Verify the installed attributes and their stacking when implementing.

### Selected ordinary-node assignments

Read from the Defensive root along the bottom route toward Bulwark (right to left in the supplied screenshot):

| Position | Current reward/icon | Planned reward |
| --- | --- | --- |
| First bottom-branch node after the Defensive root | Armor / shield | **General Resistance** |
| Second node on the entry line | Resistance / iron ingot | **Armor** |
| Third node on the entry line, before the downward bend | Health / heart | **General Resistance** |
| Node at the downward bend | Movement Speed / feather | **Ranged Resistance** |
| Node directly below that bend, where the final routes split | Health / heart | **Armor** |
| Upper approach to Bulwark | Armor / shield | **General Resistance** |
| Lower horizontal approach to Bulwark | Armor / shield | **Ranged Resistance** |
| Side node above the upper approach | Ranged Resistance / target block | **Armor** |
| Side node below the lower approach | Magic Resistance / shard | **Ranged Resistance** |

This assigns three ordinary nodes to each of the three rewards. The entry introduces general resistance and armor, the bend introduces ranged protection, and the two final approaches reinforce general versus ranged resistance. Keep both internal routes available under the existing layout; entrance exclusivity applies against the other Defensive branches. Use the author's values throughout: +1% General Resistance, +1 Armor and +2% Ranged Resistance per matching ordinary node.

Implementation reference: entry node IDs are `5h6pmokx7ro45qtw`, `sgg10fjuiiqlos8w`, `-7_4`; bend/split IDs are `fxxc8k4yljxkbepz` and `00zagtopdfilmaw7`; upper/lower approach IDs are `8meyqbog5d7qmyzg` / `bvasjpryldz0yhpx`; upper/lower side IDs are `v2pojgmi36k5fuxe` / `vgrpua0glqz5sem5`. Apply instance-specific definitions so shared rewards in other branches remain unchanged.

### Final specialty: Bulwark

| Reward | Requested target |
| --- | --- |
| General Resistance | **+2%** |
| Ranged Resistance | **+4%** |
| Armor | **+2 Armor** |
| Knockback Resistance | **+0.2 additive (+20 percentage points)**, confirmed recommendation replacing the ambiguous +2 request |

Use these four stat bonuses as Bulwark's new final reward set, replacing its old +4 Armor and +5% Resistance set. Remove its two Splash Potions of Weakness under the completion-item removal policy. Keep the name Bulwark unless a later rename is requested. The terminal node ID is `zfneydbdzicoc783`.

## Adventurer Skills: Utilities ? unrestricted progression

This supersedes the earlier proposal to restrict Utilities at its branch entrances.

- **Remove all three Utility specialty nodes entirely:** Fitness, Excavator and Insightful. Remove the nodes, their rewards, their tooltip text and connections that reference them; do not merely remove their item gifts or rename them.
- Preserve all remaining ordinary Utility nodes and their existing reward amounts, except the combat-stat replacements and 20% anvil-cost reductions specified below.
- Remove existing mutual-exclusion/red connections between Utility paths and do not add entrance exclusions. Players may allocate every remaining Utility node across all three paths on one character.
- Keep ordinary path connections, prerequisites and skill-point costs. ?Unrestricted? means no mutually exclusive branch choices, not free nodes or skipping the progression path.
- Preserve the existing non-specialty layout and avoid leaving prerequisites that reference a deleted specialty. Reconnect surviving ordinary nodes only if needed to maintain reachability.
- Do not automatically redistribute the removed specialty bonuses, create replacement capstones or add their rewards to ordinary nodes. No such replacement has been requested.
- Keep Offensive and Defensive entrance exclusivity as planned. Companion has its own overhaul below, with mutually exclusive left/right entrances.

Inspected specialty node IDs: Fitness `i0j6pbbluuqne53e`, Excavator `s02hkhd2ha87ji1n`, Insightful `qxcrnvfslhwdg5ip`. Include characters with these nodes already allocated in the migration/refund plan; do not leave spent points stranded when deleting them.

### Utilities: replace combat stats with practical rewards

Utilities must not grant or display **Magic Damage, General Resistance or Magic Resistance**. Replace their reward data, node titles, descriptions and icons with practical utility rewards. This rule is specific to Utilities; retain the separately planned Offensive magic-power and Defensive resistance rewards.

Selected replacements, retaining the existing node positions and connections:

| Utility node position | Current reward | Replacement |
| --- | --- | --- |
| Right entrance path, first blaze-powder side node above/right of the shared entry | +3% Magic Damage | **+25% Experience Orb Gain** |
| Left route, iron-ingot junction below the movement-speed node | +1% General Resistance | **+3% Movement Speed** |
| Right route, shard junction above the lower anvil-cost node | +2% Magic Resistance | **20% lower Anvil Repair Cost** |
| Upper-right short side node, formerly blaze powder below Insightful | +3% Magic Damage | **+0.1 Fortune** |

These choices give the entry an experience benefit, reinforce movement on the left route, reinforce equipment maintenance on the right route, and retain a resource-gathering reward near the upper-right end after Insightful is removed. Stamina and Jump Strength remain valid existing utility rewards; no extra nodes or additional replacements are required merely to include every allowed stat type.

Inspected node IDs in table order: `2pj0fbogctx589t3`, `yts2dfnkpz3xqziu`, `gk57oho18h66ghxe`, `vaios6drja2eapyg`. Use instance-specific utility definitions so replacing a shared combat-stat definition does not change other categories. Ordinary replacement amounts match the author's corresponding utility stat amounts, except for the explicit anvil-cost change.

### Anvil Repair Cost: 20% reduction per node

- Set **each Anvil Repair Cost node to a 20% cost reduction**, including existing ordinary anvil nodes and the newly assigned anvil node above. The tooltip should read **-20% Anvil Repair Cost**.
- Existing Utility anvil-node IDs are `46o8u8oydffa04o6` and `gh96zb6t67hnmswd`; with the replacement shard node, Utilities has three planned anvil-cost nodes.
- This is a 20% reduction per allocated node, not ?set the final cost to 20%.? Verify combined stacking and rounding in game before describing the total reduction from multiple nodes.
- The installed ordinary definition is labelled -5% but stores `value: -0.95` with `multiply_base`. Treat that as an existing data/tooltip mismatch to investigate; do not copy its value or infer correct behavior from its label. Encode and test an actual 20% reduction for one node, then test all planned nodes together so the cost cannot become unintentionally free or negative.
- This explicit 20% anvil-cost target supersedes the general instruction to preserve author amounts for this stat. Removed specialty nodes do not retain their former anvil discounts as hidden rewards.

## Adventurer Skills: Versatile overhaul ? Companion

### Category identity and starter text

Rename the **Versatile** category/root to **Companion** and redesign its remaining paths around pet capabilities. This supersedes the earlier decision to leave Versatile unchanged.

Starter-node description:

> Strengthen your pets to fight harder and survive longer.

Use a pet-related root icon when implementing. Preserve the root's identity for saved progression where practical, while updating its visible title and description. The existing starter node ID is `6xmqgm3uw97hrynh` (`root_versatile`).

### Remove the middle path entirely

- Keep the Companion root and the shared first Pet Damage node directly below it, since that node connects the two outer paths.
- Delete the central route below that shared entrance: its Magic Resistance node, Magic Damage node, lower Ranged Resistance node, and both lower forks ending in Ranged Damage and Melee Damage.
- Remove those five nodes, their rewards, descriptions and all connections referring to them. Do not leave the central path as hidden rewards or relocate its old combat bonuses into the outer paths automatically.
- Preserve the left and right outer paths and their connection through the shared entrance as the framework for the Companion redesign. Existing pet nodes on those paths remain available for reassignment during the pet-focused planning.
- Verify that removing the middle path leaves every retained outer-path node reachable and does not strand allocated points. Include the deleted nodes in the existing-character refund/migration plan.

Inspected nodes to remove:

| Node ID | Current reward |
| --- | --- |
| `a036s916b2cp7hb2` | Magic Resistance, central shard below the shared entrance |
| `i2oo5vl75gyj6b3d` | Magic Damage, central blaze powder |
| `usqd6s5h8ypx5gud` | Ranged Resistance, lower central target block |
| `ijsno4eog2w0unyr` | Ranged Damage, lower-left fork |
| `bysc5yiqm9ni1mkf` | Melee Damage, lower-right fork |

The shared first Pet Damage node to retain is `15trz7ezr8z7kcp8`. Removing the middle must not delete this junction or sever the outer branches.

### Companion path identities

- **Left ? offensive wolf companion:** emphasize pet attack damage, movement speed for chasing enemies and attack knockback. Include fewer resistance, knockback-resistance and maximum-health nodes so the pet can stay in the fight without making this the tank path.
- **Right ? defensive companion:** emphasize pet maximum health, pet resistance and pet knockback resistance. Detailed right-path node assignments are still to be planned.
- These bonuses must affect owned pets, not the player's own combat attributes. The left design is especially intended for wolves; support for other pet types requires checking their ownership, movement and attack behavior.
- Attack Knockback here strengthens the pet's own successful attacks. It is not automatic retaliation or a knockback pulse when the pet is hit.

### Companion left: selected node plan

Preserve the pictured connections and replace the existing non-pet rewards with the following pet-specific rewards. Read from the shared entrance down the left route. Ordinary-node amounts are specified below where provided; the two ordinary knockback amounts are now confirmed at +0.1 additive each. Player-stat defaults do not automatically become pet-stat values.

| Position in the supplied left-path layout | Current reward/icon | Planned pet reward |
| --- | --- | --- |
| First left-path node after the shared entrance, top-right cat | Pet Resistance | **Pet Damage** |
| Upper shovel node, left of that entry | Shovel Speed | **Pet Movement Speed** |
| Emerald node below the shovel, top-right of the small square | Fortune | **Pet Attack Knockback** |
| Arrow node at top-left of the small square | Projectile Speed | **Pet Damage** |
| Clock node at bottom-right of the small square | Attack Speed | **Pet Movement Speed** |
| Boot node at bottom-left of the small square | Luck | **Pet Resistance** |
| Cat node directly below the upper shovel on the inner route | Pet Resistance | **Pet Attack Knockback** |
| Slime-ball junction below that cat, where two short side nodes split off | Jump Strength | **Pet Movement Speed** |
| Short lower-left shovel side node from the slime junction | Shovel Speed | **Pet Damage** |
| Short lower-right arrow side node from the slime junction | Projectile Speed | **Pet Knockback Resistance** |
| Potion node on the outer route immediately before the specialty | Experience Orb Gain | **Pet Maximum Health** |

Ordinary-node distribution: **3 Pet Damage, 3 Pet Movement Speed, 2 Pet Attack Knockback, 1 Pet Resistance, 1 Pet Knockback Resistance and 1 Pet Maximum Health**. Eight nodes support offense/chasing and three provide survivability, matching the requested emphasis. Keep the shared entrance's Pet Damage reward outside this branch count.

Inspected node IDs in table order: `qp2x45h0nbr233e1`, `e7fyzt06vz8mu5iv`, `5grntvcsxgm61gbq`, `vyt2xj8si9zbat4x`, `z6awauxhc53s0bhl`, `5frbav4zdc1hwquo`, `xz6a8myofnhkv0e0`, `o3fd9bzo883nqd57`, `629rr01c70gc90kj`, `kbheklivvbq2aaj8`, `87cn8ldpzmjljp0q`.

### Companion left: ordinary-node amounts

| Pet reward | Amount per ordinary node | Status |
| --- | --- | --- |
| Pet Damage | **+1 flat damage**, not +1% | User specified |
| Pet Movement Speed | **+3%** | User specified |
| Pet Maximum Health | **+2 hearts (+4 HP)** | User specified |
| Pet Attack Knockback | **+0.1 additive attack knockback** | User confirmed recommendation |
| Pet Knockback Resistance | **+0.1 additive knockback resistance**, equivalent to **+10 percentage points** on its 0?1 scale | User confirmed recommendation |
| Pet Resistance | Preserve the author's existing ordinary Pet Resistance amount; verify its actual scaling rather than silently converting +1 into 1% | No new ordinary amount specified |

For Attack Knockback, prefer +0.1 as a small flat starting bonus. A relative percentage applied to a zero base gives no benefit, while +1 is ten times this proposed increment and may push enemies away too aggressively. The two planned ordinary attack-knockback nodes would contribute +0.2 before other modifiers if both are allocated.

For Knockback Resistance, distinguish the unit from Attack Knockback: 0 is no resistance and 1 is full resistance. Thus +0.1 adds ten percentage points; +1 would grant full resistance from a zero base. This ordinary +0.1 value applies to Companion pet nodes; Bulwark separately grants the confirmed +0.2 additive Knockback Resistance.

Reference: [NeoForge attribute documentation](https://docs.neoforged.net/docs/entities/attributes/). These are candidate modifiers on the pet's actual attributes; implementation must verify wolf support and observed combat behavior.

### Final left specialty: Packleader

The user selected **Packleader** as the replacement for Enlightenment (`iggdp0a33bljw4kl`). Its final reward set is:

| Pet reward | Target |
| --- | --- |
| Pet Damage | **+20%** |
| Pet Movement Speed | **+15%** |
| Pet Resistance | **+10%** |

These are percentage bonuses, distinct from ordinary +1 flat Pet Damage. Verify damage/resistance attribute operations so the final rewards produce the intended percentage behavior; do not encode a literal +20 flat damage or +10 flat resistance. The final Pet Resistance is damage resistance, not knockback resistance.

This replaces the earlier suggested terminal Attack Knockback bonus: Packleader now grants the three user-specified bonuses above. Do not inherit experience, anvil-cost, magic-resistance or item-gift rewards from Enlightenment.

Implementation must verify ownership-scoped modifiers, remove/reapply bonuses cleanly on respec, avoid accumulating buffs on reload, and cover both existing and newly tamed pets. Test wolf chase behavior and repeated attacks: excessive attack knockback can push targets out of bite range, so choose its amount alongside movement speed rather than assuming more knockback always increases damage output.

### Companion right: Guardian Stead path

Selected final specialty name: **Guardian Stead**. This path makes an owned pet durable enough to hold enemies in place, complementing Packleader's damage and pursuit focus. Preserve the pictured layout and connections. The following node assignments define the planned right path; the final name and numerical rewards have now been specified by the user.

| Position in the supplied right-path layout | Current reward/icon | Planned pet reward |
| --- | --- | --- |
| First right-path node after the shared entrance, top-left bone | Pet Damage | **Pet Maximum Health** |
| Upper axe node, right of that entry | Axe Speed | **Pet Resistance** |
| Emerald node below the axe, top-left of the small square | Fortune | **Pet Maximum Health** |
| Arrow node at top-right of the small square | Projectile Speed | **Pet Knockback Resistance** |
| Clock node at bottom-left of the small square | Attack Speed | **Pet Resistance** |
| Boot node at bottom-right of the small square | Luck | **Pet Maximum Health** |
| Cat node below the entrance on the inner route | Pet Resistance | **Pet Resistance** |
| Crossbow junction below that cat, where two short side nodes split off | Ranged Damage | **Pet Maximum Health** |
| Short anvil side node directly below the crossbow junction | Armor Penetration | **Pet Knockback Resistance** |
| Short lower-right shovel side node from the crossbow junction | Shovel Speed | **Pet Damage** |
| Cat node on the outer route immediately before the specialty | Pet Resistance | **Pet Movement Speed** |

Ordinary-node distribution: **4 Maximum Health, 3 Resistance, 2 Knockback Resistance, 1 Damage and 1 Movement Speed**. Nine nodes support survival and holding position, with two supporting attack/chase capability so the pet can still contribute to fights. The shared entrance's Pet Damage node is outside these counts.

Use the established Companion ordinary amounts where applicable: **+2 hearts (+4 HP)** per Maximum Health node, **+1 flat Pet Damage**, and **+3% Pet Movement Speed**. Preserve the author's ordinary Pet Resistance amount pending its scaling check. For Knockback Resistance, use the confirmed **+0.1 additive (+10 percentage points)** per ordinary node. Two such nodes would contribute +0.2 before other sources; do not describe that as a 20% relative multiplier.

Inspected node IDs in table order: `828iwet0erdpwynl`, `00ojkscfhac0ifpm`, `ha05s7wy4h135376`, `iswxmklicwgn4xmp`, `q0ul30ow7ifhtq6j`, `mk08ei1wfvcssxs6`, `mxm9kmy816lzgkbt`, `rbfta22ujrmwzugp`, `fjtdo0ic4650e43d`, `6vlgfj2olene3csb`, `fr0yn9kxau3qimvv`.

### Final right specialty: Guardian Stead

| Pet reward | Confirmed final bonus |
| --- | --- |
| Pet Maximum Health | **+10 hearts (+20 HP)** |
| Pet Resistance | **+25%** |
| Pet Knockback Resistance | **+0.3 additive (+30 percentage points)** |

These replace the earlier suggested Guardian name and +6-heart/+20%-resistance/+0.1-knockback-resistance final rewards. No completion gift items are awarded.

With all right-path ordinary nodes and Guardian Stead allocated, the planned health bonuses total **+18 hearts (+36 HP)**: four +2-heart nodes plus the +10-heart terminal. The two ordinary knockback-resistance nodes plus the terminal total **+0.5 additive (+50 percentage points)** before other sources and attribute clamping. Pet Resistance stacking must still be verified against the installed attribute implementation; do not report an untested combined damage-reduction percentage.

The current right terminal is `8wjzxjnmc1gr7dvq`, using the author's `pack_leader` definition. Rename/redefine this right node as Guardian Stead while the left terminal becomes the user-selected Packleader; do not globally rename the old definition in a way that swaps the two planned specialties. Remove all inherited non-pet rewards and completion item grants from the right specialty.

### Remaining Companion design decisions

- The left-path reward types and Packleader final rewards are assigned above. Ordinary knockback values are confirmed and ordinary Pet Resistance scaling still needs verification; the right-path node map and Guardian Stead final name/amounts are recorded above. Do not treat retained non-pet bonuses as the final approved design.
- **Only one Companion path may be completed.** Add mutual exclusions at the first left/right branch-specific nodes after the shared Pet Damage entrance, preventing investment into both paths; do not defer the choice until the final specialties. Keep the shared root/entrance available and check for deeper bypass routes. Utilities remains unrestricted.
- Verify which installed pets and damage sources respond to the available pet attributes before promising effects for every companion mod.
- The global removal of branch-completion gift items also applies to Companion. Do not introduce replacement item gifts.

### Clarification: Adventurer magic coverage versus RPG Healing Power

- **Adventurer Skills:** Magic Power rewards and Magistrate affect **Frost, Fire/Pyro and Arcane only**. They grant no Healing Power; Pyro refers to the Fire spell-power school, not an additional fourth school.
- **RPG Skills:** increase applicable **Healing Power rewards from 1% to 3%**, including Priest, alongside the already planned 3% Frost/Fire/Arcane rewards. The earlier note to keep Priest Healing Power unchanged was a misunderstanding and is superseded by this clarification.
- Keep the user's separately specified **Paladin defaults** unchanged. The Adventurer three-school restriction must not be used to exclude Priest from the RPG Healing Power adjustment.

## RPG Series skill-tree changes

| Attribute/path | Requested target | Scope and exceptions |
| --- | --- | --- |
| Frost Spell Power | **+3% per applicable power reward** | RPG skill-tree Frost power nodes |
| Fire Spell Power | **+3% per applicable power reward** | RPG skill-tree Fire power nodes |
| Arcane Spell Power | **+3% per applicable power reward** | RPG skill-tree Arcane power nodes |
| Healing Power | **+3% per applicable power reward**, increased from 1% | RPG Healing Power nodes, including Priest; preserve Paladin defaults |
| Rogue Attack Speed | **+3% per applicable attack-speed reward** | Rogue path; do not apply globally to other classes |
| Paladin | **Keep existing defaults** | Preserve all Paladin rewards, including its default Healing Power; the screenshot shows Paladin Empowerment at +0.2 Healing Power |

These are per-node/reward targets, not a cap of 3% on the character's total power. Increase applicable RPG Healing Power rewards from 1% to 3%, including Priest. Preserve the separately specified Paladin defaults. Verify modifier operations for the three damage-school power rewards before implementation.

The Paladin-default exception takes precedence over the general RPG Healing Power adjustment. Priest Healing Power is included in the 1% to 3% RPG change. The exclusion of healing applies to Adventurer's magic branch, not to RPG Healing Power nodes. Do not change its healing, melee or other rewards through a shared definition used elsewhere; split shared definitions if needed to preserve Paladin behavior.

The RPG screenshot's Archer Empowerment already shows **+1% Ranged Damage**. No separate new RPG melee/ranged damage percentage was specified in this update. The explicit **3% to 1%** damage reduction above applies to Adventurer Skills; record any further RPG melee/ranged adjustments before implementing them.

## Remove branch-completion item rewards

- Remove one-time item grants for completing branches/specialty nodes from the skill-tree changes covered by this plan. Audit Adventurer Skills and RPG Series branch-completion rewards; remove such grants wherever present.
- Examples include Strength or Regeneration potions, 16 bones, five experience bottles, enchanted golden apples and other completion gift items. The list is illustrative, not a restriction to those items or quantities.
- Keep the intended attribute bonuses and passive effects. Removing a potion item reward does not mean removing a separately configured passive/status-effect reward.
- Remove the actual item-grant reward entries or commands and their matching tooltip/description lines. Preserve unrelated rewards on the same node.
- This supersedes earlier suggestions to retain or separately decide on Colossus's regeneration potions or Magistrate's inherited enchanted golden apple.
- The Paladin-default exception remains in force for its stats and gameplay effects; this explicit item-removal policy applies to any branch-completion gift items it has as well.
- Do not confiscate items already awarded to existing characters. Prevent new completion grants, including after respec/reallocation.

## Skill progression rate ? 1.35x original

- Increase progression speed to **1.35x the original rate** for Adventurer Skills and the RPG Series skill categories, including both Class Skills and Weapon Skills.
- This means **35% more skill progression per equivalent activity**, not 35% larger attribute rewards. Keep the planned node bonuses, level caps and skill-point costs separate from this change.
- Preferred implementation: multiply each category's original skill-experience gains by **1.35 once**. If the installed configuration instead requires changing experience thresholds, use original requirements divided by **1.35** (approximately 74.074% of the original), with rounding verified. Do not both multiply gains and lower thresholds.
- Example: an activity granting 100 original skill XP should grant 135 after the change. At a constant activity rate, reaching the same threshold should take about 74.1% of the original time (about 25.9% less time).
- Use the preserved original configuration as the baseline, not a previously adjusted value. Preserve existing earned progression during migration and prevent reloads/repeated installation from multiplying the rate again.
- Apply this to skill-category progression, not global Minecraft XP drops, enchantment costs or unrelated leveling systems. Verify how the installed trees earn skill XP, including any conversion from player experience.

## Magic Damage versus Spell Power

The installed `+3% Magic Damage` node grants `puffish_attributes:magic_damage`. The installed Spell Power damage types are included in the magic damage tag, so caster-attributed Arcane, Fire and Frost hits can already benefit from it. This affects outgoing damage, not the displayed school-power attributes.

Direct integration can instead grant `spell_power:arcane`, `spell_power:fire` and `spell_power:frost`; the installed RPG Series tree already uses these attribute rewards. Each school's percentage should increase that school's existing power. Three school rewards do not mean a single-school spell receives three bonuses.

Avoid accidentally counting the same intended bonus twice. For a spell scaling directly with power, +3% school power combined with a separate +3% outgoing magic damage multiplier can yield approximately +6.09% damage before other effects. Final stacking depends on modifier operations and other equipped/allocated bonuses.

## Remaining implementation decisions and verification

The requested bonus values, three-school coverage, Adventurer-only healing exclusion and Paladin exception, Companion exclusivity and 1.35x progression rate are now specified. The following still need technical verification or a migration decision:

1. Verify Pet Resistance scaling and encode ordinary and terminal bonuses to match their intended flat/percentage behavior.
2. Choose how existing characters with multiple branches allocated should migrate. Recommended: controlled refund/reset preserving earned category experience, with no stranded points or repeatable item grants.
3. Verify modifier operations and stacking with RPG class bonuses. Critical Chance is confirmed as +2 percentage points per ordinary caster node and +5 points for Magistrate.
4. Finalize point costs for the two new Heart/Healing side nodes; retain existing node costs elsewhere. Verify the existing Colossus healing attribute's behavior.
5. Verify confirmed knockback resistance values and clamping: Bulwark +0.2, ordinary Companion +0.1, Guardian Stead +0.3.
6. Verify the +3% Draw Speed nodes on bows and crossbows, including Quick Charge interaction.
7. Locate original experience settings for every Adventurer/RPG class/weapon category and apply one 1.35x progression change without affecting global XP or doubling the multiplier.

## Implementation and acceptance checks, after approval

- Verify the actual first branch-specific node IDs and add pairwise exclusion links among Offensive and Defensive sibling branch entrances only; Utilities must have no branch exclusions.
- Check that links display red at the intended split and that deeper connections cannot unlock a second branch by another route.
- Verify each intended category independently, including the Companion rename and central-path removal.
- Verify Companion shows the selected starter sentence, has no five deleted middle-path nodes or dangling links, and retains its shared Pet Damage entrance and both reachable outer paths.
- Verify the left Companion ordinary amounts (+1 flat Pet Damage, +3% Pet Movement Speed, +2 hearts) and Packleader final bonuses (+20% Pet Damage, +15% Pet Movement Speed, +10% Pet Resistance).
- Verify the right Companion reward map and Guardian Stead final bonuses (+10 hearts, +25% Pet Resistance, +0.3 Knockback Resistance), including the complete-path +18-heart/+0.5-knockback-resistance totals and clamping; ensure the left/right specialty names and saved node identities are not swapped.
- Verify the left Companion reward map applies to owned pets rather than the player; test wolf damage, chase speed, outgoing attack knockback and the three minor defensive bonuses. Confirm respec/reload cannot duplicate pet modifiers.
- Verify Utilities contains no Fitness, Excavator or Insightful specialty nodes or dangling references, and that one character can acquire every remaining Utility node through normal progression without mutual exclusions.
- Verify Utilities contains no Magic Damage, General Resistance or Magic Resistance rewards or visible labels; confirm the four practical replacements above.
- Verify every anvil-cost node grants an actual 20% reduction individually and test their combined effect and rounding; keep tooltips consistent with observed behavior.
- Verify Defensive Colossus grants +5 maximum hearts (+10 HP) and +6% Healing, with its existing potion item reward removed.
- Verify the upper Defensive node assignments match the Aegis table and the renamed terminal grants +10% Magic Resistance and +10% Ranged Resistance, without its old rewards or item gifts.
- Verify bottom Defensive ordinary-node assignments match the table and Bulwark grants +2% General Resistance, +4% Ranged Resistance, +2 Armor and +0.2 additive Knockback Resistance, without retaining its old +4 Armor bonus or potion gifts.
- Verify the five ordinary Defensive line nodes run Heart -> Healing -> Heart -> Healing -> Heart from the category root toward Colossus.
- Verify exactly two new Defensive nodes sit above their hosts: Heart above the second-last node and Healing above the third-last node. They must not bypass branch exclusivity or obstruct the upper branch.
- Test new characters and existing multi-branch characters, including respec and relog behavior.
- Confirm school power and actual Arcane/Fire/Frost spell damage change, with no unintended double reward or healing bonus.
- Verify ordinary caster Spell Haste grants +2% and Spell Critical Chance grants +2 percentage points; Magistrate grants +5 percentage points critical chance, +5% haste and +5% power for Arcane/Fire/Frost only, with no health or Priest Healing Power bonus.
- Verify Magistrate grants the specified 5% power, 5% spell critical chance and 5% spell haste.
- Verify the Offensive ranged route contains no pet-damage, pet-resistance or movement-speed rewards at the four replaced nodes. Test the selected draw-speed bonuses on bows and crossbows and check projectile-speed coverage.
- Verify Martial Prowess grants no General Resistance and Hunter grants no Ranged Resistance; remove the matching tooltip lines without changing Defensive resistance rewards.
- Verify Adventurer melee/ranged damage rewards changed from 3% to 1% and Attack Speed rewards from 5% to 3% throughout the relevant definitions.
- Verify other ordinary stats preserve their author-defined amounts, including +2% Magic/Ranged Resistance; keep custom terminal rewards and RPG-specific exceptions intact.
- Verify RPG Frost/Fire/Arcane power rewards use 3%, RPG Healing Power (including Priest) increases from 1% to 3%, Paladin stays at all its defaults, and Rogue attack-speed rewards use 3%.
- Compare every Paladin reward against the original data and confirm no shared-definition change modified its defaults.
- Review total fully allocated Adventurer plus RPG Series bonuses and their attribute operations before accepting the balance.
- Verify branch completion grants no gift items, including after respec/reallocation; confirm removed item descriptions are gone and intended stats/passives still apply.
- Verify only one Companion branch can be invested in past the shared entrance, including respec, relog and bypass-route checks.
- Verify Adventurer, RPG Class Skills and RPG Weapon Skills progress at 1.35x the original rate using controlled equal-activity tests; confirm no global XP change or double scaling.
- Update player guidance and the handbook only when the changes are implemented. This draft is not a statement of current pack behavior.
