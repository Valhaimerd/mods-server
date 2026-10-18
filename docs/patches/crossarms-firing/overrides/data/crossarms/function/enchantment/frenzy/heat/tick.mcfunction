# Player-owned heat persists while switching weapons. Cool before sampling the bar.
scoreboard players remove @s heat 2
execute if score @s heat matches ..300 run scoreboard players set @s frenzy_hurt 0
execute if score @s heat matches 301.. run scoreboard players add @s frenzy_hurt 1
execute if score @s frenzy_hurt matches 20.. run function crossarms:enchantment/frenzy/heat/damage
function crossarms:enchantment/frenzy/heat/tittle
