# Wolf Armor Balance Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline.

**Goal:** Replace wolf armor immunity with balanced protection and support native Penchant Unbreaking.
**Architecture:** Narrow overlays of current Iron Wolf Armor, Companion bridge and Adventurer resources. Native damage/durability paths remain authoritative; a focused helper applies tier reduction and strips only body armor/toughness defaults.
**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90, Java 25 bytecode, ASM, Python.
**Spec:** docs/superpowers/specs/2026-10-26-wolf-armor-balance-design.md

## Global constraints
Same active filenames; preserve existing progression/policies and unrelated ZIP entries. No personal worlds, commits or push. Concise descriptions use percentages.

## Review focus
Vanilla scute armor must not remain an immunity loophole. Normal defenses must not double count the item. Bypass damage retains native eligibility. Unbreaking must reduce wear without increasing HP protection. Old saves/node allocations must load without refunds.

### Task 1: Exact inputs and regression fixture
- [x] Snapshot current archives, inspect native wolf and Penchant wear/progression.
- [x] Add an isolated server probe that fails on full absorption and missing enchantment compatibility.

### Task 2: Armor and skill behavior
Files: docs/patches/wolf-armor-balance/{WolfArmorBalance.java,MixinWolf.java,PatchWolf.java,build.py,verify.py,WolfProbe.java,run_server.py}.
- [x] Disable full absorption only for supported armor; remove normal item armor/toughness defaults and apply tier reduction once, preserving bypasses and durability.
- [x] Update Companion map, cap percentage resistance at 25%, update matching tree labels and specialization descriptions.
- [x] Add durability and armor enchantable tags; verify native Penchant curve and progression on equipped wolves, including module-off.
- [x] Test real hits, tiny hits, broken/unequipped armor, pet nodes, bypass sources, existing owners and ordinary non-wolf combat.

### Task 3: Review, install and document
- [x] Run candidate and module-off regression checks; review exact changed entries and focused code risks.
- [x] Install exact tested archives to active/fixed copies, archive originals ignored.
- [x] Update patch notes, handbook and owner reference; rebuild/render/review PDF and verify matching copies.
- [x] Report observed results and limits; leave changes uncommitted.
