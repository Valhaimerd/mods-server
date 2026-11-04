# Combined class-balance phases 4-6

4 November 2026. Arcane, Fire, weapon-spell and Frost follow-up over the **tested phase 1-3 inputs**, retaining saved spell/skill IDs and current JAR filenames. Clients and servers require matching replacements and a restart. Installation and verification are recorded in [progress.md](progress.md); phase 7 onward is separate.

`P` is the relevant school power. Damage coefficients below precede armor, resistance and the existing global damage retention: player/owned-summon damage remains 80% against mobs and 35% against players. The phase 1-3 defense, Rogue, Warrior and original-coefficient restoration remain intact.

**Hydra follow-up:** [20-block detection with separate firing range](../hydra-detection/README.md) supersedes the Hydra targeting/movement wording below. Other phase 4-6 values remain.

## Arcane

| Skill/node | Current behavior |
| --- | --- |
| Fissile Magic | 100% proc chance, 30% P, 6s haste-affected cooldown. The original victim is included once; existing five-block splash and squared falloff remain |
| Evocation | Ten deliveries at 0.3s intervals over 3s, starting at 0.3s. Rapid Evocation delivers twelve at 0.25s intervals. Positive critical-chance/haste stacks remain; increased damage taken is removed. Existing proportional cooldown and duration branches remain |
| Burst Blink | Can cast under movement control and cleanses it after a successful teleport. Existing collision, border, hostile Purgatory boundary, equipment, death and downed guards remain; no new immunity |
| Blitz, formerly Purge | Two saved, serially recovering charges. Each uses the normal 12s haste-affected recharge, travels up to 7.5 blocks and causes 20% P blasts within 2.5 blocks at departure and arrival. No Speed buff. Improved cannot add another five blocks. The HUD shows count/recovery; switching equipment, respec, death and reconnect do not create free charges |
| Presence of Mind | Any of the six Arcane class actives arms the existing five-second readiness; basics do not arm it. The next Arcane spell applies 20% Arcane vulnerability for 6s. Base cooldown 15s, affected by haste; a cast cannot consume the readiness it has just armed |
| Arcane Ward | Existing six-second absorption and 12s haste cooldown, without refill. A qualifying hostile hit also moves the caster four blocks horizontally away once, when a supported, clear landing exists. No safe landing or no shield capacity means no teleport |
| Arcane Emitter | Its own barrage bolts launch at 0.8x previous speed. Player Arcane Bolt and other summon bolts are unchanged |

Blink keeps native partial/blocked landing behavior. Its fractional final step is completed only when the route, destination and existing boundary checks allow it. A failed cast does not spend a Blitz charge or retain a previous success flag.

## Fire and weapon spells

| Skill/node | Current behavior |
| --- | --- |
| Fire/Frost weapon projectiles | 1.3x previous launch speed, with gentle near-aim correction. Fireball 1.3; Fire Blast 1.625; Frostbolt/Frost Shard 1.56. Acquisition requires a visible eligible enemy within eight blocks and ten degrees of the forward axis; native steering is 1.5 degrees. Party allies, pets and vanished targets remain excluded; ricochet/bounce behavior remains |
| Arcane/Holy weapon targeting | 24 blocks for Arcane Bolt, Arcane Blast, Heal and Holy Shock. Actual delivery types are preserved: Arcane wand Bolt remains a projectile, while staff Blast and Holy basics retain direct targeting |
| Arcane/Flame/Frost Trap | Five-second lifetime, with no ten-second lifetime upgrade restored |
| Fire Breath | 14% P per normalized delivery. Hits pull horizontally toward the fan's centerline; Flame Throwing uses the same behavior over its extended reach |
| Whirlpool Firestorm | Stronger inward pull; Slowness I for 1s replaces Slowness II. Knockback resistance and normal movement/collision still apply |
| Hot Impact | 100% chance; existing stun and cooldown remain |
| Eruption | 30% P explosion centered on the attacker, with strong upward knockback; 8s haste cooldown. Damage without an eligible attacker does not trigger it |
| Flameward | Retaliates for 20% P while absorption remained at the start of a qualifying hit. One shared fixed 0.5s interval prevents beam spam. The final shield-depleting hit can retaliate once; spent absorption is not replenished |
| Ancient Hydra | Only the single large Hydra slowly advances toward its existing selected enemy, about one block per second on clear ground. It stops at close range, obstacles, unsupported ground or movement control; target selection is unchanged. Its cosmetic ground pulse is three times larger, without increasing attack radius or damage. Ordinary Hydras remain stationary |

