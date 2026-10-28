# Paladin tier III and visual retouch

Minecraft26.2 ? NeoForge26.2.0.90 ? Java25 ?29 October2026.

Battle Banner now travels with its caster. Purgatory replaces Immolation with a staged, stationary ultimate; Ardent Defender supports one endangered party player and Reinforced replaces Divine Hammer. Existing IDs, scrolls, class choices, doctrines and tier-I/II mechanics remain. Only Paladin presentation is retouched; other classes and Rogue Vanish changes remain deferred.

## Gameplay

See [exact tier-III values](reference-tier-three.md) and the owner's [full Paladin reference](../../../mod%20fixing/Paladin%20skills%20-%20raw%20attributes%20and%20nodes.md). Banner lasts20/25s with6/8-block radius, fixed0.5s pulses and independently expiring sources. Each ally has one2s rescue allowance per cast. Percentage combat bonuses cap separately and actual received values appear in Effect Insights.

Purgatory channels for30s with60 fixed deliveries. Its10/20/30% holy hybrid pulses strengthen at10/20s; later deliveries spend2/3 real HP through ordinary defenses, totaling103 HP over a full channel. Damnation raises pulses to15/25/35% and the finish to900% M. Final Resolute ends the final phase with an earned blast, ally shield and3s protection without healing the caster. Ordinary final-phase release gives an earned partial explosion; full completion or actual final-phase death adds1s stun and an8s ally shield. Stun/Frozen interrupts without earning a pending completion delivery. Root does not. Dispel and administrative cleanup stop gameplay and presentation without a free blast.

Ardent uses one90s shared haste-affected cooldown,6-block player-only support,40% maximum-HP healing and10s defenses. It grants benefits without cancelling the hit. Reinforced checks health every0.25s and grants5/10/15/20% base armor and Healing Power below70/60/50/40% health. All cooldowns retain haste; Banner pulses and Purgatory stages do not accelerate.

Player/summon damage defaults remain80% against mobs/35% against players, with25% more Paladin/Priest skill damage against undead. Direct caster HP costs and healing bypass that damage policy. Protected party players/pets remain safe from harm; Ardent explicitly excludes pets. Shields never refill consumed HP on refresh.

## Presentation and wording

- Native Banner model follows the shoulder with explicit position synchronization and nonzero visual bounds; its cloud has no gameplay impacts. Aura/rescue logic uses the real configured radius.
- Purgatory uses native Last Stand channel animation, orange weapon glint, fire/sparks and gold/orange/red stage rings. The outer pull boundary is separate from the inner damaging area. The ending blast scales in intensity with earned strength.
- Banner rescue uses a flash/chime; Ardent adds a heart burst. Paladin stack gains, retaliation, actual control cleanses, Protection and Oath receive restrained cues. Existing Purge impacts, gold waves, falling hammer and official Stun/Frozen/Root icons remain.
- Node text follows [the description rewrite style](../../../mod%20fixing/RPG%20Skills%20-%20description%20rewrite.md). Native cooldown/range and custom gold trigger rows carry activation statistics. No raw impact/floor tokens or written-out percentage boilerplate. [Before/after wording](wording-review.json) records each affected key.

## Build and verification

Run from repository root:

```powershell
python docs/patches/paladin-tier-three/build.py
python docs/patches/paladin-tier-three/check_resources.py tmp/paladin-tier-three/built
python docs/patches/paladin-tier-three/run_server.py
python docs/patches/paladin-tier-three/run_server.py --legacy
python docs/patches/paladin-tier-three/client/build-client-probe.py
python docs/patches/paladin-tier-three/client/launch-isolated-client.py --world
python docs/patches/paladin-tier-three/verify.py --installed
```

The isolated client directory must already contain the normal class/runtime dependencies, matching candidates and the native probe; the launcher reads cached26.2.0.90 libraries/assets without editing personal Minecraft. Build inputs are pinned current uncommitted pack archives under ignored `tmp/paladin-tier-three/before`, not an older Git baseline. [artifacts.json](artifacts.json) lists exact before/after hashes and changed entries; installation records and final test manifests bind installed bytes to tested bytes. Same active filenames are retained; rollback archives remain ignored.

Regression evidence includes original resource failures and native RED?GREEN checks for Banner visual lifetime, client follow/bounds, CC at the final delivery, dispel cleanup and recognized magic-source reduction. Fresh and stale-config server fixtures include the original77 tier-I/II checks and new stage/party/absorption/defense cases. The real client verifies native casts, all Purgatory phases and103 HP cost, stance/glint cleanup, received tooltips, gold trigger rows and Enhanced Cats rendering. This is isolated runtime evidence, not a full live-server multiplayer fight or every renderer/shader/particle-setting combination.

The separate [cat replacement record](../cat-replacement/README.md) documents removal of Cute Cats. No personal installation, world or separate desktop server is changed. No GitHub push is included.
