# Combined phases 4-6 ledger
Status: phases 4-6 complete; four tested replacements installed in repository mods and fixed packs.
Baseline: current phase 1-3 installed bytes, pinned in baseline.json. Existing unrelated changes preserved.
Execution: inline on the existing feature branch. Isolated scratch/client/server under tmp/class-balance-phases-4-6; no personal/live-server edits or commit/push.
Ruling: Improved Frost Spikes retains its existing +10% spell-power modifier. The explicit 80/100% values are damage coefficients applied to the resulting P, rather than a second coefficient multiplier.
Confirmed: Frostbite includes class skills, Frost basics and owned Elementals. Near Zero freezes triggering targets even without native freeze. Class skills arm readiness; Frost basics consume Winter's Chill without rearming.
- [x] Arcane mechanics and emitter speed
- [x] Fire, weapon basics, traps and large Hydra
- [x] Frost status, targeted spike, passives, Lance and Blizzard
- [x] Native/client verification, installation and documentation


## Verification and rulings
- Baseline resource proof: 0/16 requested new values present; 16 expected failures. Candidate: 16/16 passed.
- Native final Java 25 / Minecraft 26.2 / NeoForge 26.2.0.90: 97/97 checks passed. Actual freezing damages and finite expiry, native immunity, external snow retention, spike snapshot dodge, Glacial/Fan/Shield damage, class-only readiness, one-use basic empowerment, Arcane/Fire Ward, attacker-centered Eruption, Colossal armor penetration, Arctic Reflex x1.2 and saved charges were exercised.
- Exact-byte prior-phase regression: 104/104 native checks passed.
- Fresh connected client: 22/22 checks passed, including controlled native Blink, both charges, third-cast rejection, death/respawn without refill, serial recovery, Ward movement/effect sync, readiness sync, giant spike rendering, Ancient Hydra size/ground-pulse metadata/live slow movement, and an owned Elemental applying Frostbite.
- Runtime failures caught and fixed: real modifier-list Blitz selection; native area impact resolution ignoring copied impact lists; integer-rounded Blink final step; Near Zero exposure extension; source-less Eruption guard. Fixture fixes: ticking chunks, supplying native passive sources, deterministic critical chance, fresh isolated client worlds/selected slot and including all phase 1-3 dependencies in regression.
- Ruling: actual Arcane wand Bolt is a projectile. Keep its delivery and set range to24; do not convert it to instant damage. Cost if wrong: differs from the user's delivery assumption, while preserving the installed weapon's mechanics.
- Ruling: native freeze immunity, including leather, remains authoritative. Exposure is not an extra custom damage loop. Cost if wrong: immune targets will not take the new cold damage.
- Ruling: the final shield-depleting Flameward hit can retaliate if absorption remained when that hit began. Cost if wrong: one extra eligible retaliation relative to testing the already-depleted shield afterward.
- Ruling: exact fractional Blitz travel is attempted only when safe; otherwise keep native partial landing. Cost if wrong: obstructed casts can travel less than7.5 blocks, preserving collision/boundary safety.
- Final review: self-review per the approved inline/no-extra-workers plan; no delegation used. Existing charge saves merge with other cooldown entries, coefficient overrides preserve Arctic Reflex, scope clears with try/finally, and no probe classes ship. No known required work deferred within phases4-6; phases7-9 remain separate.
- Installed active/fixed filenames retained. Actual preceding copies backed up under mod fixing/retired packs/class-balance-phases-4-6-2026-11-04/{active,fixed}. Unrelated World Map/user changes preserved. No commit, push, personal-world or desktop-server edits.
- Player handbook, raw-reference notices, concise description reference and phase status records synchronized; handbook PDF rebuilt and reviewed.

Final follow-up: seeded the old Evocation +10% damage-taken effect configuration in the isolated server; all 97 native checks passed again, proving the removed penalty does not return from that stale configuration. Final artifact/install verification: 73/73. PDF: 80 A4 pages, 219/219 image placements, 685 links including 469 internal links, zero missing HTML targets/blank pages/replacement characters; changed pages 17-18 visually reviewed. The old PDF checker had fixed 16-image/title assertions from an older handbook, so the current-content checker is verify_pdf.py.
