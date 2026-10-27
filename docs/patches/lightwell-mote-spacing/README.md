# Lightwell mote spacing

27 October 2026. Same-filename Spell Engine 1.10.5 overlay over the current pack, including the pending [support/visual tuning](../support-visual-tuning/README.md). Use matching client/server JARs and restart. No push requested.

When both motes target the same ally, the first launches immediately and the second launches **six server ticks (0.3s) later**. This fixed presentation offset does not scale with haste. When two different allies are selected, both projectiles still launch together. The existing 0.8s firing cycle before inherited haste, two-mote count, speed, Healing Power, targeting priorities, cleanse and heart-stack rules remain unchanged. Healing the target to full with the first mote does not invalidate the second.

The native world scheduler launches the pending shot from the well's current position, aimed at the same recipient. It checks that the original well, owner and recipient are still alive/present and in the same world; the recipient must remain allied, within the existing 16-block range and visible to the well. Removed/expired wells and removed/dead recipients cannot leave delayed projectiles behind. No second firing loop or persistent timer map is introduced.

## Reproduce and verification

```powershell
python docs/patches/lightwell-mote-spacing/build.py
python docs/patches/lightwell-mote-spacing/run_probe.py
python docs/patches/lightwell-mote-spacing/verify.py --installed
```

Baseline runtime failed the new immediate-launch check: two projectiles appeared instead of one. The candidate passed **708 isolated Priest/party checks**, including exact six-tick scheduling, first/second actual projectile creation, unchanged recipient, simultaneous distinct-target shots, removed-target cancellation and removed-well cancellation. Existing Priest/party, Barrier, Sacred Refuge, gold-tint and absorption checks remain. Java-25 classes, NeoForge 26.2.0.90 and `-Xverify:all` ran in a fresh disposable world. This verifies server projectile timing, not a fresh connected-client visual playtest.

**2,172 unchanged archive entries** are verified byte-for-byte. Only `net/spell_engine/internals/PriestTuning.class` changes; current support/visual/passive overlays, spell resources, cooldowns and unrelated mechanics remain. `artifacts.json` records exact input/output hashes; detailed runtime records are adjacent. Inputs/candidates/server remain in ignored `tmp/lightwell-mote-spacing/`; predecessors are under ignored `mod fixing/retired packs/lightwell-mote-spacing-20261027/`. Active/fixed copies match. Personal installations/worlds are untouched. Priest reference and handbook/PDF are updated.

PDF review retained 77 pages and all 51 correct contents destinations. Both output copies match. All pages were rendered for overview review, with changed page 17 inspected at reading size without new clipping or altered pagination.
