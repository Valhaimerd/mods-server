execute if score @s frenzy_empty matches 1 run return fail
execute if score @s HS_delay matches 1.. run return run scoreboard players remove @s HS_delay 1
$execute if predicate crossarms:frenzy/broken_$(slot) run return fail
$execute if function crossarms:enchantment/frenzy/$(slot)/needs_load run return run function crossarms:enchantment/frenzy/reload {slot:$(slot)}
