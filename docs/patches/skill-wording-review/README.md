# RPG skill wording - 24 October 2026

Implemented the approved [individual descriptions](RPG%20Skills%20-%20description%20rewrite.md) for Arcane, Fire, Frost, Archer, Rogue and Warrior. Priest and Paladin are excluded. The matching IDE reference is `mod fixing/RPG Skills - description rewrite.md`.

All 196 reviewed descriptions are covered: 180 class skills/upgrades/passives, seven Wizard weapon attacks and nine internal helpers. There are 176 rewrites and 20 retained descriptions. Words drop from 3,089 to 2,149 (about 30%); no individual description is longer. Hidden helpers remain hidden.

## Packaging

Four same-filename replacements are installed in `mods/` and `mod fixing/1 fixed packs/`: Archers 3.1.2, Rogues 3.1.2, Wizards 3.1.2 and Skill Tree 1.6.1. Only their English language resources changed. Other translation keys, spell data, combat code, activation labels, dynamic cooldown panels and Priest/Paladin content are byte-identical or value-identical to the reviewed sources. Spell Engine is unchanged. Percent signs are escaped as `%%` for the native formatter.

Predecessors are in ignored `mod fixing/retired packs/skill-wording-2026-10-24/`; previous fixed copies are under its `fixed/` subfolder. `artifacts.json` records exact before/after hashes and changed entry/key counts. `wording-review.json` retains the original and approved copy and reviewed source fingerprints. `review-checks.json` records the preceding brainstorming audit; `implementation-checks.json` records the installed checks.

## Rebuild and verify

From the repository root:

```powershell
python docs/patches/skill-wording-review/build.py
python docs/patches/skill-wording-review/verify.py tmp/skill-wording-implementation/built
python docs/patches/skill-wording-review/verify.py
```

Build creates candidates only; it uses the exact archived reviewed sources and refuses source fingerprint mismatches. The second command verifies candidates; the third verifies installed JARs and matching fixed copies. Do not run older tooltip builders over these newer archives. Future balance updates need a new source review rather than rebuilding from this historical baseline.

## Verification and scope

Before implementation, verification failed on Barrage's unapplied wording. Candidate and installed verification then passed: 196 exact approved strings, 176 changed keys, valid ZIPs, escaped percentages, identical archive inventories and preservation of every unrelated entry/key. A fresh independent reviewer found no important issues and confirmed the escaping convention.

No new client visual or gameplay run was performed: this is a language-only overlay. No personal Minecraft/server files or worlds were touched. Handbook gameplay, controls and mod catalog entries remain accurate, so no handbook/PDF rebuild was needed. No commit or push was requested.
