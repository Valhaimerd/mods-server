# Combat follow-up phase 1: class reworks

> **For agentic workers:** Use superpowers:executing-plans, inline without additional agents.

**Status:** Implemented and installed in repository pack folders on 2 November 2026. No commit or push.

**Goal:** Deliver the requested class balance, Mutilate targeting and melee attack-movement changes.

**Spec:** [Shared detailed specification](2026-11-02-purgatory-player-wall-enforcement.md). Read the full assigned sections; this checklist does not replace their exact values or safeguards.

**Architecture:** Update native skill resources and narrowly scoped runtime hooks together, preserving existing IDs and unrelated current patches. Share movement eligibility between Berserker, Wild Charge and the Assassin path where practical.

**Tech stack:** Current NeoForge pack, Skill Tree, Adventurer Skills, Rogues & Warriors, Spell Engine and Better Combat integration.

## Assigned specification sections

- Additional requested balance changes.
- Assassin and Warrior rework additions, including all tier I–III changes, implementation considerations and balance notes.
- Berserker and Assassin movement follow-up, including its integration checklist.

## Tasks

- [x] Record current JAR hashes and inspect exact resources/hooks before edits. Read pack workflow and description guidance.
- [x] Change Adventurer melee nodes to **2%** and RPG Warrior nodes to **5%**, synchronizing modifiers and tooltips.
- [x] Implement targeted **8-block / 0.25s Mutilate** with safe front-of-target placement and existing attack/node effects preserved.
- [x] Implement Warrior tier I projectile, Slowness, root, Vitality and Killing Spree changes using the confirmed shared healing interval.
- [x] Implement tier II Shout, Charge, tackle, Warm Up and Momentum changes. Tackle stuns each enemy once per cast; anti-roll is server-enforced.
- [x] Implement tier III Last Stand/Juggernaut/Sentinel/Enrage/Tenacity changes and Berserker/Brutality/Lethality follow-up. Preserve the specified magic mitigation, interval and cleanup rules.
- [x] Implement attack-slowdown and sprint-cancellation exceptions for active Berserker/Charge and the selected Assassin path. Unrelated control effects still apply.
- [x] Run focused value, threshold, party, cleanup and movement checks from the shared spec. Inspect actual armor, resistance, critical and healing results rather than only descriptions.
- [x] Install matching same-filename replacements into repository pack locations; record output hashes and changed entries. Update class references, concise descriptions and required handbook artifacts.

## Completion checkpoint

All assigned values and behaviors work with unchanged saved IDs; tooltips match actual effects; no obsolete Lethality self-cost remains. Report focused results and limitations. Leave wall/reach repair and archery work to phases 2/3; do not claim they are fixed.

Evidence: [phase-one implementation and results](../../patches/combat-followup-phase-one/README.md). 70 native server checks, 11 connected-client checks and 4,161 unchanged-entry checks passed. Personal installations remain untouched.