The basic-spell audit found Arcane wand Bolt is projectile-based in this installed version. It was deliberately not converted to instant damage.

## Frost

| Skill/node | Current behavior |
| --- | --- |
| Frostbite exposure | Damaging Frost class skills, Frost staff/wand basics and owned Frost Elementals apply 5s at the native freezing-damage threshold. A successful Frozen application extends this to 10s. This is native freezing damage, with native immunity/leather handling, not a second damage loop. Expiry/dispel releases only this system's contribution and preserves external powder-snow exposure |
| Deep Freeze | Adds 1s to Frost Nova's freeze; 3.5s with Improved |
| Frost Splinters | Exactly 20% P blast damage, plus Slowness III for 4s |
| Frost Spikes | Requires an enemy within 24 blocks. Cast 0.2s; after a 0.25s warning, one native spike model erupts at the snapshotted location, at three times scale. Radius 2, damage 80% P, Frozen 1s. Moving away can dodge it |
| Improved Frost Spikes | Retains its +10% spell-power modifier. The 80%/100% coefficients apply to the resulting P, without another coefficient multiplier |
| Glacial Spikes | Instant cast and eruption, 100% P |
| Frost Fan | One explosion 1s after the spike at its fixed location: radius 3, 20% P and Frozen 0.5s. It does not create the old rows or one explosion per victim |
| Winter's Chill | Each Frost class active readies the next Frost staff/wand basic spell for 10s, with no cooldown. That basic consumes the charge at launch, even on a miss; one hit freezes for 0.5s. Basics do not rearm it; ricochets cannot reuse the charge |
| Near Zero, formerly Frostbite | Existing damaging-Frost trigger, 100% chance and 12s haste cooldown. The triggering target receives Frozen for 5s, including spells without a normal freeze. This is a proc, not a permanent duration override |
| Frost Shield | Existing shield and branches remain. Expiry burst now deals 30% P and freezes for 1s |
| Ice Lance | Full charge 0.75s; projectile speed 1.6-2.4, with gentle near-aim homing |
| Colossal Lance | Removes its critical bonus. Its direct Lance projectile ignores 30% of armor for that hit; toughness, resistance and retention still apply. It does not change the target's stored armor or grant penetration to Shattering splash |
| Arctic Reflex | Multiplies the empowered spell's damage by 1.2 exactly once, rather than adding percentage points |
| Blizzard | Damage continues normally. Each enemy can receive a 0.25s Frozen application once every 1.25s; the target's gate is shared across casters |

Frostbite exposure and Frozen control are distinct effects. Existing Frozen visuals/icons remain; new readiness/exposure entries have synchronized effect icons. Tooltips use the dedicated concise-description rules, keeping cooldown/trigger information in native fields.

## Reproduction and evidence

Inputs are pinned in [baseline.json](baseline.json); [artifacts.json](artifacts.json) records changed archive members and final hashes. No probe classes ship. Builders use only pinned current-input archives; isolated fixtures live under `tmp/class-balance-phases-4-6`.

```powershell
python docs/patches/class-balance-phases-4-6/check_resources.py --baseline
python docs/patches/class-balance-phases-4-6/build.py
python docs/patches/class-balance-phases-4-6/check_resources.py
python docs/patches/class-balance-phases-4-6/run_probe.py
python docs/patches/class-balance-phases-4-6/run_regression.py
python docs/patches/class-balance-phases-4-6/run_client.py
python docs/patches/class-balance-phases-4-6/verify.py --installed
```

The client runner archives its previous isolated fixture before copying a fresh test world. Personal worlds and the separate desktop server are untouched. This batch does not commit or push.
