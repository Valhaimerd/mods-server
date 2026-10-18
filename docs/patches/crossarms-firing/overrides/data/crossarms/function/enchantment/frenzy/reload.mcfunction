execute if score @s frenzy_empty matches 1 run return fail
execute store result score @s inventory_ammo run clear @s minecraft:arrow 0
execute if score @s[gamemode=!creative] inventory_ammo matches 0 run return fail
# Infinity still requires one normal arrow, but does not consume it.
$execute unless predicate crossarms:frenzy/infinity_$(slot) run clear @s[gamemode=!creative] minecraft:arrow 1
$item modify entity @s weapon.$(slot) crossarms:common/charge
# Prevent collecting free Infinity projectiles.
$execute if predicate crossarms:frenzy/infinity_$(slot) run item modify entity @s weapon.$(slot) crossarms:frenzy/charge_infinite
scoreboard players set @s HS_delay 3
