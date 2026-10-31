# Pending Unbreaking progression repair

27 October 2026. Same-filename overlay for `penchant-neoforge-26.2.0-1.6-2.5x-progress.jar`. Install matching client/server builds and restart. Personal instances/worlds and the separate desktop server were not modified. No push requested.

The installed base and Durability Rework definitions already permit Unbreaking X. Penchant only reconciles earned progress during a progression event, so saved bars that meet a changed threshold can remain full at V until another qualifying event. The tooltip clamps displayed progress to its target, hiding any excess. A native baseline reproduces a saved full V bar remaining V after its ordinary inventory tick.

A new common ItemStack inventory-tick mixin checks once per second on server-player carried/worn gear. If Unbreaking is below X and its stored progress meets the native next-level requirement, it calls the existing progression path with **zero additional progress**. Earned progress is consumed normally, native level-up synchronization/FX are retained, and partial bars do not level for free. Existing equipment is not reset or automatically granted X. New wear progression continues normally through VI-X; ordinary X gear remains breakable. The normal native reconciliation may also settle other already-complete enchantment bars on the same item, exactly as a wear event would.

Only the common mixin list changes and one class is added. All pre-existing classes and resources remain byte-identical, including Unbreaking definitions/curves, 2.5x progress targets, armor/enchantment retirement policies and owned-wolf progression. No new dependency or configuration is introduced. The separate retired-enchantment progress codec remains unchanged.

## Reproduce and evidence

```powershell
python docs/patches/unbreaking-progression/build.py
python docs/patches/unbreaking-progression/run_server.py --baseline
python docs/patches/unbreaking-progression/run_server.py
python docs/patches/unbreaking-progression/run_server.py --module-off
python docs/patches/unbreaking-progression/verify.py --installed
```

The baseline fails `existing full V progress reconciles to VI`. Candidates pass **40 native checks with Durability Rework enabled and 40 disabled**, covering registered/progression X caps, actual full-V inventory reconciliation for a pickaxe and chestplate, earned-progress consumption, partial bars, native wear progression through VII-X, stopping at X, real reduced durability wear/eventual breakage and explicit Unbreakable components remaining respected. Java-25 classes and NeoForge 26.2.0.90 run with `-Xverify:all` in fresh disposable worlds. The installed Iron Wolf Armor and its dependencies are included because the current Unbreaking tags reference its armor tag. This is isolated native server verification, not a connected-client/full-pack playthrough or inspection of the user's saved item.

**446 pre-existing archive entries remain unchanged.** `artifacts.json` pins exact inputs/outputs; tested dependency hashes and runtime records are adjacent. Ignored inputs/candidates/worlds are under `tmp/unbreaking-progression/`; predecessor active/fixed copies are under `mod fixing/retired packs/unbreaking-progression-20261027/`. Active and fixed copies match. Handbook/PDF and pack notes are synchronized.

The [31 October VIII-to-X audit](../rpg-netherite-repairs/README.md) confirms this hook advances complete VIII and IX bars on worn RPG armor through native equipment ticks, including a connected real-player/client tooltip check. The personal client already contains this repair; a separately inspected older desktop-server archive does not. Keep the current Penchant build on both sides and restart the server. The remote server behind the reported multiplayer connection has not been identified, so the specific live stall remains unconfirmed.

PDF review retained 77 pages and all 51 correct contents destinations. Both output copies match. All pages were rendered for overview review; changed page 10 was inspected at reading size without new clipping or altered pagination.
