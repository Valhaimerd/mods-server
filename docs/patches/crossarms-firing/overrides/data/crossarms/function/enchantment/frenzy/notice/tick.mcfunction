# Notice lifetime is independent of the exhausted burst lock.
scoreboard players remove @s frenzy_notice 1
execute if score @s frenzy_notice matches 1.. run title @s actionbar {"translate":"crossarms.pack.frenzy.empty","fallback":"Empty - Reload","color":"yellow"}
execute if score @s frenzy_notice matches 0 run title @s actionbar {"text":""}
