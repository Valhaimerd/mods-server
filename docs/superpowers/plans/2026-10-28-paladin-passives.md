# Paladin Passive Rework Implementation Plan

Goal: implement the exact Vanguard/Guardian spec inline, preserving saves and other changes.
Spec: ../specs/2026-10-28-paladin-passives-design.md

- [x] Pin current Skill Tree input and assert old passive values fail the new expectations.
- [x] Add PaladinPassives equipment-condition/config helper; patch SkillEffects.register once. Reuse native triggers/statuses. Repack only Skill Tree with names,descriptions and node titles.
- [x] Native isolated fixture: native/modded shield recognition; equipment conditional removal; guardian no-critical/no-cooldown hits; four-stack cap/5s refresh; haste cooldown; saved config override; cleanup. Check tier-II bytes unchanged.
- [x] Actual client: allocate real passive nodes, equip/unequip conditions and observe armor/healing values, native new node names and modifiers.
- [x] Install tested same-name active/fixed JAR, back up prior input; update raw references and handbook/PDF, check hashes, links and layout. Display tier-II skills/nodes/passives side by side in final answer.

Completion: same-name Skill Tree replacement installed in active/fixed packs with ignored backup.35 fresh +35 legacy native checks and15 client checks pass.555 archive entries and all tier-II resources are unchanged. Handbook79 pages,51 TOC links checked and all pages visually reviewed. No workers, commit or push.
