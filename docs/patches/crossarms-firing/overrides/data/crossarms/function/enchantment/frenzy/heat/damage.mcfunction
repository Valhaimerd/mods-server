execute if score @s heat matches 301..450 run damage @s 1 minecraft:magic
execute if score @s heat matches 451..610 run damage @s 2 minecraft:magic
execute if score @s heat matches 611.. run damage @s 4 minecraft:magic
scoreboard players set @s frenzy_hurt 0
