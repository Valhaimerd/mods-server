# Utilities: Exclusive Paths — Design and Implementation Plan

**Status: implemented, installed and verified on 30 October 2026. See [runtime implementation](../../patches/utilities-paths/README.md) and [delivery evidence](../../patches/utilities-paths/verification.json). The design notes below preserve the original planning decisions.**

The owner approved the three utility identities and mastery passives, then requested the percentage adjustments and node allocation below. This document is the proposed allocation for review; it does not change installed tree data, sources, allocations, player documentation or JARs.

## Direction and alternatives

Use **Fitness / Adaptability / Ingenuity**, exclusive from their first branch-specific nodes. Keep Utilities independent of Offensive, Defensive and Companion choices. Restore one mastery at the top of each utility branch. Remove all utility movement-speed, mining-speed and Fortune rewards; the accepted jump, climbing and sneak benefits remain.

Considered layouts:

1. Preserve 7/9/7 ordinary nodes: least geometry change, but Adaptability costs two extra points to complete.
2. **Recommended: 7/7/7 ordinary nodes plus one mastery each.** Remove the middle route's two outer mining leaves. Existing trunk and useful branches stay; all three routes cost eight branch-specific points at one point per node. Root/shared entry costs remain as installed.
3. Expand all branches to nine ordinary nodes: preserves the middle route but adds four ordinary nodes without a gameplay need.

Option 2 is the proposed design, not a choice already explicitly made by the owner. The owner has authorized planning, not execution.

## Inspected installed baseline

- Minecraft 26.2, NeoForge 26.2.0.90, Java 25+.
- Archive: `mods/adventurerskills-EN-1.3-26.2-neoforge-fixed.jar`.
- Inspected SHA-256: `87a3b8f0d41c5c5842cc8dd81f77b1776bb8afe91ee44e8852c3e1690364cb05`. Re-inspect before implementation; other work may change this archive.
- Resources: `data/adventurer_skills/puffish_skills/categories/adventurer_skills/{skills,definitions,connections}.json`.
- Utilities has 25 nodes: root + shared entry + 7 Fitness-side + 9 middle + 7 Ingenuity-side ordinary nodes. The current connected component has no utility masteries and no utility exclusions.
- Current root: `h8krq290mbf4dwdx` at `(0,-64)`. Shared pickaxe entry: `nu1hd3hh115xcpax` at `(0,-136)`.
- Shared definitions are also used outside Utilities. Create utility-specific definitions and change only the mapped instances; do not edit global movement, mining, XP or repair definitions.
- Historical mastery IDs/positions were verified against the locally preserved upstream archive: Fitness `i0j6pbbluuqne53e` at `(-340,-476)`; Excavator `s02hkhd2ha87ji1n` at `(0,-544)`; Insightful `qxcrnvfslhwdg5ip` at `(340,-476)`.
- The old unrestricted design remains an implementation/history record in `docs/patches/skill-overhaul/design.md`. This proposed plan supersedes its Utilities requirements only after implementation is approved.

## Shared entry — proposed allocation choice

Keep the root and shared connector. Replace the shared entry's +2% Pickaxe Speed with **Preparedness: +4% Stamina**. This avoids a paid empty connector and grants a small practical benefit before the path choice.

Fitness supplies another 16%, reaching **20% total Stamina** including Preparedness. Adaptability and Ingenuity also receive the shared 4%, with no additional stamina nodes. This shared-node reward is a recommendation, not a user-specified new bonus. Its tooltip must say exactly `+4% Stamina`.

Stamina is the installed Puffish food-exhaustion attribute; do not describe it as a separate stamina bar or claim that +20% Stamina means exactly 20% less hunger consumption.

## Full-path budgets

All ordinary allocations below are whole-number percentages. Totals are additive within this utility system, not repeated compounding multipliers.

