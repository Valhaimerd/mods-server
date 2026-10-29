# Vanish breathing transparency — 29 October 2026

Client-only replacement for the earlier all-or-nothing Vanish equipment suppression. While `rogues:stealth` is present, the body, armor, main/offhand models, native first-person hands and Punchy's first-person arms/items share a violet-tinted transparency cycle: 10% at cast start, 0% after two seconds, 10% after another two seconds, repeating until Stealth ends. A subtle animated violet glint follows that same wave. At exactly zero, model/item geometry and the glint overlay are omitted; there is no persistent revealing particle trail. The existing release smoke/audio is preserved.

The candidate is `tmp/vanish-breathing/built/rogues-neoforge-3.1.2+26.2.jar`. This folder does not install it. No Spell Engine patch entries are needed. Match `artifacts.json` and `runtime-tested-sha256.txt` before installation. The original active filename remains unchanged.

## Implementation

The prior `StealthHeldStateMixin` is removed from the client mixin list, preserving both hand model snapshots. `StealthHandsMixin` now replaces the frame's submission collector before Punchy's HEAD callback instead of cancelling rendering. A render-state mixin attaches the same opacity to body/armor rendering. Only that client snapshot becomes visible; the actual entity remains invisible for gameplay. Native model submissions use genuinely translucent materials and packed vertex alpha. Held-item quads use their original atlas textures/tint layers through native custom geometry with the same alpha. Model-part/default collector overloads and ordered armor layers pass through the wrapper, covering Punchy and special model items such as shields. Shadows/name tags/flame overlays are suppressed during the effect.

The separate glint pass reuses Minecraft's purple armor-enchantment texture, `ARMOR_ENTITY_GLINT_TEXTURING` scrolling transform and `ENERGY_SWIRL` pipeline. No shader or texture assets are added. That native additive blend ignores vertex alpha, so the glint's RGB intensity is explicitly multiplied by the same wave, with each channel capped at 25/255 at peak and 12/255 halfway. The existing violet texture further attenuates it (maximum single-pass added channel brightness about 6.3%). Both the translucent material and glint pass are skipped at exact zero. All of these renderer references are reached only through the client mixin list, leaving dedicated-server gameplay classes untouched.

Phase is derived from synchronized remaining effect duration: `elapsed = 140 - remainingTicks + partialTick`, then an 80-tick triangle wave with its minimum at tick 40. The installed Vanish lasts seven seconds; the installed Improved Vanish adds a separate seven-second marker without extending Stealth. This avoids independent per-model timers and client observation-start resets. A future duration change must update this base, or add an explicit cast-start synchronization field. Normal network delivery differences between observers remain Minecraft's usual effect synchronization limitation. Alpha is quantized to the native eight-bit channel (peak 26/255, half 13/255).

Later vanilla Glowing or its synchronized entity flag bypasses the custom transparency/glint collector, preserving native reveal outlines and equipment rendering. Native observer outline state also takes precedence. When that reveal ends, breathing resumes at its remaining-duration phase. Sinful Retaliation co-applies vanilla Glowing with its mark; Hunter's Mark is a separate model marker rather than an intrinsic Glowing effect. The existing successful-cast cleanse remains unchanged. Five native checks cover later reveal, native outline, synchronized glow flag, and phase restoration.

Only `rogues.mixins.json`, the existing first-person client hook, and added client renderer/accessor classes differ. Gameplay classes and all spell/effect resources remain byte-identical, preserving duration, break rules, mark cleanse, Improved Vanish and server stealth behavior.

## Build and native proof

```powershell
python docs/patches/vanish-breathing/build.py
python docs/patches/vanish-breathing/run_client.py
# Wait for the disposable client's vanish-result.txt, then:
python docs/patches/vanish-breathing/verify.py
```

The isolated client uses Minecraft 26.2, NeoForge 26.2.0.90, Java 26 with Java 25 production classes, Punchy 2.8f, Player Animation Library 1.2.8 and CrossArms 1.4. It copies its disposable world and current matching mod dependencies into ignored `tmp/vanish-breathing/client`; personal worlds and external installations are untouched. Native renderer calls exercise all four armor pieces, a diamond sword and shield at 0/20/40/60/80/100/120 elapsed ticks. The proof checks body/armor availability, actual translucent material selection, emitted quad vertex alpha, complete zero-phase omission, restored rendering, ordinary Invisibility and Punchy's real first-person entry point.

All **74 native checks passed**. `candidate-results.txt` records them; the tested engine hash is recorded in `artifacts.json`. The peak emitted alpha 26 and the half-cycle alpha 13 for every captured model/quad vertex; the zero phases emitted no body, armor, held or glint geometry. Additional checks verify model and item glint submissions and cap all additive RGB channels at the phase's intensity. Punchy's actual first-person entry retained two arm model submissions plus their two glint passes and item geometry at peak, and emitted none at zero. Verification pins the original input under `tmp/vanish-breathing/baseline/`, so the same script also accepts the installed tested candidate.

Six GPU screenshots were manually reviewed in `tmp/vanish-breathing/client/screenshots/`:

| Screenshot | View | Observed result |
| --- | --- | --- |
| `2026-10-29_16.49.51.png` | Third person, peak | Faint violet silhouette, armor and held equipment with subtle violet shimmer |
| `2026-10-29_16.49.53.png` | Third person, minimum | Body, armor, held equipment and shimmer disappear |
| `2026-10-29_16.49.54.png` | Third person, removed | Normal opaque body, four armor pieces, sword and shield |
| `2026-10-29_16.49.56.png` | Punchy first person, peak | Faint violet sword/shield/arms with native glint texture |
| `2026-10-29_16.49.57.png` | Punchy first person, minimum | Hands, equipped models and shimmer disappear |
| `2026-10-29_16.49.59.png` | Punchy first person, removed | Normal opaque sword, shield and hands |

Capture is scheduled on `RenderFrameEvent.Post`. Captures taken directly inside `ClientTickEvent.Post` read a cleared render target on this client and were rejected as black frames; they are not visual evidence. The final screenshots hold each demonstration phase briefly, while the renderer checks sample the actual 80-tick wave.

Limits: no public multiplayer server, shader pack, complete client-mod collection, external server or personal installation was tested. The isolated client covers the installed animation mods and relevant dependencies, standard diamond armor, sword and shield. Exotic modded geometry that bypasses Minecraft's submission collectors may require a separate integration. Gameplay classes and spell resources are byte-identical; this visual pass does not claim a new end-to-end gameplay mechanics test.
