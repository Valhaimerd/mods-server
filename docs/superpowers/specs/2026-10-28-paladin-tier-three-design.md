# Paladin tier III and Vanish — design draft

Status: GO received29 October2026; Paladin gameplay/visuals and cat replacement are implemented and installed. See [final evidence](../../patches/paladin-tier-three/README.md). Current batch is Paladin gameplay plus Paladin visual retouch and the separately verified Cute Cats to Enhanced Cats replacement. Other classes are deferred, including Rogue Vanish's mark-clearing implementation. No commit or push requested. Implementation stays inline. One focused builder review completed; its timing and overlap findings are resolved below.

Execution checklist: [Paladin tierIII and visual retouch plan](../plans/2026-10-28-paladin-tier-three-and-visual-retouch.md). The original Vanish requirements remain in this document as deferred intent, not current execution scope.

## Intent and existing implementation

Battle Banner becomes a moving party-support ultimate. Immolation becomes Purgatory, a stationary channel whose damage, pull and personal risk grow over 30 seconds. Ardent Defender rescues one endangered party player; Reinforced trades that intervention for personal low-health scaling. Vanish clears existing marks.

At the pre-rework baseline, Banner was a placed cloud with haste-scaled pulses and Immolation was a short charged area release. These need lifecycle changes, not just resource-number edits. Existing native cast/effect/impact handlers already supply party classification, damage routing, saved IDs, animation and cleanup. Extending those handlers is the proposed approach. A resources-only change cannot implement following, per-ally rescue charges, stage costs or release/death explosions. A separate combat framework would duplicate the existing integrations.

Keep current internal spell/node IDs and mutually exclusive skill/passive branches. Rename visible skills, tree nodes, scrolls, descriptions and effects together. Remove old healing/hammer/Consecration triggers from the replaced skills. Preserve all unrelated pack changes, existing 80% PvE /35% PvP routing and the +25% holy-skill damage against undead.

## Decisions already confirmed

- Purgatory pays its caster HP cost **every pulse**, not once per stage.
- Ardent Defender rescues **only the triggering player**, with one shared cooldown for the owning Paladin.
- Final-phase Purgatory invulnerability is capped at **0.7s** per pulse. Continuous ordinary-damage protection is allowed; HP self-cost remains unpreventable.
- Ardent Defender grants benefits **without cancelling a fatal hit**. It is not a guaranteed rescue.

## Proposed defaults for approval

Banner pulses every0.5s; percentage scaling is proportional while flat HP steps use complete stat thresholds. Retain75% P /25% M hybrid weighting and Ardent Defender's10s defensive duration. Haste affects cooldowns but not the fixed Purgatory timeline. Damnation grows70 percentage points per final-phase second to reach900 at30s. Ordinary release before20s has no explosion.

These fill gaps in the request and are builder proposals for review, not claims that the user specified them. The lifecycle rules below are also part of this proposed design.

## Battle Banner

- Instant activation; 100s base cooldown. Aura follows the caster, with a 6-block radius and 20s lifetime.
- Improved: radius8. Persistent Banner: lifetime25s. Protective Banner is renamed Intense Banner.
- Fixed 0.5s pulses are proposed, retaining the old base interval. Haste affects cooldown, never pulse rate or aura lifetime.
- Each pulse increases each recipient's accumulated bonuses and refreshes their3s duration. Caps apply to the **total accumulated bonus**, not each stack. Leaving stops further pulses; existing bonuses expire normally. Re-entry before expiry continues the existing amount.
- Snapshot caster M, P and armor at activation; do not recompute from recipients. This prevents stat feedback and makes received-effect tooltips reliable.

| Bonus | Gain per pulse | Total cap | Intense cap |
|---|---|---|---|
| Attack speed |0.5% +0.1% per10 M |20% |25% |
| Draw speed |1% +0.1% per10 M |20% |25% |
| Spell haste |0.25% +0.1% per5 P |10% |15% |
| General damage reduction |0.25% +0.1% per5 caster armor |10% |15% |
| Additional magic damage reduction |0.25% +0.1% per5 caster armor |10% |15% |
| Knockback resistance |1% +0.5% per10 M |30% |35% |
| Healing |1 HP +1 HP per complete10 P |Immediate healing, no stack/cap |Unchanged |

Proposed arithmetic: percentage scaling is proportional, allowing fractional power; only the explicitly flat HP steps use complete10 P. General and additional magic reduction multiply, so10% general plus10% magic leaves81% incoming magic damage before other independent defenses. Normal healing-received modifiers still apply to healing.

Preserve the existing eligible-party/owned-pet support policy unless changed. Caster is included. Each unique supported ally may receive **one2s invulnerability rescue per Banner cast** when below30% current health or facing a lethal hit. Eligibility requires being inside the current aura. Track spent UUIDs for the whole cast, including departures/re-entry; refreshing buffs never restores a rescue charge. Each actual new Banner cast starts its own allowance. Exclude void/admin kills and Purgatory's unpreventable HP cost from ordinary invulnerability.

