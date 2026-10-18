# Once per world tick, independent of equipped hands. Exhausted bursts require a crank.
execute if score @s frenzy_empty matches 1 run return fail
execute if score @s frenzy_shots matches 1.. run scoreboard players add @s frenzy_idle 1
execute if score @s frenzy_idle matches 60.. run scoreboard players set @s frenzy_shots 0
execute if score @s frenzy_idle matches 60.. run scoreboard players set @s frenzy_idle 0
