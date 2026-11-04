# Being of Purelight cooldown and radiant appearance

4 November 2026. Focused overlay over the installed phases 7-9; the Hydra fix and other classes remain intact. Reuses the existing passive ID `skill_tree_rpgs:paladin_tier_3_passive_2` and active state `skill_tree_rpgs:purelight_cost`.

- **80s base cooldown**, affected by healing-school haste like the other Paladin passives. Starts on successful rescue, not when the continuing cost ends. Earlier lifesavers/totems do not spend it.
- The cooldown is saved, mirrored in the native cooldown display and copied on player recreation/death. Downing, ending the cost or respec does not reset it. The active cost still prevents another rescue even if the cooldown finishes.
- While active, the character and equipped armor become a **100%-opacity, full-bright gold-white silhouette with animated golden glint**. The emissive pass remains bright at night; it does not require the Glowing status or grant through-wall targeting outlines. Shader bloom and actual light cast onto surrounding blocks depend on the renderer and are not added here.
- The effect follows the synchronized active status and clears when the state ends. Ordinary armor visuals and existing Warrior glints remain outside this state.
- Full-heal rescue, earlier-lifesaver priority, persistent 1 HP/0.5s absorption-bypassing drain, and downing/death cleanup remain unchanged.

Only Spell Engine and Skill Tree receive same-name replacements. Original copies are backed up before installation. Exact changed members/hashes are in `artifacts.json`; isolated runtime results are in `candidate-checks.txt` and `client-checks.txt`.

```powershell
python docs/patches/purelight-glow/build.py
python docs/patches/purelight-glow/run_probe.py
python docs/patches/purelight-glow/run_client.py
```

Tests use isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 fixtures. No handbook/PDF rebuild, personal-world/server modification, commit or push.

Validation: **14 native server checks, 5 connected-client checks and 34 artifact/install checks passed**. Night preview reviewed. Exact tested replacements are installed in `mods/` and `mod fixing/1 fixed packs/`; restart clients and server with matching JARs.