Multiple Banners must not add uncapped copies of the same bonuses. Track amounts, caps and expiry separately per cast/recipient/stat, then expose the strongest unexpired amount. Weaker pulses cannot extend a stronger cast's expiry or preserve its Intense cap. Keep rescue allowances separate per cast.

When an aura ends, no more healing, stacks or rescues occur. Other recipients retain only the remainder of their last3s buff. Death clears the dying entity's own bonuses. Logout, respec and dimension change stop the aura as well; they cannot leave a permanent source behind.

## Purgatory

- Visible rename of Immolation. Up to30s channel; base cooldown2s, starting when the channel ends.
- First delivery at0.5s, then every0.5s:60 maximum deliveries. Keep the stage clock, pulse interval and maximum duration in real time; haste may reduce cooldown but cannot accelerate the stages.
- Caster cannot move. Use Last Stand's sword-ground stance, fire/spark particles, orange weapon glint and the old Immolation area presentation. Clear the pose/glint on every exit path.
- Pulse damage/burning radius6; taunt/pull radius12. Improved increases these to7/14 and the final explosion/shield radius to14 from12.
- Retain current holy hybrid weighting:75% P /25% M. A10% hybrid pulse therefore deals7.5% P +2.5% M; do not silently change this to10% of each school.
- Harmful effects exclude caster, party players and protected pets. Taunt affects eligible AI mobs; pulling affects eligible living enemies and uses normal collision. No teleporting through walls.

| Elapsed channel | Pulse damage | Caster HP cost | Ally regeneration | Pull | Caster protection |
|---|---|---|---|---|---|
| Before10s |10% hybrid |None |None |Small |None |
| From10s |20% hybrid |2 HP each pulse |Regeneration I,4s refreshed |Moderate |None |
| From20s |30% hybrid |3 HP each pulse |Regeneration III,4s refreshed |Strong |0.2s +0.1s per complete10 P each pulse, capped0.7s |

Each delivery reserves its sequence number, checks interruption/death, applies enemy damage/burn/taunt/pull, grants eligible ally regeneration and final-stage protection, then pays its HP cost. Check Final Resolute before a fatal cost and after a nonfatal cost crosses below5%. Process the final delivery/cost before normal completion at30s.

Threshold changes apply on the delivery at10.0/20.0s. With that boundary rule, a full channel pays40 HP during the middle stage and63 HP during the final stage,103 HP total before outside healing. These are direct HP costs: armor, absorption, reduction and ordinary invulnerability do not erase them. The final phase does not cost2+3 HP;3 replaces2. Regeneration excludes the caster and refreshes rather than stacking its amplifier.

Proposed pull tuning for runtime evaluation: small/moderate/strong acceleration with a bounded final speed, respecting collision and existing knockback resistance. Verify visibly against walking and airborne targets before fixing numeric strengths. Taunt refreshes toward the caster every pulse while in range.

### Ending the channel

- Releasing before20s ends without an explosion. This prevents2s-cooldown tap explosions.
- Voluntary release during20–30s explodes for200–700% M, growing50 percentage points per elapsed second after20s. Proposed smooth interpolation also handles half-second stops.
- Reaching the final delivery or actually dying while channeling after20s causes the full700% M explosion, a1s stun, and the ally absorption grant.
- A partial ordinary release gives no completion-only stun or shield. The Final Resolute exception below grants the shield.
- Base explosion/shield radius12; Improved14. The caster is excluded from the shield.
- Shield:10 HP +1 HP per complete2 P, capped20 HP, lasting8s. Respect received-absorption bonuses without refilling spent shield HP on refresh.
- Use one burst per cast, including simultaneous fatal damage/final pulse/cancellation. Reserve the cast's end state before dealing damage so retaliatory effects cannot recursively explode it.
- Proposed CC behavior follows the current Warrior ultimates: stun/freeze interrupts; root does not. After20s, an interruption produces only the earned partial release, not a full completion reward. Death keeps the explicit full-burst rule.
- Logout, dimension change, respec and administrative removal do cleanup only, without a free explosion. Immobilization blocks voluntary walking/jumping/rolling, but adds no unrequested knockback immunity. Ordinary knockback can displace the caster; the pulse remains centered on them. Teleports end the channel safely.
- Reuse suitable RPG Series impact/sound resources for a larger fiery/golden blast, with a readable ring matching its actual radius. Avoid a new image-generation dependency unless existing art cannot convey it.

### Upgrade branches

