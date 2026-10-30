# Accessory RPG Balance Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan directly. The owner requested immediate implementation and minimal delegation.

**Goal:** Keep every available Nameless/Artifacts accessory useful while bounding combat, healing and boss-health exploits.

**Architecture:** Reuse the existing two same-filename repack pipelines, numeric policy and item implementations. Add small server-safe proc helpers and narrowly replace existing handlers, keeping each mod independently loadable.

**Tech Stack:** Java 25, Minecraft 26.2, NeoForge 26.2.0.90, Curios 16.0.0+26.2, Python repack tools.

**Spec:** [Capped accessory scaling](../specs/2026-10-30-accessory-rpg-balance-design.md).

## Global constraints

Preserve the 12 removed relics, Vampire lock, size drawbacks, same JAR filenames and restored Feet slot. No personal-world edits, no extra dependency and no unsolicited commit/push. Existing unrelated documentation changes remain intact. Implementation follows the spec's exact item values.

## Review focus

- Two retaliators must never produce recursive damage or healing.
- Swapping stacks or reloading stale configs must not restore old power/cooldowns.
- Boss HP, absorption and overkill cannot inflate damage or life steal.
- Better Party and owned pets remain protected without requiring optional APIs to load.
- Updated archives preserve unrelated classes, resources and the Feet assignment.

### Task 1: Complete inventory and regression probe

Files: this plan; `docs/patches/accessory-rpg-balance/`; existing `nameless-balance/tests` and `artifacts-balance/tests` as references.

- [x] Generate one inventory row per 42 available Nameless / 45 wearable Artifacts, recording changed or retained effective details.
- [x] Write an isolated regression probe covering gravity -0.02625, stale policy, HP-independent capped damage, proc cooldowns, party/pet/recursion, healing and all retained guards.
- [x] Run the probe against current archives; expect old gravity/policy to fail before production edits.

### Task 2: Implement and verify bounded Nameless effects

Files: `docs/patches/nameless-balance/{balance.json,repack.py,src/}`; source handlers under the existing package tree; new shared `pack/AccessoryCombat.java` and damage resources.

- [x] Add exact spec overrides to `PackBalance` and policy; rebuild defaults consistently.
- [x] Replace affected damage/healing/economy handlers, preserve visuals and item guards, and add effect-specific wearer cooldowns.
- [x] Build using existing `tmp/nameless-balance/classpath.txt`; stage under ignored scratch only.
- [x] Run the isolated probe and retained-item coverage checks; inspect failures before installation.

### Task 3: Implement and verify bounded Artifacts effects

Files: `docs/patches/artifacts-balance/{balance.json,build.py,src/}`; `component/ability/retaliation/` replacements and narrow `ArtifactHooks.absorbDamage` patch.

- [x] Apply numeric overrides, targeted lightning and protected nonrecursive retaliation; replace only the life-steal method rather than rebuilding unrelated hooks.
- [x] Compile/repack against existing pinned upstream using `tmp/artifacts-balance/classpath.txt`.
- [x] Run combined isolated tests for both mods, damage sources, power caps, timers and Feet slot; verify ZIP CRC and preserved metadata/resources.
- [x] Back up active JARs to ignored retired storage and install verified same-name replacements in `mods/` and fixed packs.

### Task 4: Synchronize references and handbook

Files: both effect references and README files; local owner MD mirrors; `docs/MODLIST.md`, `README.md`, root handbook PDF; accessory audit hashes/results.

- [x] Replace obsolete effective descriptions with concise current percentages, actual caps and proc intervals; retain full item coverage.
- [x] Update canonical handbook accessory entries and rebuild the PDF with its existing builder.
- [x] Check links, inventory counts, `git diff --check`, audit checks and final archive hashes; visually inspect changed PDF pages and record runtime limits.
- [x] Report installed changes and test results. Leave Git synchronization for an explicit request.

## Execution record

Executed directly, as authorized, without another approval pause or Git synchronization. One focused read-only final reviewer was used. The baseline failed old Moon Stone gravity. The final isolated integrated/minimal runs passed 155/145 checks; see the combined record for exact scope and limits. Windows javac command length required an argument file. Optional RPG magic-tag recognition and stale modifier refresh were completed at the shared helper/handler layer. Review findings for Eclipse session lifetime, buffed wolf damage and persistent Creeper cooldown were fixed and covered by regression checks. Personal installations stayed untouched.
