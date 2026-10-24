# Vanish held-item visibility repair - 24 October 2026

Vanish now hides main-hand and offhand items while Rogue Stealth is active, in first-person and third-person rendering. Equipped items and their combat effects remain intact. The items return on the next render after Stealth ends or is dispelled. Ordinary Invisibility, armor rendering, Stealth timing, Shadow Stealth and cancellation rules are unchanged.

## Cause and repair

Rogues set the normal invisibility flag in `LivingEntityStealth`, but supplied no client held-item rendering hook. Native hand layers therefore kept submitting opaque weapon models after the body disappeared.

Two client-only mixins are appended to `rogues.mixins.json`. `StealthHandsMixin` cancels first-person hand submission while Stealth is present. Priority 2000 and injection order 500 place it before Punchy's own HEAD callback; a default-priority hook hid native hands too late to cover Punchy's renderer. `StealthHeldStateMixin` clears both hand model states and their stack snapshots after native armed-state extraction. This touches render snapshots only, not the player's inventory. It also covers animation-library item layers reading those snapshots. No entity scans, tasks or tick listeners are added to the production JAR.

## Installation and rebuilding

Same-filename `rogues-neoforge-3.1.2+26.2.jar` is installed in `mods/` and `mod fixing/1 fixed packs/`. Previous active/fixed copies are retained under ignored `mod fixing/retired packs/vanish-held-items-2026-10-24/`. Matching clients need this JAR for the visual change; a server-only update cannot replace client rendering. No personal Minecraft installation, external server or world was modified, and nothing was committed or pushed.

```powershell
python docs/patches/vanish-held-items/build.py
python docs/patches/vanish-held-items/verify.py --candidate --runtime
python docs/patches/vanish-held-items/verify.py --runtime
```

Build uses the pinned pre-repair archive under `tmp/vanish-render/before-*` plus the existing local compilation classpath. Do not use that historical source to replace later patches without reviewing them first. `artifacts.json` records the exact input/output hashes. Only the mixin configuration and two added Java 25 client classes differ; all original classes, spell data and the approved English wording are preserved.

## Verification

An isolated Minecraft 26.2 / NeoForge 26.2.0.90 client reproduced the original visible hand snapshots and missing first-person cancellation. The candidate passed all eleven connected-client checks in `candidate-results.txt`, with Punchy 2.8f, Player Animation Library 1.2.8 and CrossArms 1.4 loaded. Checks cover both hand models, render-stack snapshots, unchanged equipment, actual first-person early exit before Punchy submission, restoration after Stealth removal and unchanged ordinary Invisibility. `runtime-tested-sha256.txt` pins the tested candidate. This is renderer-state and callback verification, not a manual multiplayer or shader screenshot comparison.

The existing handbook's Vanish description remains accurate; this repairs its visual presentation without changing controls or gameplay, so no catalog or PDF change is needed.
