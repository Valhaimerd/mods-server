# Obtain the effective maximum damage through the native loot function.
$item modify entity @s weapon.$(slot) {function:"minecraft:set_damage",damage:0}
$execute if items entity @s weapon.$(slot) * run function crossarms:enchantment/frenzy/read_damage_$(slot)
scoreboard players remove @s temp 2
execute store result storage crossarms:frenzy damage int 1 run scoreboard players get @s temp
$data modify storage crossarms:frenzy slot set value "$(slot)"
function crossarms:enchantment/frenzy/set_damage with storage crossarms:frenzy
playsound minecraft:entity.item.break player @a ~ ~ ~ 1 1.2
