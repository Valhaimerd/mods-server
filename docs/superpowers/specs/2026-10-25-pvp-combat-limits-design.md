# PvP combat limits and status HUD

Status: approved by the user on 25 October 2026; implementation and verification tracked in the linked plan.

## Intent

Prevent mass building and mining during player combat while allowing a small amount of ordinary block use. Show clear private warnings and three compact countdown icons. Outside combat, the current pack behavior remains unchanged.

## Existing pack and approach

Minecraft 26.2 / NeoForge 26.2.0.90. The installed `pvp_flagging-26.2-1.1.2.jar` is currently a damage-retention replacement, not an active combat flag system. Its shared player-source resolver already handles direct players, projectiles and the four RPG summons, with Better Party and vanilla team protection.

Recommended approach: extend that existing mod with server-owned combat state, a small client status payload and HUD. Add narrow guards at Sophisticated Building's server operations and OneKeyMiner's shared chain-action entry point. This centralizes the rules and avoids client-only enforcement. Preserve current damage multipliers and all earlier pack repairs, including unrestricted building axes outside combat.

A separate new mod would duplicate the existing ownership/party layer. Configuration alone cannot provide confirmed-damage timers, the block quotas or the HUD. Neither alternative fits as well as extending the existing mod. Keeping additional weapon/class changes out of scope is deliberate.

## Combat flag

- A confirmed positive damage event against a player, attributable to a different player, flags **both players** for 40 seconds. The user explicitly chose both.
- Refresh both flags on each qualifying hit. Resolve player-owned projectile/spell/summon sources using the existing ownership rules. Self damage, mob/environment damage, canceled damage, zero damage and party-protected hits do not flag players.
- Send one private chat notice when an unflagged player becomes flagged. Refreshes are silent. A later new combat period can send the notice again.
- Refreshing combat does not reset either active block window or refund its allowance.
- At flag expiry, immediately stop applying all combat block restrictions, clear both windows and hide all three icons.
- Proposed implementation choice: retain remaining combat/window time and counters through reconnect, dimension changes and respawn; time continues to expire while offline. Use server-owned saved deadlines so reconnecting cannot reset the budget. Do not retain live player/entity references or create background threads/tasks. Expired saved entries are pruned and server shutdown clears runtime references.

## Building and mining restrictions

- While flagged, disallow Sophisticated Building's advanced placement, replacement, breaking, mirrors/arrays and undo/redo operations. Ordinary single-block Minecraft building remains available within its quota.
- While flagged, disallow OneKeyMiner chain actions, including its multi-block mining and chained tool interactions. The original ordinary block action remains subject to the normal quota.
- Reject new advanced work and stop remaining queued work when the flag starts. Already completed changes remain. Stopped work must not consume further materials or durability and must not resume automatically after combat.
- Enforce these rules on the server even if a client submits an advanced-operation packet. Keep native permission/protection checks authoritative.

## Independent block windows

| Ordinary action | Allowance | Window |
| --- | --- | --- |
| Break blocks | 30 blocks | 30 seconds |
| Place blocks | 20 blocks | 30 seconds |

Each window starts on the first attempt of that action while flagged. Starting the break window does not start placement, or vice versa. Successful block changes consume allowance; rejected/protected attempts consume none. At the deadline, the allowance refreshes; the next attempt starts a new window. Further failed attempts do not restart or extend the deadline.

Count each changed block in a multi-place event. Reject an event that would exceed the remaining allowance rather than allowing a partial structure. Reject over-budget breaks before block destruction. Handle client corrections so blocked actions do not leave ghost blocks or consume items. Non-block item use and entity combat are outside these quotas.

When the player attempts a capped action, show a short private action-bar warning such as `Placement limit reached. Wait 12s.` Likewise warn when an advanced tool is blocked. Throttle repeated warnings so held inputs do not flood the display.

## HUD

Use the three supplied images in this order from left to right:

1. Crossed swords: PvP flag countdown.
2. Breaking block (`05_45_46 PM`): breaking-window countdown.
3. Placement block (`05_45_57 PM`): placement-window countdown.

Render three touching square slots at `x = 0`, `y = scaled screen height - slot size`: exactly flush with the bottom-left edges, with no outer margin or gaps. Match the installed Spell Engine skill-slot size after inspecting its renderer. Size and position use GUI coordinates so they follow the game's GUI scale. Fit each supplied image into its square and draw a legible, outlined remaining-seconds number **inside** the icon.

All three slots are visible only while flagged. A block window that has not started, or is ready for a new window, displays `0`; it does not begin counting merely because combat starts. Display ceiling seconds to avoid showing zero before expiry. Render the HUD without shifting the existing skill panel. Respect the game's hidden-HUD setting.

Client countdowns are display-only; server deadlines decide permissions. Sync on flag/window changes and periodically correct the displayed time, without per-frame packets or world/entity scans.

## Verification and delivery

Use disposable server/client instances and matching active versions. Verify real direct and indirect player damage, protected party hits, silent refreshes, independent windows, exact 20/30 boundaries, no deadline extension from blocked attempts, expiry before a block window ends, reconnect/respawn/dimension continuity, and cleanup. Exercise actual building packets/queues and OneKeyMiner chain actions as well as ordinary block events; test resource consumption and corrections. Verify the existing damage-retention fixture still passes.

Visually inspect the actual HUD at more than one GUI scale: icon order, square bounds, internal countdowns, edge alignment, hiding on expiry and existing skill-panel coexistence. Verify dedicated-server loading without client classes.

After tests, install same-filename final JAR replacements in repository `mods/` and ignored fixed packs, archive predecessors, update player documentation and rebuild/review the handbook PDF. Keep personal Minecraft, the separate server and unrelated pending changes untouched. Do not commit, push or publish without a request.
