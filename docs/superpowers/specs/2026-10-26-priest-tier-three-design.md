# Priest tier III: mobile Lightwell and protective Barrier

26 October 2026. Design for review; no implementation is authorized by this document alone. Latest installed Priest tier-I/II rework and passive fixes are the baseline, not the original upstream resources.

## Intent and constraints

Give Priest a mobile healing support summon and an area defensive ultimate. Preserve current class/weapon tree placements, spell/node IDs, scroll membership, Better Party protections, latest absorption corrections and other classes' balance. Keep active filenames for same-version repacks. Personal installations/worlds remain untouched. No commit or push requested.

P means caster Healing Power. All listed cooldowns are base values before haste. All Priest casts and skill/passive cooldowns must enable native haste scaling; explicit exceptions such as Lightwell's current non-haste cooldown are removed. Buff lifetimes remain fixed. Lightwell's requested 0.8s shot interval is a base interval that retains its inherited-haste behavior; projectile speed is fixed.

## Approaches

1. **Extend the existing summon, projectile and barrier hooks (recommended).** Keep native ownership, lifetime, homing, cooldown and party infrastructure; add focused rules for hovering, recipient priority, mote upgrades, broader protection and projectile retaliation. Smallest compatible change and no new gameplay dependency.
2. Replace these skills with independent scripted entities and damage handlers. More control, but duplicates ownership, synchronization and party logic, making regression risk higher. Not recommended.

The user accepted the recommended protection policy and supplied all numerical values below. Reusing the existing hooks is the implementation recommendation, not an additional user requirement. This design awaits final review before the implementation plan.

## Lightwell

| Property | Requested behavior |
|---|---|
| Cast | Retain instant cast |
| Cooldown | 80s, haste affected |
| Active lifetime | 16s; Improved adds 4s, making 20s |
| Position | Floats above the caster's head and follows the caster |
| Physical properties | Cannot be attacked or pushed; no collision |
| Target range | 16 blocks from the hovering well |
| Power | Inherits 5% of caster Healing Power |
| Shot interval | 0.8s base, using native inherited haste |
| Holy Mote speed | 0.6 |
| Healing | 100% of the well's own Healing Power per successful mote hit |

Implementation proposals: use the existing owner reference, maintain an offset above the caster's scaled head, and synchronize movement through the native entity path. Despawn on owner death/unload or dimension departure rather than leaving an orphan across dimensions. Follow naturally through short movement; avoid making the caster collide with the summon. Preserve existing appearance/disappearance phases outside the requested active lifetime.

### Recipient selection

Reuse installed Better Party support eligibility for caster, party players and eligible owned/party pets. Choose an injured eligible ally before a full-health one. Within the injured group, prefer the greatest missing-health percentage, with distance as a tie-breaker; this ranking is an implementation proposal. Reevaluate after shots and invalidate dead, removed, out-of-range or no-longer-friendly targets. Do not let a stale full-health target monopolize shots.

Full-health allies may still be useful targets when an equipped upgrade can cleanse a harmful effect or add an unearned maximum-heart stack. They remain lower priority than injured allies. Without a useful heal or upgrade benefit, idle instead of firing at a full-health ally. Do not change existing line-of-sight/obstruction behavior merely to implement the hovering position.

### Lightwell nodes

- **Improved Lightwell:** +4s active lifetime only, for 20s.
- **Cleansing Light:** each successful mote hit removes one eligible harmful effect from that recipient. There is no separate cleanse spell cooldown limiting this upgrade. Preserve Trial Omen and existing protected-effect exclusions. No cleansing on launch, miss, obstruction or expired projectile.
- **Empowered Well:** each successful mote hit grants that ally +1 maximum heart, two stacks maximum, with a 6s duration refreshed by further hits. Per-recipient state; no shared party stack counter. Implementation proposal: multiple wells share that recipient's two-stack cap and refresh the same 6s buff, preventing extra Priests from multiplying the maximum-heart cap. No extra Healing Power bonus remains. The user explicitly chose that added maximum hearts arrive unfilled; ordinary mote healing fills them. Apply the maximum-health grant before that mote's heal so its healing can use the new capacity. Refreshing at cap neither adds maximum HP nor directly heals. Removal/expiry clears the exact temporary modifier and clamps current health to the new maximum without interfering with other maximum-health sources.

Keep the three existing saved upgrade IDs. Native branch exclusions remain as installed; do not silently make both alternatives selectable.

## Barrier

