# Class visual and effect retouches

28 October 2026. Proposed presentation pass, following the user's approval of the Paladin visual direction. These are design recommendations, not installed changes or new balance values. The Paladin tier-III gameplay design remains separately reviewable in [its draft](2026-10-28-paladin-tier-three-design.md).

**Current scheduling:** Paladin is installed with its separate verification. The approved remaining-class presentation pass is now installed: three repaired sprites,95 retouched native resources, earned-gain/readiness/area/recovery cues and the requested Vanish mark cleanse. [Implementation and precise coverage](../../patches/class-visual-retouch/README.md) distinguish shipped changes from the broader design rows below. Native models/animations/counters were retained. The crowded networked-party and exhaustive lifecycle acceptance matrix remains unverified.

## 29 October2026 audit and selected approach

Use existing RPG Series artwork, models, animation and sound resources, repairing missing status assets before adding cosmetic cues. This follows the previously accepted Paladin direction and the user's preference for reusing the skill icon when a status icon is absent. A bespoke icon set or a new visual framework adds cost without fixing the diagnosed asset paths; neither is planned.

A native registry export with current matching archive hashes contains109 class/control effects. The initial pinned-input asset scan found **three missing conventional effect PNGs**, **zero empty existing PNGs** and **zero missing English effect names**. No active JAR contains an effect-atlas override. [Audit and reproduction](../../patches/class-visual-audit/README.md) distinguish these confirmed asset gaps from future client-rendering checks.

| Missing effect | Existing artwork to reuse | Meaning to preserve |
|---|---|---|
| Improved Stealth |Vanish skill |Vanish upgrade; never an enemy-visible stealth trail |
| Tracking Disruption |Shadowstep skill |Temporary AI target loss; not a root or stun |
| Cheat Death Evasion |Cheat Death effect |Evasion after the initial protection; not continued invulnerability |

Do not treat deliberately hidden internal effects as missing icons. Inspect effect application flags, atlas resolution, localization, resource overrides and native inventory/HUD rendering. The109-effect scan includes Paladin as a shared-code regression baseline; its installed visual pass is not being redesigned.

Execution order: icon repair, Rogue, Archer, Warrior, Priest, Arcane, Fire and Frost. Follow [the new implementation plan](../plans/2026-10-29-other-class-visuals-and-effect-icons.md), inline without implementation workers. Class timing/damage/eligibility remains unchanged. The previously requested Vanish mark cleanse is a separately named gameplay item; decorative work must not implement it accidentally.

## Shared presentation rules

- Show an actual action, successful impact, active state or earned proc. An animation must not imply damage, healing, cleansing or immunity that did not occur.
- Range rings use the current server-resolved area, including upgrades and moving centers. Separate targeting/search/pull range from actual damage range. Do not display a search radius as a damage field.
- Reuse current RPG Series models, sounds, particles and animations first. Particle geometry, tint and timing can distinguish upgrades without requiring new raster artwork.
- Preserve native combat animation timing. Impacts align with the server hit frame/delivery, never the first frame of an attack animation.
- Keep the shared official Stun/Root/Frozen icons. Different classes can have different application flashes, but the final control state stays recognizable.
- Status details show received percentages, remaining charges and active phases. Ready effects visibly consume; stack increases briefly acknowledge once rather than flashing every refresh.
- Absorption appears as a shield gain only when new HP is granted. Refreshing its timer or capacity at the existing cap must not look like restored shield HP.
- Prioritize caster/self feedback, readable enemy telegraphs and party support. Distant repetitions and overlapping sound/particle emissions can be reduced without hiding critical warnings.
- Shape, motion, icons and text carry meaning alongside color. Avoid mandatory camera shake, full-screen flashes, opaque fields and repeated floating damage-style numbers for ordinary buff refreshes.
- Remove loops, temporary glints, ribbons and poses on expiry, dispel, death, interruption or world transition. Never fabricate a successful rescue, heal or cooldown reset.
- These changes do not alter skill damage, radius, counts, targeting, timing, cooldowns, eligibility or gameplay visibility. Hidden traps and stealth remain hidden according to their current rules.

