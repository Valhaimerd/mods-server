# PatPat Ctrl + right-click and working rebinding

25 October 2026. Resource version and active filename remain `PatPat-1.3.2+26.2+neoforge.jar`. Only `net/lopymine/patpat/client/keybinding/PatPatKeybinding.class` changes; server logic, target eligibility, networking and animations are unchanged.

## Player behavior

- Default and Reset: **Left Ctrl + right-click** with an empty main hand.
- Plain right-click retains normal creature use, including horse riding/taming. No horse-specific mounting rules are modified.
- Controls changes, unbinding and the installer-recommended `options.txt` binding now update the actual PatPat combination, and survive restarting.
- Existing saved choices are retained until changed. After installing the patched JAR, use Controls -> Pat Entity -> Reset, or apply the updated Recommended Keybinds profile. This patch does not force a preferred key over later user edits.

The existing PatPat target handler consumes Use when its combination is active and a supported target is under the crosshair. The new modifier prevents ordinary right-click from entering that path. Existing non-target interactions remain governed by Minecraft and the other mods.

## Cause and fix

PatPat maintained a separate `PressableKeybindingCombination` from `config/patpat/patpat/patpat-client.json5`. Its override of `setKey` only accepted the dummy default (`UNKNOWN`), and its constructor used that dummy as the native key. NeoForge's options loader calls `setKeyModifierAndCode`, which updated the native key/modifier but left PatPat's separate active combination and display unchanged. Thus changing only the options key or generic key mapping could leave right-click as the actual pat input.

The patched class uses a real native Ctrl + right-mouse default, synchronizes options/native changes into its existing combination, and synchronizes custom Controls capture back into the native key/modifier before saving. Reset's direct `setKey(defaultKey)` path explicitly restores the default modifier. Arbitrary non-default keys and Unbound are accepted. The existing custom combination capture remains in place; no global input hooks are added.

`installer/recommended-keybinds.txt` now contains:

```text
key_patpat.keybinding.pat:key.mouse.right:CONTROL
```

Installer v1.4.6 already downloads this profile and supports the `CONTROL` suffix. Its executable/version does not need rebuilding. Its existing merge test now exercises this exact Ctrl + mouse combination. The published profile will change only when these repository changes are pushed; no release or push is part of this request.

## Verification

```powershell
python docs/patches/patpat-controls/build.py
python docs/patches/patpat-controls/run_client.py --baseline
python docs/patches/patpat-controls/run_client.py
python docs/patches/patpat-controls/verify.py --installed
dotnet run --project installer -- --self-test
./docs/tests/Test-HandbookArtifacts.ps1
./docs/tests/Test-ModIconSources.ps1
```

The isolated Minecraft 26.2 / NeoForge 26.2.0.90 client includes the exact installed Controlling and Searchables versions. It starts with a right-click-only PatPat config and a Ctrl + right-click options recommendation, then checks active-combination updates, input matching, direct/native key changes, custom Controls capture, Unbound, Reset and native options save/load. A second client process verifies a changed key persists across restart. The baseline fails the active-combination synchronization check. See the adjacent result/check files and exact test JAR hashes. This is a scripted native-client keybinding regression test, not a full-pack manual horse-taming session.

The initial test fixture omitted the options data version, allowing Minecraft's old-options migration to rewrite bindings. The final fixture specifies the current data version. An intermediate candidate also exposed a missing synthetic enum-switch helper; the final source uses direct enum comparisons instead and changes only one archive class. Neither intermediate was installed into the active pack.

The final same-filename JAR is copied to repository `mods` and ignored fixed packs. Its predecessor is preserved under `mod fixing/retired packs/patpat-controls-2026-10-25`. The handbook/PDF and recommended profile are updated. Personal installations, separate servers and unrelated pending Pet Vault changes are preserved. No push requested.