| Benefit | Fitness | Adaptability | Ingenuity |
| --- | --- | --- | --- |
| Stamina, including shared entry | +20% | +4% shared | +4% shared |
| Food restored | +14% | — | — |
| Saturation received | +14% | — | — |
| Underwater breathing duration | +24% | — | — |
| Jump strength | — | +10% | — |
| Safe fall distance | — | +4 blocks | — |
| Ladder/vine climbing speed | — | +24% | — |
| Sneak speed | — | +24% | — |
| Environmental freezing buildup | — | 25% slower | — |
| Avoid eligible tool durability loss | — | — | 16% chance |
| Ordinary XP gained | — | — | +16% |
| Anvil XP cost | — | — | −33% |
| Enchanting XP payment | — | — | −33% |
| Luck | — | — | +1 from mastery |

The 25% freezing-buildup reduction and 10% jump target are retained from the previously accepted proposal; the latest request did not replace them. There is no utility movement-speed bonus, underwater mining bonus or ordinary tool-breaking-speed reward.

## Exact node allocation

Positions refer to installed tree coordinates; keep them unless noted. Text below is player-facing reward text, not literal implementation formulas.

### Fitness — left path

| Label | Existing node ID | Position | New rewards |
| --- | --- | --- | --- |
| F1, entrance | `9qyrqud3g16qneu1` | `(-68,-204)` | +8% Stamina |
| F2, lower junction | `yts2dfnkpz3xqziu` | `(-204,-272)` | +7% Food Restored; +7% Saturation Received |
| F3, lower side leaf | `tv2lczi8824fadw6` | `(-68,-272)` | +8% Stamina |
| F4, upper junction | `cls3nuryikjn5pxx` | `(-272,-340)` | +8% Underwater Breathing Duration |
| F5, middle side leaf | `tt2rbu9d7n91o1a2` | `(-204,-340)` | +7% Food Restored; +7% Saturation Received |
| F6, upper-left leaf | `o327why5r32ntmbc` | `(-340,-408)` | +8% Underwater Breathing Duration |
| F7, upper-right leaf | `1f01nhbrg46ypfdc` | `(-272,-408)` | +8% Underwater Breathing Duration |
| F8, restored mastery | `i0j6pbbluuqne53e` | `(-340,-476)` | **Fitness: Emergency Rations** |

Preserve current within-path connections. Add F6–F8. F8 also requires F1–F7, so optional leaves cannot be skipped to acquire the mastery.

### Adaptability — middle path

| Label | Existing node ID | Position | New rewards |
| --- | --- | --- | --- |
| A1, entrance | `onn0ggru4ltstcsx` | `(0,-204)` | +5% Jump Strength; +1 Block Safe Fall Distance |
| A2, lower trunk | `axl8alff75wi5qjq` | `(0,-272)` | +1 Block Safe Fall Distance |
| A3, upper junction | `egztdm9c9i404yjo` | `(0,-340)` | +12% Ladder/Vine Climbing Speed; +12% Sneak Speed |
| A4, left branch | `0jtmcffuara4whxe` | `(-136,-340)` | +1 Block Safe Fall Distance |
| A5, right branch | `2lw02if80lxvfvh9` | `(136,-340)` | +12% Ladder/Vine Climbing Speed; +12% Sneak Speed |
| A6, upper-left node | `3i4j4n1jy9kq63b3` | `(-136,-408)` | +5% Jump Strength; +1 Block Safe Fall Distance |
| A7, upper-right node | `8qvbdogd9x4xoycl` | `(136,-408)` | 25% Slower Environmental Freezing Buildup |
| A8, restored/renamed mastery | `s02hkhd2ha87ji1n` | **`(0,-476)`**, moved from historical `(0,-544)` | **Adaptability: Sure Footing** |

Remove obsolete terminal mining leaves `yu7a75bw5anm9z2u` at `(-68,-408)` and `lkc01rrygmocbish` at `(68,-408)`, and their normal links. These are leaves, not route connectors. Preserve the remaining within-path graph. Add A6–A8 and A7–A8, with A1–A7 required for the mastery. Aligning the three mastery heights is the recommended layout change.