## Archer — precise amber shots and green nature effects

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Power Shot / Entangling Roots | Prepared arrows gain restrained amber string/arrow-tip accents; remaining prepared charges are readable. Hunter's Mark gets an application/refresh cue matching its actual state, without inventing stacks. Roots has a low vine boundary matching its real cloud; only Nature's Grasp's actual immobilization uses the full Root treatment. Nettle poison uses sparse green hit flecks. Charged Shot's blast appears on impact at its actual splash radius. |
| II | Barrage / Spirit Wolf Pack | Every real arrow release gets a separate string snap/recoil cue without replacing native bow/crossbow poses. Blood Barrage shows a small recovery return only when healing occurs. Wolves assemble from green-white wisps, have a subtle owner/party identification cue and visibly dissolve at expiry; Leader's extra wolf remains individually readable. |
| III | Rain of Arrows / Magic Arrows | Rain has a restrained overhead source and moving ground boundary tied to its real current center; every actual scattered impact has its own small splash. Torrential communicates denser cadence; Endless communicates a longer remaining volley without inventing arrows. Magic Arrows have four separate trails; Conjured's upward fan has a clear free-flight interval before homing begins. Reveal appears on a successful hit, not acquisition. Magic Punch uses a heavier impact. |

| Passive pair | Presentation retouches |
|---|---|
| Concussive Shot / Rhythm | Brief impact ring plus official Stun icon on a successful concussion; Rhythm's self-only pulse grows with its real stack count, avoiding one continuous loud loop. |
| Momentum / Tactical Maneuver | A small roll-to-bow acknowledgement for gained Rhythm stacks; a distinct recharge streak for Tactical Maneuver. Actual remaining duration and stack/recharge values stay in effect details. |
| Supercharge / Deflection | One clearly different ready-arrow accent, removed when consumed or expired. Deflection shows its actual remaining three/two/one charges, with one shield fleck per genuinely blocked hit. Do not render a permanent barrier implying continuous immunity. |

## Rogue — brief smoke/violet movement, green poison and restrained red bleed

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Shock Powder / Slice & Dice | Powder has a short throw/puff and an application flash; Explosive Powder's damage burst is distinct from the stun. Smoke Screen has a readable edge and sparse interior. Slice & Dice gives brief edge glints and distinct empowered-hit ticks; extra-hit upgrades change the real charge counter rather than endlessly increasing particle density. |
| II | Shadowstep / Bear Trap | Short departure/arrival smoke, with arrival feedback after a successful teleport. Cloak has a brief protection shell; Ambush has a sharp impact and debuff acknowledgement. Trap visuals preserve the current visible arming, hidden armed state and activation reveal. No enemy-visible trigger ring, glow or persistent smoke betrays an armed hidden trap. Owner-only placement preview is conditional on existing visibility policy, not added detection. |
| III | Vanish / Mutilate | Vanish has a rapidly fading cleanse shimmer when existing marks are removed, then no enemy-visible trail, armor shimmer or hovering effect that exposes stealth. Any sustained self feedback is first-person/UI only. Mutilate uses one coherent dual-blade sweep aligned to its actual hit frame; Crimson recovery happens once per successful cast, and Bloody Strikes adds sparse bleed flecks. |

| Passive pair | Presentation retouches |
|---|---|
| Coated Blades / Fracture | Poison has a green application cue; Fracture has a brief cracked-armor impact. Show actual armor lost and stacks; do not imply all incoming damage is increased. |
| Sidestep / Opportunist | Sidestep gets a short evasive self cue. Opportunist has a one-hit readiness accent which disappears on its qualifying melee hit; no visual claim of extra critical damage. |
| Cheat Death / Prepared Counter | Cheat Death has one unmistakable escape flash and a separate expiring protection cue. Prepared Counter has a restrained self-only ready blade accent and one consumption flash on the eligible basic melee hit. Neither reveal exposes a stealthed Rogue. |

