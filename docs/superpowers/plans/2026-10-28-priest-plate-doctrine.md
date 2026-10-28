# Priest plate restriction and Paladin armor progression

User-approved concrete scope: Priest class allocation rejects Paladin/Crusader/Netherite Crusader/Justicar/Lightbringer armor, with a brief doctrine warning and Priest root tooltip update. Healing Power per piece becomes1/1.25/1.5/1.75/1.75. Preserve other stats, set bonuses, Priest weapons and previous Paladin hand restriction. Display all Paladin actives/nodes/passives paired by tier in final chat. No push requested.

Implementation: extend existing server doctrine helper and existing armor helpers; no new dependency, worker or separate equipment system. Add a reusable20-item armor tag. Reuse persisted overflow and early equipment/casting guards. Existing generated configs receive new values.

- [x] Snapshot current installed inputs; extend native failing cases for Priest equipment and new armor values.
- [x] Implement tag, Priest role/equipment checks, warning and root description; update three existing helper classes without disturbing older patches.
- [x] Verify native armor defaults/fresh/old configs, all20 restrictions, safe full inventory, power removal, respec, persistence and old Paladin behavior.
- [x] Run one actual client pass for warning, root text, equipment sync and allowed Priest weapons/robes; perform focused inline code review.
- [x] Install same-name active/fixed replacements with ignored backups; synchronize working references, patch notes and handbook/PDF; verify hashes/links/layout.
- [x] Display full tier pairs and implementation results in chat. Keep work in one agent per user's token-efficiency preference.
