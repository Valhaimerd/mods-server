# Installed Adventurer Defense tree audit

4 November 2026. Read-only inspection of the active Adventurer Skills JAR. `tree.json` records its SHA-256, exact skill IDs, definition rewards, coordinates, neighbors and exclusions. No gameplay JAR was changed; this is a graph/reward audit, not a fresh damage-mechanics test.

The Defensive root grants no attribute reward. Its three first nodes are mutually exclusive, selecting Aegis, Colossus or Bulwark. The connected component contains 27 placements: one root, 23 ordinary nodes and three final nodes. Normal edges are bidirectional. The sequences below describe outward progression from Defensive; side leaves are optional, not native all-path prerequisites.

## Aegis (upper)

Defensive -> +2% Magic Resistance -> +2% Ranged Resistance -> +2% Magic Resistance -> +2% Ranged Resistance -> +2% Magic Resistance -> +2% Ranged Resistance -> Aegis.

The last ordinary ranged-resistance node also connects to a dead-end +2% Magic Resistance node; that side node does not connect to Aegis. Seven ordinary nodes exist in this branch, six on the direct route and one side leaf.

Aegis rewards: +15% Magic Resistance, +10% Ranged Resistance, +6% Movement Speed. Icon: enchanted golden apple; size 1.

## Colossus (middle)

Defensive -> +1 Heart -> +3% Healing -> +1 Heart -> +3% Healing -> +1 Heart -> Colossus.

The second healing node also has an optional +3% Healing leaf. The last +1 Heart node also has an optional +1 Heart leaf. Neither leaf connects to Colossus. Seven ordinary nodes exist in this branch, five on the direct route and two side leaves.

Colossus rewards: +6 Hearts (+12 maximum HP) and +6% Healing. Every ordinary +3% Healing node and the final +6% Healing node affect both Puffish healing and the Healing spell school. Icon: totem of undying; size 1.

## Bulwark (lower)

Defensive -> +1% Resistance -> +1 Armor -> +1% Resistance -> +2% Ranged Resistance -> +1 Armor -> split:

- +1% Resistance -> Bulwark; that Resistance node also has an optional +1 Armor leaf.
- +2% Ranged Resistance -> Bulwark; that Ranged Resistance node also has an optional +2% Ranged Resistance leaf.

Both penultimate nodes connect to Bulwark. Nine ordinary nodes exist: five before the split, two penultimate nodes and two optional side leaves. Because edges are normal/bidirectional, both incoming links alone do not mean both predecessors are mandatory.

Bulwark rewards: +5% Resistance, +4% Ranged Resistance, +2 Armor, +20 percentage points Knockback Resistance. Icon: netherite chestplate; size 1.

Percentages above are actual configured node reward amounts, not a sum claiming all final stats add linearly. Resistance/healing reward operations use multiply_total; maximum HP/armor use addition. The separately planned Resistance/magic parity audit is not implemented by this report.
