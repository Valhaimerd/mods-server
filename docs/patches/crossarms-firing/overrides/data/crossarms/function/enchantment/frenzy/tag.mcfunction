tag @s add FRENZY
# Read the actual projectile owner, rather than the nearest player.
scoreboard players set @s frenzy_stage 0
execute store result score @s frenzy_stage run execute on origin run scoreboard players get @s heat
execute if score @s frenzy_stage matches ..300 store result entity @s damage double 0.0000005 run data get entity @s damage 1000000
execute if score @s frenzy_stage matches 301..450 store result entity @s damage double 0.0000006 run data get entity @s damage 1000000
execute if score @s frenzy_stage matches 451..610 store result entity @s damage double 0.00000075 run data get entity @s damage 1000000
function crossarms:enchantment/frenzy/arrow/calculate_color
