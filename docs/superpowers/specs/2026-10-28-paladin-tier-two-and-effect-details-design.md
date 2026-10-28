# Paladin revamp and received-effect details

Approved scope from the user's28 October2026 rework and follow-up. User explicitly chose knockback resistance10% +1% M capped80%. Execute inline, no workers, no push. All current uncommitted work is the starting point; preserve unrelated edits.

## Common rules

P=Healing Power, M=physical-melee power. Snapshot noncritical caster power/armor for support values, excluding a previous copy of the same support buff; avoid recursive self-scaling. Existing skill IDs, node allocations, scroll bindings and exclusivity persist; English names and descriptions follow new behavior. All native cooldowns/casts use haste, except the explicitly requested extra-damage limit of one proc per recipient per0.5s remains a hard floor. Fixed durations and counts do not shrink with haste. Party allies and protected pets remain safe. Final player damage routing80% mobs/35% players remains.

All direct damaging Paladin/Priest skill/passive impacts gain25% against #minecraft:undead mobs. Replace old undead-specific power/critical bonuses to achieve the uniform rule; do not modify healing or ordinary basic attacks. Native fire immunity/burning rules remain.

## Tier I

Purge Heal stays50% M, Improved60% M, Absolution80% M. Successful damaging impacts stun0.25s, or0.5s with Absolution; splash applies only once per enemy, self-heal once per cast. Existing healing, range, cooldown and party rules remain. Blessed waves apply Slowness I for8s; Wrath applies Slowness II for8s, preserving20-block range and one/two-total enemy limits.

Vanguard: shield equipment grants5% Healing Power; successful damaging hits grant5% Healing Power per stack,4 stacks,5s refreshed,1s haste-affected cooldown. Guardian: empty offhand grants5% base armor; incoming hits grant5% base armor per stack,4 stacks,5s refreshed,0.25s haste-affected cooldown. Equipment conditions govern the basic5%, independently of earned stacks. Preserve capability-based shield detection. Incoming hit qualification means actual combat damage to HP or absorption, with an attacker; no environmental or periodic free stacks.

## Tier II

Divine Protection: instant,28s cooldown,8-block caster-centered party application,8s duration. Damage reduction min(50%,5% +2%*P), flat mitigation1+floor(M/10) after normal defenses/percentage reduction and before absorption, never negative. Caster additionally gains10% Healing Power for the duration. Snapshot protection before granting this bonus. Stronger Paladin protection wins instead of adding to Protective Banner. Native original full-hit protection registration must be removed for this effect alone.

Improved: duration10s; cleanses stun/Frozen/root/Slowness and related movement-impairing effects, grants0.25s control protection. Allows this skill to be cast while controlled only with the Improved node; native cooldown/cost checks stay.

Bulwark of Faith replaces Pursuit of Justice: reduction min(50%,5% +5%*P), knockback resistance min(80%,10% +1%*M).

Merciful Aegis replaces Blessed Protection: first qualifying incoming hit per protected recipient starts five healing pulses, one per second, each5% caster P. Same capped knockback bonus as Bulwark. Readiness is not replenished by refresh/overlap. Healing occurs after surviving the trigger; not a resurrection.

Divine Oath replaces Judgement:0.75s cast,20s cooldown,15-block self/ally targeting (self fallback when no eligible ally aimed at). Reuse the old falling hammer visual, follow the selected recipient, apply on impact. Damage enemies within4 blocks for30% P and stun1s. Recipient receives additional flat damage15% caster M per qualifying offensive hit, armor5+10% caster armor and absorption2+15% P, for7s. Extra damage works with weapon/spell hits but only once per recipient per0.5s; no recursive proc. Apply it as a separate native skill impact with correct owner/damage routing. Allies are not damaged by the landing.

Improved Oath: cooldown17s. Decree replaces Empowered Judgement: additional damage20% M, armor5+15% caster armor, absorption2+20% P (retain flat2). Judgement replaces Judgement of Command: landing radius6, damage70% P, stun2s. Branches stay exclusive. Same support buff refreshes rather than stacking; keep stronger values. Absorption refreshes preserve spent HP and grant only newly added capacity. Death/expiry/dispel/respec clean up runtime modifiers and timers.

Sinful Retaliation replaces Conviction:100% on roll,12s cooldown, readiness6s for one qualifying incoming hit with an identifiable hostile living attacker (resolve projectile owner). Consume before retaliation. Attacker marked, takes15% more damage for8s, burns4s. Environment/ownerless damage does not consume readiness.

Break Restriction replaces Blessing of Freedom: retain installed10s cooldown and self cleanse; explicitly cover stun/Frozen/root/Slowness. With this node selected and its passive cooldown ready, a controlled player can initiate a combat roll and cleanse; the other passive choice cannot roll while stunned. Preserve native roll charges, cooldown, food and other eligibility. No unrequested party speed buff from the earlier brainstorming proposal.

## Effect details and supplied art

Effect Insights already renders native per-instance attribute modifiers using active amplifier/stack count. Preserve that pipeline. Add synchronized server-resolved supplemental details for custom caster-scaled support and missing class mechanics; display actual received percentages/HP/armor/remaining charges, not formulas, floor or {impact} placeholders. Cover both buffs and debuffs across Archer, Rogue, Warrior, Priest, Paladin and Arcane/Fire/Frost. Audit registered effects and record whether native attributes already supply values or require supplemental/semantic description. Do not invent numeric values for pure control effects.

Use a small native NeoForge synchronized attachment to carry resolved custom details from server to client, rather than recomputing from the recipient's P/M. Store short runtime support records with expiry/owner information for consistent cleanup and refresh. Tooltip-only client class stays isolated from server class loading.

Supplied RGBA PNGs are pack-official artwork:07_34_07 Root,07_33_57 Stun,07_33_40 Frozen. Preserve transparency/art; produce appropriate native texture sizes using nearest-neighbor asset conversion. Replace applicable control-family status icons, retaining IDs. Control descriptions state actual limitations and show the supplied icon.

## Verification and delivery

Pin actual JARs. Native tests cover damage/undead conversion, pulse counts and coefficients, cap/mitigation order, snapshots/overlap, party safety, absorption anti-refill, cooldowns/haste, node-gated roll/cast permissions, first-hit readiness, cleanup and passive flips. Actual client tests cover targeting/casts, controlled rolling, received-value synchronization when caster differs from recipient, new names, buff/debuff tooltips and icons. Inspect screenshots. Preserve same filenames and fixed/retired pack workflow; sync current raw references, patch notes and handbook/PDF. No personal profile/world or desktop server writes. Compilation alone is not runtime proof.
