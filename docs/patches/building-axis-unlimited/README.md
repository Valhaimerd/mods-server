# Sophisticated Building: axis-only cap removal

25 October 2026. Same-filename replacement for `sophisticatedbuilding-neoforge-26.2-5.1.0.jar`. Install matching client/server copies and restart. Based on the exact installed archive; [upstream source](https://github.com/navrelis/EffortlessBuildingSophisticated).

No separate axis-length cap remains on any power tier or Creative. Existing batches remain 128 / 256 / 512 / 2,048 in Survival, and 10,000 in Creative, subject to existing server settings. Start reach, mirror radius, materials, block permissions, world bounds and undo behavior remain. This is not unlimited blocks per click.

The shared axis getters now return an unrestricted integer sentinel. Native shape previews, server extent validation and array offsets therefore stop using the old 8 / 16 / 24 / 32 caps. Server distance arithmetic handles that sentinel without overflowing. Radial and array labels show **Unlimited**. The old axis config entries are superseded; batch and reach config/resources and native server placement code remain unchanged.

## Verification

The baseline native preview stopped at eight for a requested 32-block line. The final isolated Minecraft 26.2 / NeoForge 26.2.0.90 connected client and integrated server passed **31 checks**: positive/negative previews on all three axes; a 200-block preview capped at 128; all power tiers and Creative; full-world-span arithmetic; sparse array offsets; Unlimited labels including the native array entry; server batch truncation and distant-start rejection; actual placement of 32 Survival blocks consuming 32 materials; and queue cleanup.

The final run used a fresh disposable world. A repeat against an already mutated test world produced no placement, so retained-world repeatability is not claimed. Huge geometric selections still have native preview-enumeration costs; batch caps limit placement rather than promising unlimited preview performance. Personal installations and worlds are untouched.

```powershell
python docs/patches/building-axis-unlimited/build.py
python docs/patches/building-axis-unlimited/run_client.py
python docs/patches/building-axis-unlimited/verify.py --installed
```

Five existing classes receive narrow ASM edits; one stateless helper is added. Other archive entries are byte-identical. Hashes and entry changes are in `artifacts.json`; the tested hash matches active/fixed copies. Predecessors remain under ignored `mod fixing/retired packs/building-axis-unlimited-2026-10-25/`. Disposable clients remain under ignored `tmp/building-axis-unlimited/`. No push requested.

## Later combat-limits overlay

The [PvP combat-limits patch](../pvp-combat-limits/README.md) extends this artifact without changing its preceding balance rules. Use that patch?s final artifact verification for the currently installed JAR; this page records the earlier baseline.

The [31 October reach follow-up](../building-reach/README.md) additionally sets placement reach to 1,000 blocks at every tier. It supersedes this page's historical statement that starting reach is unchanged, while preserving the unlimited axis policy, batches and later combat guards.
