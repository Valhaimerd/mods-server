execute as @e[tag=MULTISHOT] at @s run function crossarms:enchantment/multishot/calc_damage
execute as @e[tag=BOMBARD] at @s run particle minecraft:dust{scale:4,color:[1,0,0]} ~ ~ ~ 0 0 0 0 1 force
execute as @e[tag=ACCELERATION] at @s run particle minecraft:electric_spark ~ ~ ~ 0 0 0 1 10
execute as @e[tag=ZERO_DROP] at @s run function crossarms:enchantment/zero_drop/tick
execute as @e[tag=RICOCHET] at @s run function crossarms:enchantment/ricochet/tick
execute as @e[tag=DEFLECTOR] at @s run function crossarms:enchantment/deflector/tick
execute as @e[tag=PIERCING] at @s run function crossarms:enchantment/piercing/tick
execute as @e[tag=FRENZY] at @s run function crossarms:enchantment/frenzy/arrow/tick with entity @s data
execute as @e[scores={heat=1..}] at @s run function crossarms:enchantment/frenzy/heat/tick
execute as @a[scores={death=1..}] run function crossarms:enchantment/frenzy/heat/reset
execute as @a[scores={frenzy_shots=1..}] run function crossarms:enchantment/frenzy/burst/tick
execute as @a[scores={frenzy_notice=1..}] run function crossarms:enchantment/frenzy/notice/tick
