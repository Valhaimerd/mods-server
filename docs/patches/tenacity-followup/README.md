# Confirmed Tenacity threshold and lifetime

24 October 2026. The user's follow-up confirms Enrage's size stacks at all six health thresholds and the ultimate cooldown sequence remains 60/70/80/90/100 seconds. Those two mechanics already match the installed implementation.

Tenacity changes from the provisional 1–40% health / five-second absorption to **1–30% health / twelve-second absorption**. Absorption amount remains 6 HP plus 30% melee power, rounded to the nearest whole HP. Its 25-second haste-affected cooldown, two-second nearby stun, five-block radius and party/pet protection remain.

The build preserves the preceding [Archer/Wizard description overlay](../class-descriptions/README.md). Only `WarriorUltimates.class` in Spell Engine and two English descriptions in Skill Tree change. Tenacity's text matches its new values; Splitting Missiles is clarified to fire one extra missile on every third channel hit, starting with the first (four extra over a full channel), as confirmed by ProjectileLauncher's existing modulo-three rule. No missile mechanics change. The builder generates its source from the pinned [tier-III implementation](../warrior-tier-three/WarriorUltimates.java), changing the two explicit constants. Earlier patch notes retain their historical values.

```powershell
python docs/patches/tenacity-followup/build.py
python docs/patches/tenacity-followup/run_probe.py
python docs/patches/tenacity-followup/verify.py tmp/tenacity-followup/built
# After installing the verified same-filename candidates:
python docs/patches/tenacity-followup/verify.py
```

Inputs and candidate archives remain ignored under `tmp/tenacity-followup/`. `artifacts.json` records the exact hashes and changed entries. The isolated probe retains prior Warrior/shield/party checks and independently verifies no activation/cooldown at 40% health, activation at the inclusive 30% boundary, and 240 ticks of absorption. Backups reside under ignored `mod fixing/retired packs/tenacity-followup-2026-10-24/`; fixed copies match installed archives. No personal worlds or installations changed. No commit or push requested.

Isolated result: **946 Warrior/shield/party checks passed**.

Handbook proof: all 77 pages rendered; page 17 is the only pixel change from the reviewed prior edition and was visually reviewed. All 51 contents destinations remain correct, and root/output PDF hashes match.
