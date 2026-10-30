# Accessory Character Scaling Implementation Plan

> Execute directly using the existing repack and isolated-test workflows. The owner approved the recommendations and requested implementation without another design pause.

**Goal:** Give stat-focused curios a useful progression curve without feedback loops or uncapped boss damage.

**Architecture:** Reuse existing item handlers and the two independent accessory helpers. Evaluate attributes without all participating accessory health/melee modifiers, preserving outside flat/base/total modifiers. Apply dynamic flat modifiers adjusted for outside multipliers so the effective bonus stays under its cap. Reconcile equipped gear every server tick and remove inactive bonuses. Keep optional integrations optional.

**Values:** Titan +15% baseline max HP capped10; Crystal +10% capped8; Ghast +7.5% capped6; Power Glove +5% baseline melee capped2; Woundbearer capacity10% baseline melee capped4. Chorus restores25% current max HP capped16, retaining its consumed death-protection and teleport. Thorn deals1+10% armor capped4, retaining chance and interval. Tick deals1+10% strongest offensive power capped6 once/second in an8-block sphere, retains target maxHP>50/currentHP>half eligibility and self-health/hunger/removal drawbacks. It protects party/pets and uses attributed nonrecursive damage. One wearer cost per successful pulse rather than per affected entity. Other items unchanged. Wooden Stick remains fixed.

**Health rules:** Remove Titan's fixed Health Boost renewal; use a scalable max-health modifier. Existing short Health Boost expires naturally without stripping unrelated effects; exclude Health Boost from the shared baseline to avoid migrating old Titan health into new bonuses. No instant healing on equip, tick, external health changes or refresh. Clamp current health only when max HP falls. All three scalable HP bonuses use the same baseline and cannot scale each other. Shared melee baseline excludes Glove/Woundbearer additions. Caps refer to effective attribute gain, before health-received or combat damage multipliers.

**Files:** Existing `docs/patches/{nameless-balance,artifacts-balance}/src`, policies, tooltip overrides and builders; new scaling helpers in each independent package; `docs/patches/accessory-rpg-balance` tests/inventory/records; both owner references, handbook and PDF.

## Tasks

- [x] Add/run failing regressions for baseline/caps, combined gear, external multipliers, repeated refresh, removal, no healing, Chorus healing, Thorn scaling and Tick damage/party/cost/interval.
- [x] Implement shared attribute math, Nameless item callbacks and bounded Tick; compile staged archive.
- [x] Implement Artifacts equipment reconciliation, capped Thorn and Chorus healing; compile staged archive.
- [x] Run minimal and RPG/Better Party isolated-server suites; preserve old archive backups and install same-filename verified builds.
- [x] Update all references, hashes and the handbook PDF; visually review changed pages and run coverage/link/archive checks. No commit/push requested.

## Review focus

No self/cross-item feedback; effective caps under outside multipliers; health not restored by swapping; legacy modifiers and fixed Titan boost; Tick cannot harm allies or scale off victim HP; native Chorus consumption and cleanup retained.

## Result

Installed same-name verified archives after171 minimal /181 integrated isolated-server checks. The fixed Titan baseline, mixed-multiplier caps, no-healing refresh and disable cleanup were exercised. The review found disabled-item cleanup, which was reproduced then fixed. The updated handbook remains79 pages; changed pages24-25 were visually inspected. No new client/comparative class playtest, commit or push.