- Damnation replaces Condemn. Pulse damage becomes15% /25% /35% hybrid. Explosion becomes200–900% M. Proposed interpolation is+70 percentage points per second during20–30s so it reaches900 at30s; death/completion use900.
- Final Resolute replaces Consecration. During20–30s, falling below5% current HP or facing a fatal hit stops the channel, prevents that fatal outcome, deals the explosion earned at that time, grants the normal8s ally shield, and gives the caster3s invulnerability. This includes a lethal Purgatory HP cost: stop that cost/channel rather than making future costs blockable. Check already-low HP before the next delivery and newly-low HP after a nonfatal cost. Do not add a heal or force a full700% blast.
- Fatal-event order: Final Resolute first; otherwise Banner's available rescue for eligible ordinary damage; otherwise Ardent Defender grants benefits without cancelling damage. Stop processing fatal rescues once an earlier effect prevents the hit. Ardent's heal may improve survival, but a sufficiently large hit still kills; do not rewind completed mitigation stages.
- Nonfatal low-health order: Final Resolute's below5% exit, then Ardent's qualifying heal, then Banner only if the target remains below30%. Purgatory self-cost may trigger Final Resolute or Ardent, but ordinary invulnerability cannot erase it; do not waste a Banner charge on that cost.

## Passive pair

### Ardent Defender

- Watches caster and eligible party **players only**, not pets, within6 blocks.
- Taking damage while above1% and below30% current HP, or receiving potentially fatal damage, qualifies the injured player.
- Only that player receives Resistance I, +10 armor, +5% general damage reduction, and healing equal to40% of their maximum HP.
- Owning Paladin has one90s base cooldown, haste-affected; it is shared across all possible recipients. Multiple recipients cannot consume the same readiness in one tick.
- Retain the previous10s defensive-buff duration as a proposed unchanged default. Healing is immediate and uses normal received-healing rules.
- Resistance I and the separate5% reduction multiply:24% combined reduction before armor/other layers. These are ordinary defensive buffs, not hit cancellation. Fatal-trigger benefits apply before HP loss, without rewinding completed mitigation stages or guaranteeing survival.

### Reinforced

- Replaces Divine Hammer; no hammer projectile or old5s proc remains.
- Below70% /60% /50% /40% current HP, gain5% /10% /15% /20% base Healing Power and base armor.
- Interpret “below40 HP” as40% of current maximum HP, consistent with the listed percentage thresholds.
- Proposed check interval0.25s, no cooldown, self only. Adjust down or remove bonuses as healing crosses thresholds; no permanent or stale stacks on death/respec.

## Vanish and received descriptions

Vanish clears active reveal/mark states on a successful cast, including Glowing, Archer's Hunter's Mark and Sinful Retaliation's mark. Audit other actual marking sources rather than matching arbitrary names. This is a one-time cleanse, not immunity to future marks. Preserve Vanish's existing duration, movement penalty and break rules; do not strip unrelated damage debuffs merely because they accompany a reveal.

Use brief result-first node text, `%` signs and the native cooldown/trigger panel. Status tooltips show actual accumulated bonuses, current phase, received reduction and remaining rescue availability. Do not expose floor operations, placeholders or source formulas to players. Raw references retain exact arithmetic.

The required writing reference is [RPG Skills - description rewrite.md](../../../mod%20fixing/RPG%20Skills%20-%20description%20rewrite.md). Apply its brief effect text with selective numbers to Paladin under the user's explicit follow-up, despite the historical exclusion of Paladin from that document's original key list. Most entries are one sentence, occasionally two; preserve essential risks and exceptions. Upgrade text explains the added benefit. Use current rework values rather than copying older examples, and let the native panel display cooldowns, triggers, cast times and ranges wherever it already does.

## Verification and distribution scope

Before editing, snapshot current active JARs and preserve prior uncommitted changes. Test fixed pulse timing at different haste levels, all stage boundaries, self-cost bypass, partial/full/death/rescue bursts, exact-once explosion and rescue, party/pet rules, overlapping Banners, shield anti-refill, mark cleanup, low-HP buff downgrades and death/logout/respec cleanup.

Use isolated server/client instances. Review actual moving aura, channel stance/glint, taunt/pull, blast radius, scroll/tree renames and received-value tooltips. Keep active filenames, archive backups locally, synchronize class references/player handbook and rebuild/review the PDF. Do not change personal installations or push to GitHub without a request.

## Review record

One focused read-only builder review examined pulse ordering, channel exits, source-separated Banner expiry and competing defensive effects. Routine defaults are now explicit above. No gameplay code, JARs or installed settings changed during brainstorming. After design approval, write the detailed implementation/test plan.

## Accepted visual direction and class-wide follow-up

The user liked the proposed Paladin finishing touches: shoulder-carried Banner and moving pulse boundary; distinct Banner rescue flash/chime/readiness; gold-to-orange-to-deep-red Purgatory stages; separate damage and pull boundaries; an earned-strength expanding blast; distinct Final Resolute and Ardent Defender feedback; a quiet Reinforced status; and a brief Vanish mark-cleanse shimmer. They requested expanding the presentation review to the other classes. The class-wide proposal is recorded in [Class visual and effect retouches](2026-10-28-class-visual-retouches-design.md). This visual-direction acceptance does not silently approve unresolved gameplay defaults in this draft.