### Ingenuity — right path

| Label | Existing node ID | Position | New rewards |
| --- | --- | --- | --- |
| I1, entrance | `2pj0fbogctx589t3` | `(68,-204)` | +8% Ordinary XP Gain |
| I2, lower junction | `gh96zb6t67hnmswd` | `(204,-272)` | −11% Anvil XP Cost; −11% Enchanting XP Payment |
| I3, lower side leaf | `46o8u8oydffa04o6` | `(68,-272)` | 8% Chance to Preserve Tool Durability |
| I4, upper junction | `gk57oho18h66ghxe` | `(272,-340)` | −11% Anvil XP Cost; −11% Enchanting XP Payment |
| I5, middle side leaf | `b4r7snauht7yus9f` | `(204,-340)` | +8% Ordinary XP Gain |
| I6, upper-left leaf | `vaios6drja2eapyg` | `(272,-408)` | 8% Chance to Preserve Tool Durability |
| I7, upper-right leaf | `arhes7xrvfitahm3` | `(340,-408)` | −11% Anvil XP Cost; −11% Enchanting XP Payment |
| I8, restored/renamed mastery | `qxcrnvfslhwdg5ip` | `(340,-476)` | **Ingenuity: Careful Preparation**; +1 Luck |

Each of the three cost nodes grants both discounts, not three nodes shared between the two costs. Three nodes therefore provide exactly −33% to each cost. Two durability nodes add to one 16% roll rather than making two independent 8% rolls. Two XP nodes add to +16%, not 1.08 multiplied twice.

Preserve current within-path connections. Add I7–I8, requiring I1–I7 for the mastery. Luck is on the mastery to avoid adding an eighth ordinary node.

## Mastery passives — accepted behavior

### Fitness: Emergency Rations

Player description: **“When hunger falls to three drumsticks or less, restore four drumsticks. Cooldown: 3 minutes.”**

- At hunger ≤6 food points, restore 8 food points, clamped to the normal maximum. No added saturation and no direct heal.
- One proc per 180 seconds, per player. This is a fixed utility interval; do not add class-haste scaling.
- This is a resource grant, not eating an item. It must not trigger the +14% food bonus, food-effect extension, eating procs or backpack consumption.
- Preserve the cooldown across logout, death and Orb/refund reallocation; respec cannot refill it. Block activation while dead/downed or spectating. This blocks procs, not the cooldown clock.

### Adaptability: Sure Footing

Player description: **“Soften a damaging landing by 50%. For 5 seconds afterward, resist powder-snow sinking and freezing damage. Cooldown: 20 seconds.”**

- Trigger only when the landing would still cause positive fall damage after safe fall distance and other existing fall protection. Harmless jumps, Moon Stone immunity, creative flight and canceled damage do not consume the cooldown.
- Apply a separate 50% multiplier to the qualifying fall damage, then start a 20-second fixed cooldown.
- For 5 seconds, prevent powder-snow sinking and environmental freezing damage. Do not clear or prevent class Frozen, root, stun or other movement-control effects.
- The ordinary 25% slower freezing buildup affects environmental accumulation only. Use fractional accumulation so 0.75 ticks of buildup per tick does not round to zero and become permanent immunity.
- Cooldown survives equipment/skill reallocation and logout. Block activation while dead/downed or spectating. Temporary protection expires on death; do not keep a saved always-on immunity flag.

Both cooldown passives use a persisted server-game-time expiry. Time advances while the server runs, including when the player is offline/downed/spectating; stopping the server pauses the clock. Copy the expiry across respawn and do not reset it on allocation. Temporary five-second snow protection follows ordinary online effect duration and clears on death/logout rather than being saved as a permanent flag.

### Ingenuity: Careful Preparation

Player description: **“Beneficial effects from food and drinks last 35% longer.”**

