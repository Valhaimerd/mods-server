# Paladin tier-III wording review

Player-visible text uses `%`; language resources escape it as `%%`. Cooldowns, range and activation details stay in native/gold statistics rows. Result-first text retains essential Purgatory risks and Ardent?s survival limitation.

| Key | Previous text | Installed text |
|---|---|---|
| `spell.paladins.battle_banner.description` | Increases attack speed and knockback resistance for you and allies nearby, within {cloud_radius} blocks, for {cloud_duration} seconds. | Carry a banner that builds nearby allies’ combat buffs and heals them. Each ally can receive one brief rescue from low health or a fatal hit. |
| `spell.paladins.immolation.name` | Immolation | Purgatory |
| `spell.paladins.immolation.description` | Erupts in holy fire, dealing 120% of your Healing power as damage to nearby enemies and setting them ablaze, while healing you and nearby allies by 50% of your Healing power. The longer the cast is held, the harder it hits and the wider it spreads. | Channel holy fire, burning and drawing enemies inward. Pulses grow from 10% to 20% and 30% holy hybrid damage after 10s and 20s, costing 2 and 3 HP in those later stages. Allies gain regeneration; the final phase protects you and builds a powerful finishing explosion. Stun or Frozen interrupts the channel. |
| `effect.paladins.battle_banner.description` | Increases attack speed and knockback resistance | Combat bonuses build while you remain near the banner. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_root.name` | Improved Battle Banner | Improved Battle Banner |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_root.description` | Reduces the cooldown of Battle Banner by {cooldown_duration_deduct} sec. | Battle Banner reaches 2 blocks farther. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_modifier_1.name` | Persistent Banner | Persistent Banner |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_modifier_1.description` | Increases the duration of Battle Banner by {spawn_duration_add} sec. | Battle Banner lasts 5s longer. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_modifier_2.name` | Protective Banner | Intense Banner |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_1_modifier_2.description` | Battle Banner also reduces damage taken by {effect\|skill_tree_rpgs:banner_protection\|0\|\|abs}. | Raises each Banner buff’s maximum by 5%. Healing is unchanged. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_root.name` | Improved Immolation | Improved Purgatory |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_root.description` | Radius of Immolation increased by {range_add} blocks. | Purgatory’s flames reach 1 block farther; its pull and finishing effects reach 2 blocks farther. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_modifier_1.name` | Condemn | Damnation |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_modifier_1.description` | Immolation drags struck enemies towards you. | Purgatory pulses deal 15%, 25% and 35% holy hybrid damage. Its finishing explosion can reach 900% Melee Damage. |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_modifier_2.name` | Consecration | Final Resolute |
| `spell.skill_tree_rpgs.paladin_tier_4_spell_2_modifier_2.description` | Immolation consecrates the ground beneath you, dealing 20% of your Healing power as damage to enemies, for {cloud_duration} sec. | During Purgatory’s final phase, critical health or a fatal hit ends the channel with its earned explosion, shields allies and protects you for 3s. |
| `spell.skill_tree_rpgs.paladin_tier_3_passive_1.name` | Ardent Defender | Ardent Defender |
| `spell.skill_tree_rpgs.paladin_tier_3_passive_1.description` | Upon taking damage below 30% health, your max health is increased by {effect\|skill_tree_rpgs:ardent_defender\|0\|\|abs}, for {effect_duration} sec. | An endangered nearby party player gains Resistance I, 10 armor and 5% damage reduction, and heals for 40% maximum HP. A fatal hit can still kill them. |
| `spell.skill_tree_rpgs.paladin_tier_3_passive_2.name` | Divine Hammer | Reinforced |
| `spell.skill_tree_rpgs.paladin_tier_3_passive_2.description` | Melee attacks throw a hammer at the target, dealing 50% melee damage, ricocheting {ricochet} to nearby enemies. | Gain 5% Healing Power and base armor at each low-health threshold, up to 20% below 40% HP. |
