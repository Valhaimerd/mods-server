# Spell details and Penchant progress

The installed Spell Engine 1.10.5 has a dedicated Tooltip Spell Details key.
It falls back to Bypass Spell Hotbar only when the dedicated key is unbound.
The previous recommended profile left both unbound, preventing expansion and
suppressing the hold-for-details hint. Penchant was not deleting spell details.

The recommended profile now binds Tooltip Spell Details to Left Ctrl. Penchant
progress retains its Left Ctrl recommendation, and hotbar bypass stays unbound.
Both installed implementations poll the physical held-key state independently:
SpellTooltip.isKeyPressed and Penchant KeyMappingExt.isDownAnywhere. Neither
consumes a key press, so both sections can expand together. No mod JAR changes
or optional compatibility layer are needed.

For an existing profile, open Controls > Spell Engine > Tooltip Spell Details
and assign Left Ctrl, or reapply Recommended Keybinds using an installer built
with this profile. Penchant can also be assigned Left Ctrl for simultaneous
expansion, or kept on a separate preferred key. The inspected personal profile
currently has Penchant on grave accent and both Spell Engine keys unbound;
it was inspected read-only and was not changed.

Validation: installed bytecode decompiled to verify both independent raw-input
paths and the Spell Engine fallback/hint conditions. The recommended profile
contains a single binding for each action; the dedicated key is Left Ctrl,
Penchant stays Left Ctrl, and bypass stays unbound. This is configuration and
code-path verification, not a live client hover test. No installer release was
built or published.

Handbook validation: 74 pages rendered, with only controls page 70 differing
from the previously reviewed edition. Page 70 was visually checked without
clipping; 461 internal links and identical root/output PDF copies remain.
