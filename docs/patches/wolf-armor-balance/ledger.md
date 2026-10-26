# Implementation ledger

- User approved the combined approach and exact proposed values.
- Baseline: native Warden20 hit against Netherite caused 0 HP loss.
- Ruling: use NeoForge incoming damage rather than changing Wolf.actuallyHurt's float argument; NeoForge damage containers preserve their own amount. An initial probe proved that argument-only interception did not change HP damage.
- Ruling: dedicated Unbreaking tags avoid enabling Protection/Mending through global armor/durability tag expansion.
- Ruling: keep native damage-source bypasses and damage-cost exclusions; fixed armor percentages replace only the supported item's default armor/toughness.
- Root cause: Penchant native wear hook admits ServerPlayer only; scoped owned-wolf armor progression preserves native player behavior.
- Armor/pet probe now observes 20 ->11 ->8.25 and proportional nonzero small-hit damage.

- Complete: candidate and module-off each passed 84 checks; exact tested archives installed in active/fixed copies. Review found no blocker. Handbook/reference updated and 77-page PDF reviewed; 590 untouched archive entries verified. Nothing committed/pushed.
