# Bifrost native skill hotbar - 25 October 2026

Bifrost is now a native Spell Engine active skill appended after existing weapon, book/class and active accessory skills. Opening destinations is instant with a zero-second spell cooldown. The existing three-second teleport charge and five-second travel cooldown are unchanged. The initial user-supplied Rainbow Rune Portal Gateway PNG was the skill icon; the [visual follow-up](../bifrost-melee-visuals/README.md) replaces it with Pixel Rainbow Portal Vortex.

Hofund now matches `paladins:diamond_claymore` attributes and the native two-handed claymore combat profile, granting Flurry. Mjolnir retains Netherite Mace/Smash; Stormbreaker retains Netherite Great Hammer/Ground Slam and 0.8 attack speed. The prior long-handle/model repair remains.

## Inputs and ordering

The normal weapon skill remains available on right-click under the native use-key setting. Bifrost is an extra skill at the end. Example: right-click weapon skill, class skills 1-3, active trinket 4, Bifrost 5. Removing/equipping skills changes its position; switching to an ordinary weapon removes it. Number bindings come from the existing skill-key preferences; no options file or fixed physical key is rewritten. If all numeric slots are already occupied, this pass does not overwrite an existing skill to make space.

Native Spell Engine consumed the first numeric index even when showing its weapon spell on right-click, producing `Right Button, 2, 3, 4, 5, 6`. A client-only Bifrost hotbar mixin compacts this case to `Right Button, 1, 2, 3, 4, 5` for these three weapons. It leaves ordinary loadouts and configurations without a leading use-key spell alone. Explicit user bindings are still read from the native wrappers. Native shield item-use precedence is retained.

Right-click in air no longer opens Bifrost or channels a custom beam/slash. It passes to the native weapon skill. Marker right-click remains available. The old Asgardian Ability key is not registered.

## Dormant custom features

The old selector, attacks, beam/slash channels, throws/ricochets and healing passives remain in source for later redesign but cannot be activated. Guards reject the old network intents and direct action/throw entry points. Ability ticks and passive event bookkeeping are disabled. Saved ownership, claims, protected weapon death recovery, recipes, marker data and transit remain intact; original items and registry IDs are not removed. Existing saved cooldown data is not erased.

[Bifrost Skills.md](../bifrost-weapon-alignment/Bifrost%20Skills.md), mirrored in `mod fixing/Bifrost Skills.md`, distinguishes the current Bifrost-only state from future abilities. Its future slot order starts with Bifrost, then each weapon's custom skills; no future combat spell or activation is added here.

## Implementation

`BifrostSkills` registers a native custom-delivery handler. `data/bifrost/spell/travel.json` is a real active spell with no cast time, durability cost or cooldown. A narrowly scoped container-update hook appends the spell and its held-weapon source after native merging, preserving all earlier skills, passives and modifiers. An already-correct tail entry returns immediately, avoiding repeated allocation or duplicate skills. Native casting, cast options, client-server sync, icon lookup and hotbar input handle activation; no new input packet or tick listener is introduced.

The handler validates the current held weapon, Asgardian eligibility, owner and availability before opening the existing server-driven destination screen. It does not reject its own instant spell process as a conflicting cast. Native cast restrictions still apply. Client rendering code is listed only in the mixin configuration's `client` section. The source's full expansion builder is updated to preserve that side split.

## Rebuild and verification

```powershell
python docs/patches/bifrost-skill-hotbar/build.py
python docs/patches/bifrost-skill-hotbar/run_client.py
python docs/patches/bifrost-skill-hotbar/verify.py --candidate --runtime
python docs/patches/bifrost-skill-hotbar/verify.py --runtime
```

Build layers over the pinned pre-pass repaired Bifrost archive in `tmp/bifrost-skill-hotbar/before-*`; it does not install automatically. Preserve that source and do not rerun this historical overlay over future changes without review. `artifacts.json` records hashes and exact changed/added entries. Production sources remain under `docs/patches/asgardian-expansion/src/`; current source resources are synchronized there. The exact tested same-filename archive is installed in `mods/` and `mod fixing/1 fixed packs/`, with predecessors in ignored `mod fixing/retired packs/bifrost-skill-hotbar-2026-10-25/`.

The baseline fails because no native Bifrost spell exists. The replacement passes **42 connected-client/integrated-server checks** on Minecraft 26.2 / NeoForge 26.2.0.90, with Spell Engine 1.10.5, RPG modules 3.1.2, Better Combat 3.2.2, Punchy 2.8f and Player Animation Library 1.2.8. Checks cover all three weapon sources, single tail entry, source removal/re-equipping, book/accessory ordering, exact claymore attributes/profile, zero cooldown, server race denial, rejection of all four old intent actions, disabled primary/selector/owned throws, retained owner claims, native cast-to-menu and actual numeric hotbar activation. Book and active accessory entries are fixture sources through the native container API, not a full live Curios equipment matrix.

The final in-game capture visually confirms the supplied portal icon on skill number 5. A rebuilt handbook page is visually checked; total pagination remains 77 pages. This was not a new dedicated-server smoke run or full multiplayer/shader/teleport stress test. Transit implementation and timings were preserved byte-for-byte. No personal Minecraft installation, external server or world was changed. Nothing was committed or pushed.

The [25 October visual repair](../bifrost-melee-visuals/README.md) also enlarges Stormbreaker, fits its inventory model and adds exact Punchy resource mappings. Historical output hashes below this layer are superseded by that overlay.
