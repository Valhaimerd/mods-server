# Handbook Pack Sync and Group Compaction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sync the player handbook with the current 26.2 mod pack, explain the new skill and enchanting systems, and shorten the handbook by consolidating the seven grouped collections.

**Architecture:** `docs/MODLIST.md` remains the source for handbook text and indexes. The icon manifest and fetch script will map each collection to one verified member icon, then the existing PowerShell builder will regenerate the print PDF from the updated Markdown.

**Tech Stack:** Markdown, PowerShell, Git, the repository's existing Edge/Chrome PDF build script.

**Spec:** [2026-10-11 handbook pack sync and group compaction design](../specs/2026-10-11-handbook-pack-sync-and-group-compaction-design.md)

## Global Constraints

- Use the live `mods/` and `mod store/` directories as the inventory source and recount immediately before editing.
- The seven group titles and representative members must match the approved design table exactly.
- Recount live JARs immediately before finalizing the distribution count; omit support libraries such as Patchouli from the gameplay catalog.
- Distinguish Penchant's core behavior from optional per-world modules; do not claim which modules are active.
- Identify the missing Pufferfish's Attributes dependency for Stronger Skill Tree; do not alter JARs or claim its attribute effects work.
- Preserve existing policies that omit libraries and support APIs from the gameplay catalog and use verified official artwork.
- Keep all user changes in `mods/` intact and include those current additions/removals in the already-authorized GitHub update.
- Do not add or run test suites for this documentation task. Build the requested PDF and inspect its rendered pages.

## Review Focus

- Pack counts must match the current `mods/` and `mod store/` directories; the gameplay catalog excludes support libraries.
- Removed names must no longer appear as active mods in content, controls, indexes, source lists, or screenshots.
- Names of consolidated group members must remain findable in the compact group body even though separate member entries and index rows are removed.
- Each representative image must map to the correct member project and retain its official source information.
- Penchant module activation and the Stronger Skill Tree dependency gap must be described without overstating confirmed in-game behavior.

---

### Task 1: Synchronize handbook content and mechanics

**Files:**
- Modify: `docs/MODLIST.md`
- Modify: `docs/assets/screenshots/README.md`
- Delete: `docs/assets/screenshots/04-skill-perks.png`

**Interfaces:**
- Consumes: the live JAR inventory and the approved mechanics/grouping decisions in the spec.
- Produces: current, indexed handbook content that Task 2 can map to verified group icon sources.

- [x] Recount `mods/*.jar` and `mod store/*.jar`, and compare them with `git status --short -- mods 'mod store'` immediately before editing.
- [x] Update the handbook date and distribution counts. Remove all active Skill Perks, Easy Magic, and Enchanting Infuser guidance, including keybind and onboarding copy, source references, indexes, and gameplay tags.
- [x] Add concise skill-tree guidance for Adventurer Skills and Stronger Skill Tree under Pufferfish's Skills. Note the absent Pufferfish's Attributes dependency and avoid describing its attribute effects as working.
- [x] Rewrite the enchanting section for Penchant's use-based leveling, explain that its optional modules are selected per world, and describe the installed Max Protection and Toughness Enchantment behavior. Retain only enchantment systems still present in the live inventory.
- [x] Remove the obsolete Skill Perks screenshot from the handbook and capture queue; do not create a replacement without a real current-pack capture.
- [x] Consolidate all seven groups according to the spec: one collection entry with one concise member list, no separate full member entries, and no separate member rows in the alphabetical index. Keep Penchant as a detailed individual entry.
- [x] Read the affected sections and indexes to ensure member names remain findable and removed mods are not described as active.

**Completion:** The handbook describes the current player-facing skill and enchantment systems, has no active references to the removed mods, and presents each approved collection once.

### Task 2: Align icon sourcing and maintenance guidance

**Files:**
- Modify: `docs/assets/mod-icons/manifest.csv`
- Modify: `docs/fetch-mod-icons.ps1`
- Modify: `docs/tests/Test-ModIconSources.ps1` (update existing expectations only; do not run it)
- Modify: `docs/assets/mod-icons/README.md`
- Modify: `docs/HANDBOOK_MAINTENANCE.md`
- Remove: grouped-member icon assets that have no remaining manifest reference

**Interfaces:**
- Consumes: the collection titles and member names produced by Task 1.
- Produces: one traceable representative icon per collection and a refresh script that preserves those mappings.

- [x] Map the seven group titles to the approved representative members and copy each member's verified CurseForge title, page URL, icon URL, and existing local image into the group manifest row.
- [x] Add verified icon sources for the newly indexed Adventurer Skills, Stronger Skill Tree, and Penchant entries; leave artwork unresolved only when no policy-compliant official image exists.
- [x] Update `fetch-mod-icons.ps1` so refreshing the alphabetical index resolves the same representative member for each group rather than deleting the group image mapping.
- [x] Update the existing icon-source test expectations to require traceable representative artwork instead of image-free group rows. Do not add tests or run the test scripts.
- [x] Remove only group-member images no longer referenced by any manifest row. Keep all unrelated icon assets.
- [x] Update the icon README and handbook maintenance rules for representative group images, compact member lists, the current counts, the Pufferfish's Attributes dependency, and Penchant's world-specific modules.
- [x] Read the updated manifest, script mapping, and maintenance notes together to ensure the group labels and representative source titles agree.

**Completion:** Group images render from verified member artwork, refresh logic retains the mapping, and maintenance instructions describe the new catalog shape.

### Task 3: Rebuild and deliver the handbook PDF

**Files:**
- Modify: `docs/MODLIST.md` (final page numbers and any pagination corrections)
- Generate: `output/pdf/modpack-player-handbook.pdf`
- Generate: `modpack-player-handbook.pdf`

**Interfaces:**
- Consumes: the final Markdown, manifest, and style rules from Tasks 1 and 2.
- Produces: matching final repository-root and output PDFs.

- [x] Run `.\docs\build-handbook.ps1` from the repository root.
- [x] Render the generated PDF pages to PNG and inspect the cover, table of contents, index pages, skill progression, enchanting, and each section affected by group compaction.
- [x] Correct pagination references in `docs/MODLIST.md` if the rendered destinations changed, then rebuild and inspect the final PDF.
- [x] Leave temporary rendering files in `tmp/pdfs/` only while reviewing; keep the committed deliverable at the repository root and confirm the generated output copy is identical by visual inspection of the build result.
- [x] Stage the handbook changes and the user's current `mods/` additions/removals explicitly, commit on `main`, and push to `origin/main` as already authorized.

**Completion:** The rebuilt handbook has readable group entries, current contents-page numbers, no stale mod guidance, and a final PDF at the repository root.