## Warrior — iron/earth weight, crimson aggression and clear defensive states

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Throw Weapon / Throw Net | Charge feedback distinguishes minimum/full power. Throw Weapon's trail follows the real weapon; Strong Throw adds a small retarget bend and acknowledges no more than its actual two victims. Head Throw uses a short head-impact flash plus official stun. Net has visible strands and a continuous pull motion; Bigger Net expands the real hit footprint, not the traveling projectile's implied damage. |
| II | Wild Charge / Demoralizing Shout | Charge gains directional boot/ground streaks and one cleanse burst, with a separate protection expiry. Shout uses an expanding sound ring matching its radius. Enemy power reduction and ally Battle Shout buffs use different impact shapes; Challenging Shout's taunt/stun is distinct. Concussion Blow has one readied weapon accent and one actual hit consumption. |
| III | Berserker / Last Stand | Preserve the sword-ground stance and make each completed channel pulse visible. Berserker builds crimson sparks with its real levels; clearly distinguish active rage, normal ending and fatal-hit rescue. Last Stand uses steel/gold protective bands and the existing stronger glint; health gains accompany actual new HP, without suggesting damage immunity. Juggernaut's scale remains gameplay-driven; Sentinel adds only brief stack-gain feedback. |

| Passive pair | Presentation retouches |
|---|---|
| Killing Spree / Endured Vitality | A small aggression accent for earned damage stacks; recovery feedback only on an actual credited kill. Vitality gets a subtle armor/health acknowledgement only for newly granted stats, with no healing flourish on a cap refresh. |
| Warm Up / Momentum | One small roll-triggered recovery cue for Warm Up. Momentum distinguishes prepared hits from earned speed stacks and communicates the remaining real hit allowance. |
| Enrage / Tenacity | Enrage changes a subtle self intensity only on threshold transitions, alongside existing actual size changes. Tenacity has a distinct shield-onset flash and nearby stun pulse; shield refresh/expiry follows actual absorption instead of an apparent refill. |

## Priest — ivory light, restrained gold and readable support destinations

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Holy Light / Penance | Holy Light's beam has restrained delivery pulses and different successful heal/harm impacts. Graceful/searing branches strengthen the relevant impact, not every beam frame. Penance's ten bolts remain individually visible; each blast has the true boundary and different ally-shield/enemy-harm cues. Caster support gained outside the blast gets a small self/UI acknowledgement, without drawing a false link from a distant explosion. |
| II | Circle of Healing / Ascension | Circle's expanding ring shows its real radius. Cleanses acknowledge actually removed effects; Sanctuary's regeneration has a quiet continuing cue. Ascension visually builds four pulses and clearly signals its stronger final push. Uplift shows only actual lifted allies; Serenity shows newly granted shield HP without pretending to refill spent absorption. |
| III | Lightwell / Barrier | The well's two motes have distinguishable paths and target impacts. Preserve the0.3s same-target launch offset; avoid extra artificial delays for different recipients. Barrier has a readable boundary with a transparent interior and entry/exit feedback; protection remains tied to the actual volume. Sacred Refuge has its own smaller healing boundary. Deflective Shield shows one interception/retaliation cue per real intercepted projectile. |

| Passive pair | Presentation retouches |
|---|---|
| Healing Focus / Incanters' Cadence | Gentle self acknowledgement on a gained stack and a clear maximum-stack icon, without four flashes per second on checks/refreshes. Received healing/absorption and haste values remain explicit. |
| Fade / Divine Favor | Fade's departure cue ends immediately; neither equipment nor continuous particle ribbons betray invisibility. Divine Favor has a self-only ready halo and a distinct counter pulse only when consumed, with official Stun indicators on actual victims. |
| Pain Suppression / Celestial Orbs | Pain Suppression changes intensity only when the protection band changes. Orbs visibly correspond to the actual remaining count and fire once when a valid caster/nearby-ally hit consumes a charge. Avoid multiplying cosmetic orbs on refresh. |

## Arcane — violet/cyan geometry and clean, discrete energy states

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Arcane Missiles / Arcane Explosion | Distinct missile heads/trails and an impact acknowledgement per real delivery. Conjured extras remain visibly separate. Explosion has a short contraction/flash/expansion at its actual target-centered area; delayed secondary detonations use a smaller, distinct pulse. |
| II | Arcane Beam / Arcane Barrage | Beam width follows its true growing geometry, with a restrained edge so targets remain visible. Exposure has readable stack updates. Emitters have individual firing points and visible bolt cadence; extra/faster emitter upgrades change the actual events rather than a generic visual spray. |
| III | Burst Blink / Evocation | Blink marks its real departure and arrival bursts, with the existing delayed arrival blast shown at the fixed arrival point. Evocation builds geometric rings with earned stacks, while its increased-damage-taken risk has a distinct status cue. Do not imply protection from a shield-shaped visual. |

