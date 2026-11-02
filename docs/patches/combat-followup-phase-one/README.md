# Combat follow-up phase one: Warrior and Assassin

2 November 2026. Same-version overlays over the current six affected archives: Adventurer Skills, Skill Tree, Rogues & Warriors, Spell Engine, Better Combat and Combat Roll. Saved skill IDs, equipment, unrelated patches and active filenames are retained. Both clients and servers need matching replacements and a restart; no config deletion is required.

## Progression and Assassin

- Adventurer ordinary melee nodes: **2%**, down from 3%. RPG Warrior root/ordinary damage nodes: **5%**, up from 4%.
- Selecting the Rogue/Assassin path root grants a hidden modifier enabling full-speed melee attack movement and sprinting. Removing the path removes the exception. It does not negate unrelated control effects or sprint prerequisites.
- Mutilate requires an eligible enemy within **8 blocks**, casts in **0.25s**, and teleports the caster immediately in front of that enemy before its existing dual-wield strike. The landing requires support, clear space, line of sight and a valid world-border location; hostile Purgatory boundary clipping also rejects crossing. No valid landing means no remote strike. Its post-teleport melee hit retains ordinary melee reach rather than the eight-block cast range. Existing healing and branches remain.

## Warrior tier I

| Skill/node | Current behavior |
| --- | --- |
| Throw Weapon / Throw Net | Projectile speed **1.6**; existing 0.3s charge, damage and range curves retained. |
| Throw Weapon | Slowness IV for **4s**. Own-Weakness recast fix retained. |
| Strong Throw | At most two enemy hits; Slowness V for **4s**. |
| Head Throw | Existing 60–100% M charge damage; Slowness IV for **5s**, stun **0.5s**. |
| Multiple Nets | **4s** base cooldown and **2s** root. |
| Endured Vitality | Exactly **2 flat armor and 2 maximum HP per stack**, ten stacks, eight-second refresh and existing haste-affected 0.25s gain interval. No remaining melee-based armor scaling. Only newly added HP is healed. |
| Killing Spree | Retains 2% melee damage per stack, five stacks for 8s, and 4% maximum-HP kill healing. Qualifying attacks also heal **1 HP**, sharing its existing haste-affected **0.25s** interval across targets. |

## Warrior tier II

| Skill/node | Current behavior |
| --- | --- |
| Wild Charge | Existing movement, control and knockback benefits; **+4 armor** while active. Melee attacks neither slow movement nor cancel sprinting. |
| Endurance | **6 total armor** while Charge and Endurance are active; full knockback resistance retained. |
| Concussion Blow | Replaces next-hit readiness: enemies within **1.5 blocks** are stunned for **2s**, once per enemy per Charge cast. |
| Demoralizing Shout | **20%** lower melee, ranged and offensive magic power for affected enemies. Existing reveal, radius and control protection retained. |
| Battle Shout | **12%** higher melee, ranged and offensive magic power for caster/party allies, retaining 6s duration and matching radius. |
| Challenging Shout | **20% M damage**, existing aggro and brief stun. Reveals the caster and prevents affected enemies from combat rolling for the debuff duration. Anti-roll is a synchronized harmful effect with the Shout icon, checked on client and server. |
| Warm Up | Combat roll immediately heals **5% maximum HP**; **1s** haste-affected base cooldown. |
| Momentum | Roll prepares five melee hits within **8s**. Each grants **3% attack speed and movement speed**, up to **15% each**, for **5s**, refreshed. Base cooldown remains 12s. |

## Warrior tier III

| Skill/node | Current behavior |
| --- | --- |
| Berserker | Existing benefits and tradeoffs retained; melee attacks no longer slow movement or cancel sprinting. |
| Brutality | **6 critical-chance points and 6 critical-damage points per pulse**, up to **30 each**, before outside multipliers. Uses native critical attribute units. |
| Lethality | No self-damage cost. Qualifying melee impacts heal **1 HP + 1 HP per complete 10 M**. Existing **0.2s** haste-affected shared healing interval retained. |
| Last Stand | **2% damage resistance per pulse**, up to **10%**. Retains maximum-HP growth and healing of newly added HP. The fifth pulse fully heals. |
| Juggernaut | **4% total resistance per pulse**, up to **20%**, with existing size benefit. Implemented as Last Stand's 2% plus the additional 2% from this branch, never a 30% combined total. |
| Sentinel | **2% melee damage per stack**, ten stacks; Regeneration II refreshed for **5s**. Existing direct-hit and haste-affected 0.25s gain interval retained. |
| Enrage | Size **4% per HP threshold**, up to **24%**; melee bonus remains 3% per threshold, up to 18%. |
| Tenacity | **30s** haste-affected base cooldown; existing shield, stun and fourteen-second duration retained. |

Last Stand and Juggernaut use the existing general incoming-damage attribute, covering ordinary physical and magical damage without a second duplicate magic layer. True-damage policies remain separate. Their duration, channel timing, death cleanup and lack of full damage immunity remain.

## Integration rulings

- The tree calls the Rogue path root `rogue_root`; this is the installed Rogue/Assassin class selection, not a new higher-tier unlock. Both current category definitions receive the marker reward without changing skill IDs or allocation costs.
- Effects are overridden immediately before native registration: later historical registration helpers otherwise restore older Momentum and Demoralize values. Existing generated configs do not silently restore the old defaults.
- The obsolete periodic Momentum movement-modifier cleanup is disabled for that attribute; ordinary effect removal now owns cleanup.
- Melee movement changes bypass only Better Combat's attack-input slowdown and attack-scoped sprint cancellation. They do not add another movement multiplier or clear roots/Slowness globally.
- Mutilate's ordinary native melee delivery and dual-wield/node hooks are retained. The launch-to-impact delay is removed so it does not add another half-swing delay after the requested cast.
- Phase 2 wall/reach investigation and phase 3 archery changes are excluded.

## Build and evidence

Run `python docs/patches/combat-followup-phase-one/build.py`, `check_resources.py`, `run_probe.py` and `run_client.py` from the repository root. Builders use pinned current inputs under ignored `tmp/combat-followup-phase-one/before`; never use older phase-one snapshots as replacement baselines. [Archive identities](artifacts.json) record changed/added entries. Native server and connected integrated-client results are recorded alongside this file. This does not certify remote multiplayer reach or wall enforcement; those are phase 2.

## Installed verification

All six replacements are installed in repository `mods/` and ignored fixed packs. **70 native server checks**, **11 connected integrated-client checks**, nine initial resource checks and **4,161 unchanged-entry preservation checks** passed. Physical and magic test hits fall from 10 to 9 with full Last Stand, and to 8 with Juggernaut. Native Player attacks retain sprint; the actual Mutilate execution path rejects a cast on cooldown before teleporting and teleports successfully after validation. A review regression confirms nondamaging Shout cannot trigger Killing Spree's added heal.

The handbook remains 80 pages with 469 valid internal links and 51 correct printed contents destinations; changed pages 17–19 and the contents were visually reviewed. Matching root/output PDF copies pass the artifact check. No personal installation or separate desktop server was changed. No commit or push was requested.
