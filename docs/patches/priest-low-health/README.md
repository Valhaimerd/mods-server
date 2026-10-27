# Priest tier-III passive rework

Same-filename overlays for the current Skill Tree 1.6.1 and Spell Engine 1.10.5. Install matching client/server JARs and restart. Existing spell IDs, tree positions and saves remain. No push requested.

| Passive | Current behavior |
|---|---|
| Pain Suppression | Every 0.25s, compare current HP with current maximum HP. Below 50/40/30/20%, reduce incoming damage by 6/12/18/24%. No cooldown, healing trigger or recipient buff. Healing weakens/clears protection on the next check. |
| Celestial Orbs | Critical active spell impacts ready five orbs for 15s. Each retaliates for 30% Healing Power with the existing 0.1 knockback. One charge is spent when the caster or an eligible ally within eight blocks of the caster takes hostile attributed damage, including shield-absorbed damage. |

Thresholds are strictly below the listed percentages, independent of absolute maximum HP. Protection uses the original `pain_suppression` effect and incoming-damage attribute. Its icon and status presentation remain; original particles and sound play on entry or increased protection, rather than every check. Downgrades remove the old instance before replacement so hidden higher levels cannot return. Unequipping the passive or death clears the effect; the short lease also expires without refresh.

Celestial keeps its original critical trigger, readiness duration, icon, orbiting visuals, activation sound and impact presentation. Existing native self-hit processing remains, avoiding double spending on the caster. The ally hook affects only Celestial, preserving other ready effects. Party members and owned/party pets qualify; unrelated entities, friendly attackers and source-less environmental damage do not. Each qualifying hit spends one charge per protecting Priest. Targets outside the eight-block sphere do not qualify. Charges are reserved before retaliation to prevent recursive ally-trigger loops. Native spell impacts retain party filtering and the pack's separate final damage policy.

## Build and proof

```powershell
python docs/patches/priest-low-health/build.py
python docs/patches/priest-low-health/check_resources.py tmp/priest-low-health/built
python docs/patches/priest-low-health/run_probe.py
python docs/patches/priest-low-health/verify.py --installed
```

The original installed resources failed the new healing-trigger removal check. Candidates passed **679 isolated Priest/party checks**, including 60 new checks for threshold boundaries, native five-tick updates, changed maximum HP, weaker levels after healing, passive removal, no recipient protection, actual damage reduction (10 HP to 7.6 HP at 24%), five native ready charges, per-hit consumption, 30% P damage, native own-hit/no duplicate, actual ally damage and absorbed-hit events, range boundaries and friendly/unrelated exclusions. Earlier Priest active/passive/Barrier/absorption and Better Party regressions also passed. Java-25 classes, NeoForge 26.2.0.90, `-Xverify:all`, a fresh disposable world and Normal difficulty were used. The simulated player was marked loaded so native server damage could run. No connected-client visual playtest is claimed.

**3,617 archive/description/visual preservation checks passed.** Only the two passive resources, their English descriptions, one configure hook and one added helper change. Visual resources and unchanged classes remain byte-identical. `artifacts.json` records exact before/after hashes. Inputs/candidates/server live under ignored `tmp/priest-low-health/`; ignored backups are under `mod fixing/retired packs/priest-low-health-20261026/`. Active JARs and matching `mod fixing/1 fixed packs` copies agree. Personal installations and worlds were untouched.

The Priest reference and handbook/PDF are synchronized. Full runtime records are alongside this file.

PDF review retained 77 pages with all 51 contents destinations correct. Both output copies match. All pages were rendered for overview review; changed pages 17-18 were inspected at reading size without new clipping or broken section flow.

The subsequent [support/visual overlay](../support-visual-tuning/README.md) preserves these passive hooks; retain it when rebuilding the current active pack.
