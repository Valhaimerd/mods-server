execute if items entity @s weapon.offhand *[minecraft:charged_projectiles] unless items entity @s weapon.offhand #minecraft:enchantable/crossbow[minecraft:charged_projectiles=[]] run return fail
function crossarms:enchantment/frenzy/shoot {slot:"offhand"}
return 1