| Property | Requested behavior |
|---|---|
| Cast | 1s, haste affected |
| Cooldown | 60s, haste affected |
| Radius | 10 blocks |
| Lifetime | 10s; Improved adds 2s, making 12s |
| Position | Retain stationary placement |
| Hostile control | Repeated strong outward knockback, retaining the barrier's current cadence |
| Friendly protection | Prevent ordinary combat and damage-over-time damage while inside |

The user approved protection against ordinary damage sources, including poison, fire, Bleed and other damage-over-time effects. Preserve void damage, `/kill`/administrative kill and deliberate skill health costs. Do not universally cancel death events or convert direct health assignments into damage events. Any health-subtraction path found during the implementation audit must be classified; explicit skill costs stay unaffected. Document and test ordinary DoT sources actually covered instead of claiming every mod's arbitrary health manipulation is blocked.

Broaden native protection beyond the current player-attack/explosion/magic tag. Apply protection only inside the barrier's actual volume and to the owner and eligible party/support principals; do not protect enemies. Use existing volume/dimension behavior with the new radius unless testing reveals disagreement between visual dome and protection bounds. Prevent a lingering immunity grant from protecting someone who has already left the volume. Keep hostile knockback and party/pet exclusions consistent with installed party rules.

### Barrier nodes

- **Improved Barrier:** replaces the old cooldown deduction with +2s barrier duration. Base cooldown remains 60s; Improved lifetime is 12s.
- **Sacred Refuge:** unchanged: separate four-block healing/cleansing cloud lasting 10s, 10% P healing per 2s pulse, using the existing dispel exclusions. It is not enlarged to ten blocks or extended by Improved Barrier.
- **Deflective Shield:** replaces/renames Barrier Duration while retaining its saved node ID. An eligible hostile projectile intercepted by the barrier causes 25% P damage to its living attacker. This is retaliation, not a physical projectile reflection. Preserve current projectile blocking/consumption behavior. Attribute retaliation to the caster through the installed Priest holy/Healing-school spell damage path so native defenses, party protection and the pack PvP/PvE multiplier apply. It is not true damage.

Implementation proposals: one retaliation per projectile at its first interception; no repeated damage from lingering/multi-tick contact or overlapping barriers. Resolve projectile owners through the installed attribution path; ignore projectiles without an eligible living attacker and friendly/self/party projectiles. Retaliation must not itself create a new projectile or chain reflection. An attacker can receive separate retaliations from distinct projectiles as requested.

## Descriptions and documentation

Follow `mod fixing/RPG Skills - description rewrite.md` and its tracked canonical version: concise effect wording, `%`, meaningful benefits and durations; no literal patch history, unresolved impact tokens, rounding formulas or duplicated cooldown/haste prose. Keep native yellow trigger/cooldown panels authoritative. Synchronize tree titles and scroll spell names for Deflective Shield.

Update Priest raw attributes/exporter, patch notes and player handbook; rebuild and visually inspect changed PDF pages. Do not rewrite unrelated classes or pending changes.

## Verification requirements

Use isolated current-pack fixtures. Check exact coefficients/cooldowns/ranges and preservation of all unrelated resources.

- Haste reduces Priest casts/cooldowns and Lightwell's base action interval without shortening buff lifetimes.
- Well follows a moving caster above the head, cannot be attacked/pushed, does not collide, expires on time and cleans up after owner departure/death.
- Motes use 0.6 velocity and 100% own-power healing with 5% caster inheritance; injured ally priority and stale target replacement work with party players and protected pets.
- Cleanse removes one eligible debuff on impact without a second hidden cooldown; no cleanse on misses.
- Maximum-heart stacks are independent per ally, cap at two, expire after 6s, arrive unfilled and refresh without extra HP or healing; other health modifiers survive cleanup.
- Barrier excludes hostile recipients, stops actual ordinary melee/projectile/magic/poison/fire/Bleed damage, respects its volume, and permits void/admin kills and intentional skill costs.
- Each qualifying intercepted projectile retaliates exactly once for 25% P before defenses/pack scaling; no ally/pet retaliation or overlapping-barrier duplication.
- Improved lasts 12s without the old cooldown reduction. Sacred Refuge retains its independent 4-block/10s behavior.
- Connected-client checks cover hovering, visible barrier dimensions, projectile interception and concise names/tooltips; compilation alone is not visual proof.

## Approval state

Confirmed: mote speed 0.6; maximum hearts filled by normal healing; Barrier ordinary-damage protection with void/admin/skill-cost exceptions. Remaining step: review and approve this concrete design, including the explicitly labeled implementation proposals, then write the implementation plan.

## Builder review

A read-only builder review found no blocking issues. Its two implementation details are resolved above: the maximum-heart cap is shared per recipient across wells, and retaliation uses the native Priest holy/Healing-school damage path. No code or JAR changes were made for this design review.
