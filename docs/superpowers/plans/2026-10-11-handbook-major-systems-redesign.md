# Handbook Major Systems Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorder and expand the player handbook so the RPG comes first and the pack's major exploration, collection, equipment, and companion systems are easy to find and understand.

**Architecture:** Keep the existing five-part handbook and visual system. Add a compact RPG-first major-systems route to Quick Reference, promote detailed canonical guides within Parts II and III, and keep cross-references, indexes, source lists, and PDF navigation synchronized. Leave lower-priority mod summaries concise.

**Tech Stack:** Markdown, existing PowerShell handbook maintenance/build scripts, the current HTML/CSS print pipeline, Microsoft Edge or Google Chrome for PDF output and visual review.

**Spec:** `docs/superpowers/specs/2026-10-11-handbook-major-systems-redesign-design.md`

## Global Constraints

- Target remains Minecraft 26.2 with NeoForge 26.2.
- Preserve the current cover, five-part visual structure, typography, color hierarchy, entry/icon treatment, keybind table format, and one-page Table of Contents.
- Keep all complete category-ordered keybind tables together at the beginning of Part I.
- The reading order is RPG first, then exploration.
- Give one canonical detailed guide to each player-facing mod; cross-link other mentions.
- Treat the installed pack and its in-game behavior as authoritative for pack-specific controls, configuration, and mechanics.
- Keep support APIs out of the gameplay catalog; explain Curios as a player-facing equipment system without listing the API as new content.
- Keep narrow equipment additions, small structures, and cosmetic entries concise.
- Do not stage or change unrelated JARs in `mods/` or `mod store/`.

## Review Focus

- **Penchant configuration:** current world datapacks may change which mechanics are enabled. Verify the installed world and packaged guide before describing defaults or progression.
- **Pet Vault locket access:** local handbook guidance and the currently published upstream page differ. Test whether the installed pack requires the Curios necklace slot, inventory, or both, then use that one verified behavior everywhere.
- **Curios vanity behavior:** confirm the purple cosmetic layer, functional slots, hat/head/face placements, and how the ribbon toggle changes the UI before writing user instructions.
- **Source and icon accuracy:** do not imply upstream support for this pack's local ports or use an approximate Penchant icon; verify the current exact source and image permissions.
- **PDF navigation:** moves can change anchors and pagination. Verify internal links, alphabetical/tag indexes, TOC page numbers, screenshot references, and printed layout after the build.

---

### Task 1: Connect RPG, Curios, and Penchant as one equipment/progression story

**Files:**
- Modify: `docs/MODLIST.md` — Parts I and II, alphabetical index, gameplay-tag index, and equipment/enchanting source lists.
- Modify: `docs/assets/mod-icons/manifest.csv` only if a verified Penchant project icon can be used under the existing policy.
- Modify: `docs/assets/mod-icons/README.md` only if the catalog/icon count changes.
- Modify: `docs/assets/screenshots/README.md` if a new Curios or Penchant screenshot is needed.
- Create: real screenshots in `docs/assets/screenshots/` only when the current game UI teaches the guide materially better than prose.

**Interfaces:**
- Consumes: existing RPG, equipment, and enchanting sections in `docs/MODLIST.md`.
- Produces: a coherent progression/loadout explanation and a canonical Penchant entry in the enchanting topic.