- Extend the original finite duration of eligible positive food/drink effects once, using the original consumed effect payload. Do not multiply an already-extended active duration on every refresh.
- Round extended duration once to the nearest server tick. Instant effects, negative effects, equipment effects, class buffs, splash/lingering effects and effects applied without consumption do not qualify.
- Milk does not manufacture positive effects. Drinking a potion qualifies; throwing it does not. Native effect replacement/refresh rules remain.
- Identical extension sources do not repeatedly apply this mastery. Other mods' separate duration changes require a test confirming this mastery is applied exactly once.
- The mastery also grants +1 Luck. Describe Luck as affecting supported loot tables, not guaranteed universal better drops or Fortune.

## Exclusive paths and mastery requirements

- Pairwise entrance exclusions: F1 `9qyrqud3g16qneu1`, A1 `onn0ggru4ltstcsx`, I1 `2pj0fbogctx589t3`.
- Exactly three exclusion links: F1–A1, F1–I1, A1–I1. Use the installed Puffish exclusion format.
- Every ordinary node must have a valid route through its own entrance. There must be no normal cross-path link above those entrances. The shared entry stays available to all three.
- The first purchased branch-specific node commits the path. Respec releases the choice only after all nodes in that path are refunded; do not allow partial-refund bypasses with retained deeper nodes.
- Each mastery requires its own seven ordinary nodes as well as its entrance path. Prefer supported data requirements; if the installed format lacks that capability, use one narrow server allocation validator rather than inventing unsupported JSON fields.
- Passive runtime checks must confirm the valid allocated path, not merely a restored historical mastery ID.
- Do not restore old combat bonuses, item gifts, command rewards or original specialty descriptions when reusing the historical IDs.

## Exact percentage and cost semantics

### Additive path budgets

Compute each utility benefit from the total allocated nodes for that benefit, then apply one effective modifier/operation appropriate to the installed attribute. Reuse native attributes when their behavior matches the intended result. Do not compound the two 7%, two 8%, three 8% or three 11% rewards.

Food and saturation increases are separate **14% bonuses to their original amounts**. Do not derive saturation from already-increased food and then apply another 14%, which would create an unintended 29.96% increase. Saturation supports fractions. For integer hunger points, carry the fractional earned remainder between actual meals, so small foods do not get a full extra hunger point on every bite. Do not save overflow that was discarded because the wearer was already full.

Underwater breathing means 24% more full-air time under the same conditions, not +24 raw oxygen points. Test with Respiration and existing water effects, and avoid interfering with Melfork/Gills water breathing or land penalties.

Sneak speed is a conditional crouching benefit, not a general movement-speed modifier. Target +24% relative crouched speed, preserving native normal-speed limits. At an ordinary 0.3 movement ratio, the full benefit is 0.372. Test Swift Sneak and modded sneaking; do not accelerate ordinary walking/sprinting. Climbing applies to deliberate upward ladder/vine movement, not arbitrary wall climbing or falling.

### Anvil: proposed rounding rule

The utility multiplier is exactly **1 − 0.11 × allocated cost nodes**, or 0.89 / 0.78 / 0.67. It combines with separate existing discounts once, without restoring the old four anvil nodes' rewards.

Apply to the relevant native anvil XP cost, round up to a whole displayed level, and keep positive transactions at least one level. Zero-cost transactions stay zero. Client display, affordability and server payment must agree. Keep repair materials and prior-work behavior native; test rename, combine and repair operations separately before claiming all are supported.

### Enchanting: recommended interpretation for review

Reduce the **actual XP payment** by 11/22/33%, preserving lapis consumption, bookshelf/enchantment quality and offer eligibility. This is recommended as an XP-point payment discount, not reduced level eligibility: a tier requiring level30 still requires level30 to select it.

