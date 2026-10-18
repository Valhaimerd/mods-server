data modify entity @s data.color set value 65280
execute if score @s frenzy_stage matches 151..300 run data modify entity @s data.color set value 16776960
execute if score @s frenzy_stage matches 301..450 run data modify entity @s data.color set value 16711680
execute if score @s frenzy_stage matches 451..610 run data modify entity @s data.color set value 11141120
execute if score @s frenzy_stage matches 611.. run data modify entity @s data.color set value 0
