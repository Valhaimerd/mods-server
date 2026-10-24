# Warrior ultimates and health passives

Latest presentation fix: [tree names and concise tooltips](../skill-tooltip-polish/README.md). The original detailed descriptions below are historical; current tooltips reserve activation/cooldown details for the yellow section.

Local same-version overlay, 24 October 2026. Builds on [tier II](../warrior-tier-two/README.md) and preserves the existing spell IDs, class-tree placement and earlier pack patches. Both client and server need all three matching JARs. No personal worlds or installations are changed.

## Installed behavior

- **Momentum:** combat rolls ready five melee hits within six seconds. Each adds 2% base attack speed and 2% base movement speed, up to 10% each. Buffs last three seconds, refreshed by hits. Base cooldown 12 seconds; damage and resistance bonuses removed.
- **Berserker** replaces Mortal Strike. Channel five pulses over 2.5 seconds, stationary, with Last Stand's channel presentation. Each pulse grants 15% base attack speed, 10% extra basic melee attack damage, 15 knockback-resistance points and 20% increased damage taken. Basic-attack amplification never raises melee spell power. Buffs last 12 seconds, 15 with Improved. The fifth pulse fully heals and grants Speed IV for two seconds. Natural expiry fully heals. A fatal hit ends it, heals fully, puts Throw Weapon, Throw Net, Wild Charge and Shout on their current cooldowns, and applies ten seconds of Nausea and five seconds of stun.
- **Brutality:** four physical critical-chance points per pulse, up to twenty. **Lethality:** melee impacts heal 2 HP; every ten ticks costs 1 HP directly, ignoring armor, Resistance, absorption and immunity. A lethal cost uses Berserker's fatal-hit outcome.
- **Last Stand:** five pulses over 2.5 seconds; each grants 0.6 seconds of immunity, refreshes Regeneration I and full knockback resistance for 15 seconds (20 with Improved). No maximum-health inflation or healing pulses. Juggernaut adds 10% base size and 3% base incoming-damage reduction per pulse, up to 50%/15%, with matching duration. Sentinel replaces Revenge: direct damage during Last Stand adds 1% melee damage per stack, up to ten; five-second refresh, plus Regeneration I refreshed for five seconds without shortening a longer existing application.
- Both ultimates have base cooldowns **60/70/80/90/100 seconds after one/two/three/four/five pulses**, then native haste applies. Cancelling before any pulse costs no cooldown. Completed pulses retain their cooldown on interruption. Stun/freeze interrupts; roots do not. Buff durations remain fixed rather than haste-scaled. Berserker's attack-speed bonus itself contributes to native melee haste.
- **Enrage:** checks current health every five ticks, no hit/chance/cooldown requirement. Thresholds 70/60/50/40/30/20% grant 4% base melee damage and 8% base size each (24%/48% maximum). Three-second effect refreshes; recovery adjusts the level down immediately at the next check.
- **Tenacity:** at 1â€“40% current health, grants absorption equal to 6 HP plus 30% melee power rounded to the nearest whole HP, for five seconds. Stuns enemies within five blocks for two seconds. Base cooldown 25 seconds, haste affected. Caster, party allies and owned pets are protected.

Enrage's stacking size and Tenacity's five-second absorption duration are the documented defaults used while those optional clarifications remain unanswered.

Warrior and Rogue class descriptions use plain percentages and timing; no unresolved impact tokens or rounding formulas. The remaining Mutilate upgrade token is replaced with its two-second cooldown reduction. Technical formulas remain in the raw reference, outside player tooltips.

## Rebuild and proof

Inputs are pinned in `inputs.json`; `artifacts.json` records exact changed entries and output hashes. Preserve the three original archives under `tmp/warrior-tier-three/before/` when rebuilding. Backups belong under ignored `mod fixing/retired packs/`, never Git.

```powershell
python docs/patches/warrior-tier-three/build.py
python docs/patches/warrior-tier-three/run_probe.py
# Install candidates only after a passing isolated probe and unchanged active input hashes.
python docs/patches/warrior-tier-three/verify.py
python docs/patches/warrior-reference/export.py
```

Result: **944 Warrior/shield/party checks passed**.

The rebuilt 77-page handbook was rendered and reviewed, including the updated Warrior page. Its 51 contents destinations retain correct page numbers; root and output PDF copies match.

The probe runs NeoForge 26.2.0.90 on Java 26 with Java-25 classes and `-Xverify:all`. It exercises native damage, attack and status paths; prior Warrior/Rogue/party/shield regressions run in the same fixture. Client animation appearance still requires an in-game playtest; compilation and server checks do not establish visual quality.

Confirmed follow-up: [Tenacity now triggers at 30% HP or lower and grants twelve-second absorption](../tenacity-followup/README.md). Enrage size stacking and the 60/70/80/90/100-second cooldown sequence are confirmed. The provisional values above describe this original revision.

Latest Warrior overlay: [minimum-charge throws, health pulses, death cleanup and icon fixes](../warrior-followup/README.md). The current hashes are recorded there; earlier build outputs remain historical.
