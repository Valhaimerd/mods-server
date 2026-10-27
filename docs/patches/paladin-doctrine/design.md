# Paladin equipment doctrine and Healing Power

Approved scope on28October2026: a player who selects Path of the Paladin cannot wield healing staves or wands in either hand. Show a brief oath warning. Ordinary Priests and damage-school weapons remain usable. Resolve Paladin membership from the actual unlocked class root; weapon training alone is not class membership. Use the installed healing_staff/healing_wand tags. Clear the restriction after a respec and recheck on reconnect. Preserve every item, including when inventory is full; do not drop or delete rejected equipment.

Numeric balance: the Paladin entry and ordinary flat Healing Power nodes increase from0.20 to0.25 in the class/weapon definitions and their runtime defaults. Per armor piece: Paladin0.75, Crusader1.0, Netherite Crusader1.15, Justicar and Lightbringer1.25. Full sets grant3/4/4.6/5/5. Apply both defaults and legacy loaded equipment configs. Preserve all other stats, robes, recipes, identifiers and prior fixes.

Implementation uses same-filename replacements of the existing mods. Native server authority must prevent healing weapon attributes and spell use; merely cancelling right-click is insufficient. Rejected equipment should return to inventory where possible, with safe handling of full inventories. Reuse existing APIs and avoid a new dependency.

Tests cover actual Puffish root unlock/reset, both hands, full inventory, casting and stat sources, reconnects, Priests and non-healing weapons. Compare all20 plate pieces and other native modifiers against the current pack under fresh/old configs. Update working references, patch notes, handbook and PDF. No push requested; personal instances remain untouched. Existing uncommitted Delight work is preserved.
