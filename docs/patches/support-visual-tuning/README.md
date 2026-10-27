# Support and visual tuning

26 October 2026. Same-filename Skill Tree 1.6.1 and Spell Engine 1.10.5 overlays over the current Priest/Warrior pack. Both sides need matching JARs and a restart. No push requested.

- **Last Stand:** the existing gold model/equipment tint increases from 30% to **50%**. The separate scrolling gold glint, enchantment-glint suppression, synchronized lifetime and death/expiry cleanup remain. This changes color intensity, not armor transparency.
- **Lightwell:** when exactly one eligible ally is injured, **both motes target that ally**, including when full-health allies nearby have useful cleanse or heart upgrades. This behavior already existed; the current helper is preserved byte-for-byte and explicitly regression-tested. Two injured allies still receive one mote each, prioritizing lowest raw HP. Existing useful-support fallback when nobody is injured remains.
- **Barrier:** already uses a **10-block radius**. Its native diameter is 20 blocks; protection and projectile interception use the circular boundary. No unnecessary radius change or Paladins repack is made.
- **Sacred Refuge:** lasts as long as Barrier: **10s normally, 12s with Improved Barrier**. Its radius remains four blocks; healing stays 10% Healing Power and cleansing remains every two seconds. Improved Barrier gains a second native `spawn_duration_add: 2` modifier matching the Refuge passive. This extends the actual cloud entity, not merely its description.

## Reproduce and verify

```powershell
python docs/patches/support-visual-tuning/build.py
python docs/patches/support-visual-tuning/check_resources.py tmp/support-visual-tuning/built
python docs/patches/support-visual-tuning/run_probe.py
python docs/patches/support-visual-tuning/verify.py --installed
```

The baseline resource check failed because Improved Barrier did not extend Refuge. Candidates passed **699 isolated Priest/party checks** with Java-25 classes, NeoForge 26.2.0.90 and `-Xverify:all` in a fresh disposable world. New checks verify the common 50% tint value; two actual mote projectiles following the sole injured ally despite full-health upgrade targets; native cast-triggered Refuge clouds at 200/240 ticks matching Barrier; ten-block radius, twenty-block diameter, unchanged Refuge radius and pulse interval. Previous Priest actives, passives, shield, Barrier and Better Party regressions also pass. This task does not claim a new connected-client visual playtest; the unchanged sheen/rendering path retains its prior client verification.

**3,615 archive/description preservation checks passed.** Changed entries are exactly LastStandAppearance.class, Improved Barrier's resource and two relevant English descriptions. Existing Lightwell/defense helpers, renderer/mixins, glint texture, other passives and gameplay entries are preserved byte-for-byte. Before/after hashes are recorded in `artifacts.json`; runtime results are adjacent. Ignored inputs/candidates/server are in `tmp/support-visual-tuning/`; predecessors are in `mod fixing/retired packs/support-visual-tuning-20261026/`. Active and fixed-pack copies match. Personal installations/worlds are untouched.

Priest and Warrior references plus the player handbook/PDF are synchronized.

PDF verification: 77 pages retained, all 51 contents destinations match, and both output copies agree. All pages were rendered for overview review; changed pages 17-18 were inspected at reading size without new clipping or broken section flow.

The later [Lightwell mote-spacing overlay](../lightwell-mote-spacing/README.md) adds a fixed 0.3s delay to the second same-target projectile. Preserve it when rebuilding the current pack.
