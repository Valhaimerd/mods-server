execute if items entity @s weapon.mainhand *[minecraft:charged_projectiles] unless items entity @s weapon.mainhand #minecraft:enchantable/crossbow[minecraft:charged_projectiles=[]] run return fail
function crossarms:enchantment/frenzy/shoot {slot:"mainhand"}
return 1