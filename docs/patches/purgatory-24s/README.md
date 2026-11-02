# Purgatory: 24-second channel

2 November 2026. Purgatory channels for **24s**, delivering **48 pulses** at the unchanged **0.5s interval**. Stage II and control protection begin at **8s**; stage III, its immunity and Final Resolute eligibility begin at **16s**. Continuous pull follows those same boundaries. The enclosure's safety lifetime and channel stance are shortened with the cast.

Voluntary final-phase release scales smoothly from **200% Melee Damage at 16s** to **700% at 24s**, or **900% with Damnation**. The midpoint at 20s deals 450% / 550%. Completion and actual final-phase death retain their existing full-strength finish. Final Resolute still uses the strength earned when it stops. No explosion occurs before 16s. Per-pulse costs and damage stay the same; the shorter full channel now costs 83 shield/health points before outside healing, rather than 103.

The 12s cooldown, continuous pull strengths, buried enclosure, 30% Ardent Defender healing, corrected node range UI and pending six-second Ward tuning remain. Descriptions contain the revised timing without exposing the ramp formula.

**50 focused native-server checks passed**, covering native cast duration/deliveries, damage/pull phase boundaries, control protection, explosion coefficients, completion, true cost and enclosure cleanup. Method-scoped bytecode guards leave unrelated skill/buff timers unchanged. Inputs/outputs are recorded in [artifacts.json](artifacts.json). Same-filename replacements are installed in repository `mods/` and ignored fixed packs, with originals under ignored `mod backup/purgatory-24s-2026-11-02`. Personal installations are untouched; both clients and server need matching files and a restart.