- [ ] **Step 1: Verify the installed Penchant 26.2 JAR metadata and packaged Patchouli guide.** Compare the local behavior with the [NeoForge port README](https://github.com/WoXayZ/PenchantNeoforged); label only the behavior verified for the installed pack.
- [ ] **Step 2: Expand the RPG progression flow in Parts I and II.** Connect persistent race choice, flexible class loadouts, the `T` and `G` progression screens, spells/runes, combat, stats, and gear without repeating the same full explanation in multiple sections.
- [ ] **Step 3: Add a Curios loadout/cosmetics subsection under equipment.** Verify `Ctrl + G`, functional versus vanity slots, the purple cosmetic state, the ribbon control, and the current supported head/face/hat combinations in-game. Explain which mods supply the equipped items and bonuses.
- [ ] **Step 4: Add a dedicated `Penchant` profile to the enchanting topic.** Cover its verified core loop, its connection to current enchanting stations and equipment, what the player should inspect in the installed world, and any controls or configuration confirmed in-game. Do not present the unofficial port as an upstream release.
- [ ] **Step 5: Add Penchant to the alphabetical index, relevant gameplay tag, source list, and icon manifest if an exact permitted icon exists.** Update icon/catalog totals only after confirming the final entry set.
- [ ] **Step 6: Review link targets and icon rendering.** Run the existing alphabetical-index and internal-link checks; expected result is one Penchant entry with a valid destination and no duplicate or broken links.

### Task 2: Promote navigation and Alex's Caves within Part III

**Files:**
- Modify: `docs/MODLIST.md` — Quick Reference links, Part III order, World Map profile, Alex's Caves profile, source list, and TOC.
- Modify: `docs/assets/screenshots/README.md` if a real Alex's Caves capture is required.
- Create: a real Alex's Caves screenshot only if a current pack capture is available and useful.

**Interfaces:**
- Consumes: the existing travel and world-expansion sections in Part III.
- Produces: an early Part III navigation guide and a fuller Alex's Caves discovery/exploration guide.

- [ ] **Step 1: Reorder Part III's opening around expedition preparation and discovery.** Put navigation and the integrated world expansions ahead of generic bosses, structures, and creature lists while keeping the part's existing styling.
- [ ] **Step 2: Promote Xaero's World Map into a dedicated icon-backed profile.** Explain explored versus unknown terrain, route planning, useful map actions, and how map use fits an expedition; verify the active `M` binding in the current pack.
- [ ] **Step 3: Expand Alex's Caves from its short overview into a field guide.** Use the installed Cave Compendium and verified current pack behavior to describe discovery, preparation, cave-specific mechanics/rewards, and what a player should do on arrival. Keep exhaustive mob/block lists out.
- [ ] **Step 4: Keep the other world and dimension summaries at their current scope.** Retain their current entry structure and cross-links unless an installed-pack check shows that another system is also a core gameplay loop.
- [ ] **Step 5: Verify the moved World Map/Alex's Caves links and screenshot references.** Run existing internal-link and image-path checks; expected result is no broken anchors or images.

### Task 3: Promote Starcatcher and Pet Vault; preserve concise summaries

**Files:**
- Modify: `docs/MODLIST.md` — Starcatcher and companion sections, Part IV cross-reference, TOC, indexes, and relevant source lists.
- Modify: `docs/assets/screenshots/README.md` if new captures are required.
- Create: real Starcatcher or Pet Vault screenshots only from the current 26.2 pack when they demonstrate actual player actions.

**Interfaces:**
- Consumes: the exploration-first Part III sequence from Task 2 and the Curios guide from Task 1.
- Produces: one promoted Starcatcher guide and one clearer Pet Vault guide, each with a single canonical explanation.

- [ ] **Step 1: Verify the installed Pet Vault workflow in-game.** Test Keeper's Locket access, registration, summon/store, permanent unregister, status information, and any Curios integration. Resolve the documented inventory-versus-necklace discrepancy before editing prose.
- [ ] **Step 2: Move Starcatcher's full profile into Part III's discovery/collection sequence.** Preserve the installed controls and first-catch flow, and explain location conditions, tackle, catalogue, progression/rewards, and relevant accessibility or multiplayer settings. Do not list every fish.
- [ ] **Step 3: Expand Pet Vault into a focused player workflow.** Explain its value for expeditions, verified locket requirements, registration versus storage, summoning, status/last-known behavior, and the dangerous unregister interaction.
- [ ] **Step 4: Remove the now-empty Part IV “Fishing and collection systems” heading and add one Starcatcher cross-reference under “Farming, food, and cooking.”** Keep the survival reader path intact without duplicating the full guide or leaving an empty topic.
- [ ] **Step 5: Keep ordinary companion, armor, and structure entries short.** In particular, do not inflate Iron Wolf Armor or narrow structures to major-system-guide length.
- [ ] **Step 6: Confirm canonical headings, index links, gameplay tags, sources, and real screenshot paths.** Run the existing link and local-image checks; expected result is no broken references or duplicate canonical descriptions.

### Task 4: Add the RPG-first systems route to Quick Reference

**Files:**
- Modify: `docs/MODLIST.md` — Quick Reference and first-session checklist.

**Interfaces:**
- Consumes: the canonical headings and player guidance created or retained in Tasks 1–3.
- Produces: one compact front-of-book route in this order: RPG build; equipment/Curios/Penchant; world navigation/Alex's Caves; Starcatcher; Pet Vault.

- [ ] **Step 1: Add a concise “Major systems that change how you play” guide map inside Quick Reference.** Give each item one sentence about why it matters and link it to its canonical guide. Keep the RPG first and exploration second.
- [ ] **Step 2: Align “First ten minutes” and “Your first controls” with that route.** Preserve verified controls and retain the complete control tables as the first chapter of Part I.
- [ ] **Step 3: Check the updated Quick Reference at print width.** Keep it compact enough to read cleanly in print; shorten prose rather than changing the handbook's visual style.
- [ ] **Step 4: Check every new Quick Reference anchor.** Run the existing internal-link check from `docs/HANDBOOK_MAINTENANCE.md`; expected result is no broken anchors.

### Task 5: Synchronize maintenance guidance and validate the handbook artifact

**Files:**
- Modify: `docs/MODLIST.md` — TOC page numbers, audited counts/date, alphabetical/tag indexes, source links, and any final cross-references.
- Modify: `docs/HANDBOOK_MAINTENANCE.md` — add the prominence rubric and single-canonical-guide rule for major systems.
- Modify: `docs/assets/mod-icons/manifest.csv`, `docs/assets/mod-icons/README.md`, and `docs/assets/screenshots/README.md` only for verified catalog/artifact changes.
- Modify: `docs/build-handbook.ps1` or `docs/handbook-print.css` only if the existing pipeline cannot preserve the specified order or printed layout without a targeted adjustment.

**Interfaces:**
- Consumes: the finished content and real visual assets from Tasks 1–4.
- Produces: an internally consistent Markdown source and a visually reviewed handbook PDF using the existing pipeline.

- [ ] **Step 1: Update the indexes and sources.** Add Penchant to its canonical entry, alphabetical index, and the most useful gameplay tag. Confirm all promoted mods appear once in the catalog; update the documented entry count after the pack/catalog comparison.
- [ ] **Step 2: Update the TOC and handbook audit metadata.** Preserve the TOC's one-page layout; rebuild after pagination changes and correct every printed page number.
- [ ] **Step 3: Add the future maintenance rubric to `HANDBOOK_MAINTENANCE.md`.** Explain how to select major system guides versus concise mod summaries, and require installed-pack verification for controls/configuration.
- [ ] **Step 4: Run the existing handbook checks from `docs/HANDBOOK_MAINTENANCE.md`.** Verify distribution/catalog counts, index order and uniqueness, internal anchors, local images, screenshot queue parity, icon sources/rendering, and `git diff --check`. Add no new automated test suite for this documentation-only change.
- [ ] **Step 5: Build the PDF with `.\docs\build-handbook.ps1` and inspect every page.** Confirm the original visual system remains intact; the TOC stays one page; links and numbers resolve; screenshots are real, legible, and uncropped; headings/tables do not clip; and no unintended blank pages appear.
- [ ] **Step 6: Review the final diff and stage only exact handbook paths changed by this plan.** Leave unrelated `mods/`, `mod store/`, and unrelated `docs/assets/` changes untouched; do not use `git add -A` or stage the entire assets directory.
- [ ] **Step 7: Commit the reviewed handbook change.**

```powershell
git commit -m "docs: highlight major handbook systems"
```
