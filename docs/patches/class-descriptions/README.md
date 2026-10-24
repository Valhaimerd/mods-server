# Archer and Wizard description audit

Superseded presentation wording: [skill names and concise tooltips](../skill-tooltip-polish/README.md) also synchronizes tree titles and supplies accurate yellow activation labels. Values below document this earlier revision.

24 October 2026. English spell, secondary-node and passive descriptions for Archer, Arcane, Fire and Frost were checked against the installed spell JSON, runtime helper sources and registered effect defaults.

All **135 descriptions** are covered by the scan. Archer already had concrete descriptions; its Barrage/Magic Arrows/Endless Volley wording is clearer. Wizard trees retained modifier/range/duration template tokens, awkward school-power phrasing and excessive Blizzard precision. The final overlay replaces those tokens with readable percentages, distances, durations and upgrade benefits.

Nimble Shield's text now accurately says it adds 50% base movement speed for three seconds, offsetting half of Frost Shield's -100% base movement penalty. It does not restore full movement under ordinary defaults. Meteor text states three meteors, six-block splash and no falloff. Charged Beam explains its 10–15% damage and 1.3–5x width progression without formulas. Wards retain plain absorption-heart descriptions and clarify haste.

Only English `.description` values change in the existing same-filename Archers, Wizards and Skill Tree JARs. There are no gameplay JSON, bytecode, translation-key, texture or archive-entry changes. Existing base descriptions explicitly distinguish pre-upgrade values where useful. Modifiers remain described on their own nodes; the native statistics panels still supply current cast/cooldown information.

`descriptions.json` is the explicit final wording. `build.py` snapshots current inputs on its first run under ignored `tmp/class-descriptions/before/`; subsequent runs use those snapshots. Candidates go under `tmp/class-descriptions/built/`. `artifacts.json` records input/output hashes and changed keys. Rebuild this overlay after earlier gameplay builders; do not rerun the historical emitter rollback builder as a description-only patch.

```powershell
python docs/patches/class-descriptions/build.py
python docs/patches/class-descriptions/verify.py tmp/class-descriptions/built
# After reviewing and installing the matching candidates:
python docs/patches/class-descriptions/verify.py
```

Verification compares every archive entry and every language key, checks CRCs, forbids formula/template tokens across all 135 descriptions and checks percentage escaping. Gameplay and bytecode must be byte-for-byte identical to the inputs. This is a description-only change; no new combat simulation or client tooltip visual playtest is claimed.

Previous active/fixed archives are retained in ignored `mod fixing/retired packs/class-descriptions-2026-10-24/`. Personal installations and worlds remain untouched. The player handbook's existing mechanics remain accurate, so this wording-only pass does not require a handbook/PDF revision. No commit or push requested.

The final [Tenacity follow-up](../tenacity-followup/README.md) also clarifies Splitting Missiles: its existing launcher adds one missile every third channel hit, starting with the first, for four extra per full channel. This corrects the old ambiguous batch wording without changing missile behavior.