Reason: applying ceiling to the ordinary 1/2/3-level payment would leave a three-level payment at three even with 33% reduction. Determine the native level payment after applicable native/mod cost adjustments. Compute its exact XP-point equivalent using a pure snapshot of the player's pre-payment level and bar progress, reproducing native level-debit semantics, including how that same progress fraction is preserved after the level change. Do not assume that one level always costs a fixed number of XP points.

Apply the utility factor to that XP-point debit once. For integer payment, floor the accumulated discounted debit plus its saved fractional debit remainder, preserve the remaining fraction, and keep positive transactions between one XP point and their native debit. Transactions already costing zero remain zero; a minimum-payment clamp must not build negative credit or a later charge greater than the native payment. Apply the final payment directly as the transaction's debit; do not simulate charging full cost and then refunding XP through reward hooks. Cost savings are never ordinary XP gains.

Show the effective XP payment and a short `−33% Enchanting XP Cost` description without exposing internal arithmetic. Do not include Penchant's combat progression, anvil transactions a second time, or unrelated spell-binding costs. Tests must cover identical offers at level30 with empty/partial/full-next-level progress, a level-boundary payment, mixed cost modifiers and repeated transactions with fractional remainders.

This XP-point interpretation is a proposed implementation choice; it has not been separately confirmed by the owner. It should be visible in the review summary rather than silently changing enchantment eligibility.

### Durability and XP

- One 16% preservation chance per eligible tool durability-loss event. Do not add a second preservation roll per allocated node. Keep Unbreaking's native behavior; apply this extra chance once to otherwise qualifying loss.
- Limit this reward to actual tool-use wear. It does not protect armor, shields, elytra or scripted durability costs unless separately approved. Test pickaxes, axes, shovels, hoes and shears; do not promise automatic coverage for every modded device.
- +16% XP applies once to ordinary newly earned player XP, after other applicable native reward calculations and with native attribution. Remove the old utility XP reward from these node instances so it is not also applied through a second attribute hook. The two nodes contribute to a single +8%/+16% bonus calculation.
- For integer XP, carry the earned fractional bonus per player: award the floor of eligible XP times the utility bonus plus the saved bonus fraction, then preserve the remaining fraction. Thus seven eligible one-XP awards at +16% produce eight credited XP with0.12 bonus XP left toward a future award. Do not create physical bonus orbs or route bonus credit back through the reward hook.
- Exclude skill refunds, XP transfers, stored-XP withdrawals, repair/enchant refunds and recursive bonuses. Keep the XP bonus remainder separate from enchanting's fractional payment remainder. Do not change class/category progression formulas; verify any native interaction with player-XP gains before documenting it.

## Save migration and reset behavior

New exclusions invalidate some existing mixed-path allocations. Perform a one-time **Utilities-only refund**: restore spent utility points, preserve earned category XP and leave Offensive, Defensive and Companion allocations unchanged. Include removed leaves and the three historical mastery IDs from very old saves.

Use the installed Puffish per-node refund/server API where available and verify the point accounting with snapshots. The previous broad Adventurer migration is not permission to refund the entire tree again. If no safe selective refund API exists, design and test the minimal selective migration before implementation; do not silently use a global reset.

Clear obsolete utility modifiers and stored mastery-owned state while preserving active cooldown timestamps. Do not remove unrelated effects from other mods. Verify Orb resets, reconnects, death/respawn and a second login without another refund.

## Future implementation tasks — do not execute now

> Use the existing pack workflow and native implementation. The following checkboxes are deliberately incomplete.

### 1. Tree data and runtime contract

Files to inspect/update after approval: `docs/patches/skill-overhaul/repack.py`, the latest same-name Adventurer patch pipeline and its artifact manifest; exact installed category resources above.

- [ ] Capture current archive hashes and allocations before editing. Rebase the patch on the latest installed features rather than blindly rebuilding an older upstream repair.
- [ ] Add isolated checks for exactly seven ordinary nodes plus one mastery per path; retained shared entry; three exclusions; mastery completeness; no upper bypass; no old commands or combat rewards.
- [ ] Map the 21 retained ordinary node IDs, retire two leaves, restore three mastery IDs and apply utility-specific definitions with concise text/icons.
- [ ] Use purposeful food/breathing, climbing/fall and equipment/XP icons. Masteries use challenge frames and titles `Fitness`, `Adaptability`, `Ingenuity`.

