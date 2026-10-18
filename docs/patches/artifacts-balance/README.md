# Artifacts pack balance — Minecraft 26.2

Same-filename replacement for `mods/artifacts-neoforge-15.1.3+26.2.1.jar`.
Upstream: [Artifacts](https://github.com/ochotonida/artifacts), MIT, commit
`803fd1aea1cece1362d40ba626c64cc1c3cdabb7` (26.2 / 15.1.3).
The upstream license is preserved in `LICENSE.upstream` and in the archive.

## Effective pack values

| Artifact | Effective change |
| --- | --- |
| Cross Necklace | Extra invulnerability remains 20 ticks; cooldown **5 seconds**. |
| Panic Necklace | **Speed I, 5 seconds**, cooldown **7 seconds**. |
| Shock Pendant | Cooldown **3 seconds**; chance/immunity retained. |
| Thorn Pendant | Cooldown **3 seconds**; damage/chance retained. |
| Charm of Shrinking | Half size retained; **×1.2 movement speed** and refreshing **Weakness I, 60 seconds** while active. |
| Obsidian Skull | **Fire Resistance II**; 30-second duration and 60-second cooldown retained. Level II has no extra vanilla fire-protection benefit over level I. |
| Crystal Heart | **+8 maximum health / +4 hearts**. |
| Feral Claws | **+15% base attack speed**. |
| Vampiric Glove | **10% melee life steal**, maximum **4 health / 2 hearts per hit**; 100% chance retained. |
| Running Shoes | **+20% base movement speed while sprinting**; step height retained. |
| Power Glove | **+2 flat attack damage**. |
| Pocket Piston | **+0.3 attack knockback**. |
| Steadfast Spikes | **+0.5 knockback resistance**; slip resistance retained. |

No extra life-steal cooldown, reduced Cross Necklace invulnerability, or new
duplicate-artifact restriction is included. Other item settings remain configurable.

## Implementation and distribution

`ConfigValue` enforces the 18 approved/explicitly retained `items` config keys in `balance.json`,
both as defaults and on reads. Existing configuration values and network updates
cannot restore stronger values for those keys. Unrelated config keys are untouched.
The physical TOML may still contain an older value; the effective value is the pack
policy. Restart clients and servers after replacing the archive.

`ModItems` adds the movement modifier and Weakness equipment component to the
shrinking charm, and changes the skull's effect level. Shrinking uses Artifacts'
native toggle, attribute, effect ticker and removal handling. Equipment effects
include the native **19-tick refresh padding**: 60 seconds produces 1,219 ticks,
and 30 seconds produces 619 ticks. Native effect cleanup runs on unequip; no
separate Nameless Trinkets-style lingering-debuff handler is added.

The installer already compares Git content hashes and delivers this changed JAR
without a filename/version change. No installer rebuild or config installation is
needed. Both client and server receive the same replacement. Personal Minecraft
folders and the separate desktop server were not changed.

## Rebuild

1. Preserve the original archive as `tmp/artifacts-balance/upstream.jar`, SHA-256
   `3be92116769cbba850acfc0e03bd0f3688c459e2b17144d97da36679fe35c104`.
2. Supply a UTF-8 file containing a semicolon-separated compilation classpath:
   NeoForge-patched Minecraft 26.2 first, upstream Artifacts, NeoForge/FML, Curios,
   and the libraries used by the upstream classes. This pass compiled against
   the cached NeoForge 26.2.0.88 development artifact, Java release 25.
3. Run `python docs/patches/artifacts-balance/build.py --classpath-file <file>`.
   Java 25+ and Python are required; `--java-bin` selects the JDK bin directory.
4. Verify the output under `tmp/artifacts-balance/`, then replace the active JAR
   under its existing filename. Do not ship test probes or intermediate classes.

The build verifies the upstream hash, replaces exactly two classes, preserves all
other archive entries, and adds the numeric policy as `artifacts-pack-balance.json`.
Source edits must stay aligned with `balance.json`; the runtime probe checks each
effective/default value and each override after writing a stale value.

## Verification

Isolated dedicated server: **Minecraft 26.2 / NeoForge 26.2.0.90**, Artifacts
**15.1.3+26.2.1**, Curios **16.0.0+26.2**, Java **26**, local test mod only.
The unmodified archive failed on Cross Necklace's old cooldown. The replacement
passed **64 checks** covering all 18 values, stale-config writes, defaults,
unrelated configuration mutability, charm scale/speed component operation,
Weakness level/duration and active/disabled/cooldown ticking, and skull effect.

Probe source: `tests/ArtifactsProbe.java`. Test logs/results stay ignored under
`tmp/artifacts-balance/`. This is focused server verification, not a full client,
tooltip, combat-balance, or multiplayer playtest. Retained values use upstream
mechanics; no claim is made that melee/ranged/caster outcomes are now equal.
