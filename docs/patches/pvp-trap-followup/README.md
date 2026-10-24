# PvP and Improved Bear Trap balance - 24 October 2026

Attributed player-to-player damage now retains 30% instead of 50%. A 20-point event becomes 6 instead of 10 before any later modifiers. This is 40% less damage than the previous pack policy. The PVP Flagging replacement still leaves PvE, ownerless damage and party/team protection unchanged. Its server fixture passed 29 checks across melee, arrows and Arcane/Fire/Frost. The previous 50% build failed the new expectation with 10 damage instead of 6.

Improved Bear Trap now deals 60% combined dual-melee power instead of 75%. Its ordinary unupgraded hit remains 40%. The installed base cooldown was already 4 seconds and remains 4 seconds, reduced by haste; no longer cooldown or haste bypass was added. Root remains 1 second and Slowness I remains 3 seconds. The 75% coefficient lived in `RogueRules.impact`, rather than the visible modifier JSON. The build changes exactly that one constant using an ASM match-count assertion and updates the Improved tooltip. It preserves every other class, spell, party rule and the preceding RPG lifecycle repair. The unchanged Rogues candidate is only a verification input, not a new active replacement.

The dedicated fixture passed 946 Warrior/shield/Rogue/party checks, including actual 60% trap detonation, Serrated, jumping activation, area control and pet protection. Source baseline assertions are updated for 24 damage from a 40-power upgraded trap instead of 30. PVP rules are exercised in their separate 29-check fixture.

Build: `python docs/patches/pvp-trap-followup/build.py`; regression: `python docs/patches/pvp-trap-followup/run_regression.py`. PvP build/test: `python docs/patches/pvp-flagging-26.2/build.py`, then `python docs/patches/pvp-flagging-26.2/run_probe.py --candidate`. The two fixtures use disposable repository servers and shut down after results.

Same-filename PvP, Spell Engine and Skill Tree replacements are installed in repository mods and fixed packs. Previous active files remain in ignored `retired packs/pvp-trap-followup-2026-10-24/`. Personal installations, external server and worlds are untouched. Restart/sync those installations to receive the changes. README, handbook, rebuilt PDF and Rogue reference are synchronized. No commit or push requested.

The subsequent [global player damage revision](../pvp-flagging-26.2/README.md) restores 50% PvP retention after a separate 20% player-damage reduction. The 30% PvP figures above are historical; Bear Trap coefficients remain unchanged.
