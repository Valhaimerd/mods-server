# Shield blocking pose compatibility

24 October 2026. Same-filename client-only Punchy 2.8f repack, Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 bytecode. Extends the existing RPG/Spell Engine handoff; no new animation asset is created.

## Root causes and behavior

Paladins' iron, golden, diamond and netherite kite shields are ordinary `Item` instances carrying `BLOCKS_ATTACKS`, rather than subclasses of `ShieldItem`. Their native blocking state and damage reduction worked, but Punchy's old checks skipped its shield state machine, treating their use as `interact`. A connected baseline observed `use_shield` on the vanilla shield and `interact` on the blocking kite shield.

Fourteen old shield-class tests across seven Punchy classes now also recognise the blocking component. This connects the existing shield clip, equipment/hand classification, impact-hand selection, and use-property preservation to those shields. Existing custom shield subclass behavior remains supported.

Visual review found an additional first-person problem: Punchy's shield grip lowered these shields out of view. The actively used blocking hand now temporarily yields to the existing native raised block pose in first person. It resumes Punchy rendering on release; the opposite hand and Spell Engine's existing handoff are preserved. In third person Punchy's original `use_shield` clip remains active.

Minecraft's native first-person `BLOCK` branch also tests `ShieldItem` and otherwise applies a generic blocking-item tilt. One client mixin redirects only that branch's type-test input for component shields, selecting the existing shield transform. The actual stack, model, textures, custom data and rendered item identity are untouched. It does not reclassify swords or alter nonblocking hands.

No damage, shield activation delay, durability, cooldown or network rule changes. All animation JSON, item models, textures, metadata and the RPG handoff helper remain byte-identical to the installed input. Client-only `mod store/` placement is retained.

## Build and verify

```powershell
python docs/patches/shield-pose/build.py
python docs/patches/shield-pose/verify.py --candidate
python docs/patches/asgardian-expansion/run_client.py --shield-only
# Wait for the client result, then preserve its result, measurements and tested hash under tmp/shield-pose/.
python docs/patches/shield-pose/verify.py --candidate --runtime
```

The pinned input is the already repaired pack Punchy archive at `tmp/shield-pose/before-punchy-2.8f-neoforge-26.2.jar`. Rebuilding layers on that pinned input, preserving preceding matrix, compatibility-default and spell-hand fixes. `artifacts.json` records exact input/output hashes, patch counts and production source hashes. Candidates stay in ignored `tmp/shield-pose/built/` until tested.

The native mixin's ordinal is specific to Minecraft 26.2's third `ItemStack.getItem()` call in `submitArmWithItem`, used solely by its shield guard. It has `require=1` and must be reviewed on Minecraft updates. Archive verification checks exact changed entries, CRC, Java class versions, source hashes and absence of probes.

The focused client exercises native vanilla/component-shield use, held blocking state, native first-person handoff, existing third-person clip selection and release. Screenshots are reviewed in daylight. This is not a claim that every modded shield, shader or remote third-person animation combination was exercised. The prior server shield rules remain unchanged.

The tested same-filename replacement goes to `mod store/` and `mod fixing/1 fixed packs/`. The old archive is retained under ignored `mod fixing/retired packs/shield-pose-2026-10-24/`. No personal Minecraft profile or desktop server is changed. No commit or push is part of this repair.
