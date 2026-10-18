execute if score @s inventory_ammo matches 1.. run scoreboard players add @s heat 20
execute unless score @s[gamemode=creative] inventory_ammo matches 1.. run scoreboard players add @s heat 20
execute if score @s heat matches 240..299 run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.5
execute if score @s heat matches 600..649 run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 2
