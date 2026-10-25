# Warrior ultimate armor tint and Last Stand gold glint

26 October 2026. Same-filename Spell Engine overlay over [Berserker appearance](../berserker-visuals/README.md). Combat behavior, items and enchantments are unchanged.

- **Berserker:** retains the fixed 50% red model tint and white sparkles. The existing Spell Engine equipment submission tint also colors worn armor, including dye-color composition. No second tint is applied on top of the armor hook.
- **Last Stand:** fixed 30% gold model/equipment tint, blending neutral toward `#FFD700`; native tint multiplier is `#FFF3B3` with full opacity. It does not change armor transparency.
- A separate scrolling gold glint pass uses Minecraft's enchantment glint pipeline, armor glint texture transform and view-offset layering. A recolored copy of the native armor-glint pattern supplies yellow/gold highlights. This is a model reflection sheen, not sparkling particles or a real world-light source.
- Gold glint is submitted for the affected character and armor models, including unenchanted armor. Native purple glint is suppressed only for models under Last Stand, so it does not clash with the yellow effect. Item enchantment data, inventory glints and unrelated entities remain unchanged.
- Last Stand's previous custom particle emitter is disabled. Berserker's separate particle emitter is preserved.
- The gold pass follows the synchronized Last Stand status captured into each living-entity render state. Ending, dispelling, expiring or death-clearing Last Stand clears that render-state flag; no delayed visual tasks survive the buff. Normal synchronization latency still applies. Both buffs keep their existing composition if simultaneously active.

## Implementation

`LastStandAppearance` registers the tint through the current common Berserker appearance initializer. Two client-only mixins capture Last Stand into render state and append a guarded gold model pass at the native model submission layer. The gold pass excludes itself to prevent recursion and excludes native glint submissions. Worn armor retains its native dye and equipment rendering path. Custom mod renderers that bypass Minecraft's model submission API are outside the isolated test's coverage.

## Build and proof

```powershell
python docs/patches/ultimate-armor-visuals/build.py
python docs/patches/ultimate-armor-visuals/run_client.py
python docs/patches/ultimate-armor-visuals/verify.py --installed --runtime
```

`artifacts.json` pins the current input/output hashes, exact changed/added entries and source vanilla-glint hash. Inputs/candidates stay under ignored `tmp/ultimate-armor-visuals/`. The texture generation only recolors the native glint's pixels, preserving its seamless pattern. The common tint helper has no client references; all render mixins are in the mixin configuration's client list.

**57 connected-client checks and 2,156 archive preservation checks passed.** The disposable connected client uses iron armor to verify Berserker's red armor tint, Last Stand's fixed gold tint, dye composition, native armor submission/glint routing, suppression of purple glint, cleanup on removal/expiry/death and no glint leakage to another render state. Screenshots compare active red armor, active gold armor and normal armor. Archive verification checks unrelated entries byte-for-byte and validates gold texture channels. Exact results and tested hashes are alongside this file. Personal installations and worlds are untouched.

The matching same-name active/fixed archive replaces the current Spell Engine contents; predecessors are retained in ignored `mod fixing/retired packs/ultimate-armor-visuals-20261026/`. The handbook and Warrior reference are synchronized. No commit or push was requested.
