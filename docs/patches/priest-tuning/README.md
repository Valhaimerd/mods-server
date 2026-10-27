# Priest tuning - 26 October 2026

Implements the confirmed Holy Staff, Penance, Holy Light and two-mote Lightwell changes over the current installed pack. Active Paladins & Priests 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5 filenames and saved skill IDs are preserved. Use matching updated client/server JARs and restart. No commit or push requested.

## Current values

| Skill / node | Change |
|---|---|
| Holy Staff Specialisation | Holy Shock deals 5% more damage. Only DAMAGE impacts get the modifier; the old three-second cooldown deduction is removed. Healing and normal cooldown are unchanged. |
| Penance | Five-second channel, ten bolts, one every 0.5s before haste. Smooth 8-12s completion-based cooldown. Four-block blast radius. Base damage 15% Healing Power per explosion, 20% with Improved. Other upgrades, shield amounts/durations, knockback and flight range remain. |
| Penance caster support | A living ally/enemy contact grants the caster one normal shield application and selected Hysteria even when outside the blast. If already inside, the caster receives only the single area application. Terrain-only hits do not grant remote support. Killing hits still qualify. No direct-hit bonus damage or shield refill is introduced. |
| Holy Light | Ten-second channel, still 25 deliveries. Healing 10% / damage 20% Healing Power per delivery. Improved makes these 15% / 25%. Graceful Light heals for 20% and retains 2.0 knockback; Searing Light damages for 35% and retains ignition. Smooth 12-24s cooldown and all other mechanics remain. |
| Lightwell | Two simultaneous motes per existing firing cycle, with unchanged healing per mote, inherited power, firing cooldown/haste, range, lifetime and upgrades. Injured eligible allies are ranked by lowest **raw HP**, then distance and entity ID for deterministic ties. With two or more injured allies, the top two each get one mote. A sole injured ally gets both. If none are injured, existing useful cleanse/maximum-heart-stack targets are considered; otherwise the well idles. |

Both Lightwell targets are selected together at firing time. The existing every-tick primary target is also selected using raw HP. Subsequent cycles reevaluate health and eligibility rather than retaining a healed target. Party, pet ownership, range and line of sight filters are preserved. The caster's new remote Penance grant does not change support received by allies inside the explosion.

## Implementation

One new `PriestTuning` helper plus narrow ASM hooks overlay the current compiled classes. Existing `PriestRules` still supplies normalization, party classification, absorption/no-refill behavior and all unchanged impacts. Its final requested coefficients are adjusted after the previous impact logic. The native projectile launcher is reused twice for a Lightwell cycle, preserving physical well ownership and effective caster attribution. The native action supplies one cooldown, rather than starting a second firing loop.

Resources change only the two active spells, the Holy Staff specialization and relevant English descriptions. Current tree IDs/positions, Priest tiers, passive values, Barrier rules, other summons, enchantments and global damage policy are preserved. Unrelated dirty workspace files are not replaced.

## Build, install and proof

```powershell
python docs/patches/priest-tuning/build.py
python docs/patches/priest-tuning/run_probe.py
python docs/patches/priest-tuning/verify.py --installed
python docs/patches/priest-reference/export.py
```

Exact snapshots are in ignored `tmp/priest-tuning/before/`; candidates and an isolated server are alongside them. `artifacts.json` pins before/after hashes, changed entries and additions. Same-name active and `mod fixing/1 fixed packs` outputs match; predecessors are retained under ignored `mod fixing/retired packs/priest-tuning-20261026/`.

**619 isolated Priest/party checks passed** with Java-25 classes, NeoForge 26.2.0.90 and `-Xverify:all`, in a fresh disposable world. This includes previous Priest active, passive, Barrier, absorption and Better Party regressions; two actual launched Lightwell motes and their distinct/single injured targets; raw-HP ordering over percentage; three/two/one/no injured cases; full-health cleanse fallback; remote enemy/ally/killing-hit Penance caster support; no terrain remote grant or inside-radius duplication; Holy Staff native damage, healing and cooldown; coefficient branches, channel timing and smooth cooldown samples. The separate global damage multiplier is excluded to verify raw coefficients.

**4,750 archive/resource/description preservation checks passed.** No fresh connected-client visual playtest is claimed. Descriptions follow the existing concise effect guide and leave current cast/cooldown statistics to the native panel. The Priest reference and handbook/PDF are synchronized; personal installations/worlds are untouched.

Handbook verification: 77 pages retained, all 51 contents destinations match their printed page numbers, both distributed copies match, and all pages were rendered for layout review with pages 17-18 inspected at full reading size. No new clipping, blank pages or broken section flow was observed.

Subsequent [tier-III passive tuning](../priest-low-health/README.md) overlays the Skill Tree and Spell Engine outputs here. Preserve that overlay when rebuilding the active pack.

The later [Lightwell mote-spacing overlay](../lightwell-mote-spacing/README.md) adds a fixed 0.3s delay to the second same-target projectile. Preserve it when rebuilding the current pack.