### 2. Utility stats and mastery effects

Likely runtime owner: the existing skill compatibility module beside `docs/patches/skill-overhaul/SkillOverhaul.java`; keep the exact owner choice pending inspection of current follow-up patches.

- [ ] Pin tests for one-node/two-node/full-path totals, food/saturation independence, fractional food, water breathing, climb/sneak scope, safe fall and environmental freezing.
- [ ] Pin deterministic tests for 0.89/0.78/0.67 costs, integer anvil rounding, level30 eligibility, lapis unchanged, real XP debits, near-zero costs and interaction with current accessory discounts.
- [ ] Pin tests for Emergency Rations at hunger6/7, exactly8 food restored, no saturation/consumption proc, cooldown boundary and reset persistence.
- [ ] Pin tests for Sure Footing positive/zero/canceled fall damage, +4 safe fall, 50% retained fall damage, five-second protection, class Frozen not purged and twenty-second cooldown.
- [ ] Pin tests for 35% positive food/drink duration, negative/instant/excluded sources unchanged and no repeated multiplication.

### 3. Selective migration and pack verification

- [ ] Reproduce a character with nodes across all three utility paths and old mastery allocations. Verify exact Utilities refund while other allocations/XP remain unchanged.
- [ ] Test whole-path and partial refunds, stale saved modifiers, logout/death and Orb reset. Verify no duplicate refund on later login.
- [ ] Run the smallest isolated server with current Skills/Attributes, compatible migration module and required hooks. Add Better Party/current gear only for the interaction tests that need them.
- [ ] In an isolated client, verify the complete layout, exclusions, locked masteries, readable brief descriptions, icons, exact cost display and allocation/reset flow.

### 4. Documentation and delivery after implementation

- [ ] Update the implemented Utilities section of `docs/patches/skill-overhaul/design.md`/README, relevant owner references and verification records. Keep this plan as the reviewed design record.
- [ ] Update `docs/MODLIST.md`, `README.md` and the handbook PDF only once installed behavior changes; rebuild and visually review the PDF then.
- [ ] Retain active filenames for same-version repacks, keep backups ignored, install only verified final archives in active/fixed-pack folders. Do not change personal Minecraft/server installations.
- [ ] Do not commit or push unless requested separately.

## Open review choices

1. Shared +4% Stamina entry, with Fitness supplying the remaining16%, instead of a paid empty connector.
2. Remove two middle mining leaves to normalize all three routes to seven ordinary nodes; restore the middle mastery at the same height as the other two.
3. Put +1 Luck on Ingenuity's mastery, with the 35% duration passive.
4. Each cost node grants both −11% anvil and −11% enchanting XP payment.
5. Enchanting discounts actual XP payment rather than level eligibility; lapis and offer quality remain unchanged.

These are explicit recommendations for review. No gameplay change has been made by writing this plan.

## Execution notes

The owner approved implementation together with the expensive Orb recipe and 30-level use cost. All five recommended choices above are included. Mastery completeness uses the native required-skills count with seven own-path neighbor links, rather than an unsupported JSON requirement. Enchanting pays discounted XP points while preserving offer requirements and lapis. Runtime tests verify selective migration and exact additive totals.

## Delivery

Installed all three same-filename archives in active and fixed packs; previous builds remain in retired packs. Passed68Utilities server checks,28Orb runtime checks, connected-client reward/cost/reset checks, exact archive/hash/CRC checks and51handbook contents destinations. The80-page handbook was rebuilt and visually reviewed. Detailed test coverage and limitations are in the verification record; the original checklist above is retained as design history rather than a claim that every proposed test scenario ran. No commit or push was requested.