| Passive pair | Presentation retouches |
|---|---|
| Fissile Magic / Arcane Radiance | Small secondary blast on the actual eligible proc; a modest recovery cue when healing occurs. |
| Arcane Trap / Phase Shift | Trap uses its existing visibility and activation policy. Phase Shift's brief distortion exactly follows its short immunity, without a lingering protective shell. |
| Presence of Mind / Arcane Ward | A single next-spell readiness rune, visibly consumed by the chosen spell; target vulnerability appears on actual impacts. Ward has a thin faceted shell and accurate remaining absorption, without growing stronger on timer refresh. |

## Fire — directional flames, ember buildup and clear persistent boundaries

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Fire Breath / Flame Slash | Breath visibly follows the real cone and mouth/hand origin while leaving the center readable. Slash is a short arc aligned to impact. Branch effects distinguish actual extended burn, push or added damage rather than increasing opaque fire everywhere. |
| II | Meteor / Firestorm | Meteor gives a brief descent/landing telegraph at its actual impact area, then a sharp impact. Firestorm has a restrained perimeter and individually readable pulses. Persistent fire is visually separate from the initial hit so players can judge whether an area still hurts. |
| III | Wall of Flames / Fire Hydra | Wall shows its actual placement, lifetime and remaining cast allowance; Great Wall's solid behavior gets a clearer boundary without implying projectile blocking. Hydra breath originates at the mouth and follows real aim; enemy selection changes should not make the model jerk or duplicate flame cones. |

| Passive pair | Presentation retouches |
|---|---|
| Scorching Flames / Hot Impact | A sparse target ember mark for real vulnerability stacks; a brief directional impact acknowledgement for actual Hot Impact procs. |
| Flame Trap / Blazing Speed | Keep trap concealment policy; use a distinct activation burst. Speed has brief foot embers which stop at expiry and never leave damaging-looking ground fire. |
| Eruption / Flame Ward | Eruption has a short recognizable proc ring. Flame Ward uses a translucent ember shell whose appearance means absorption, not fire immunity. Show newly gained shield capacity separately from duration refresh. |

## Frost — readable ice edges, sharp impacts and distinct chill versus freeze

| Tier | Active pair | Presentation retouches |
|---|---|---|
| I | Frost Nova / Frost Spikes | Nova has an expanding crystalline ring at its real area. Spikes visibly emerge along the actual damage geometry. Slow/chill uses a light frost rim; only actual Frozen uses the official icon and full frozen treatment. |
| II | Frost Shield / Ice Lance | Shield distinctly communicates the real immunity period and its ending freeze burst, removing its protected appearance promptly on expiry. Lance uses a narrow bright core and restrained fragments; branch splash/extra impacts match real events. |
| III | Blizzard / Frost Elemental | Blizzard has a visible perimeter and lower-opacity interior so combat remains legible. Elementals assemble and dissolve visibly; Legion's three bodies and Colossus's real scale are distinct. Super Nova has a brief readable expansion matching each summon's actual blast radius, followed by actual freeze cues. |

| Passive pair | Presentation retouches |
|---|---|
| Winter's Chill / Frostbite | Different target cues for actual critical-vulnerability stacks and the separate Frostbite effect; do not present every cold debuff as immobilization. |
| Frost Trap / Arctic Reflex | Keep native trap visibility. Arctic Reflex has a single next-Frost-spell readiness crystal and a consumption flash, without implying instant casting. |
| Cold Snap / Frost Ward | Cold Snap acknowledges the actual cooldown recovery; excluded spells must not look reset. Frost Ward uses a translucent segmented shell for absorption, distinct from Frost Shield's immunity and from full Frozen control. |

## Implementation order after approval

1. Fix misleading or stale presentation first: charge counts, immunity versus absorption, concealed states, exact impact timing, effect cleanup and true range boundaries.
2. Apply restrained ready/consume/stack-gain cues and correct native sound timing.
3. Add larger summon/ultimate transitions and branch-specific accents.
4. Verify in isolated native client/server sessions, including a crowded party fight and reduced-particle settings. Compare real events with counts, rings and durations; capture representative skill and passive states.

Keep this pass distinct from balancing. Proposed effects do not authorize changing gameplay stealth/trap visibility, ranges, damage or cooldowns. Player documentation and the handbook should describe these presentations only once implemented and verified.
