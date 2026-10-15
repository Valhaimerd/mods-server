# Agent workflow for this modpack

Use the user's latest request to choose the relevant workflow below. Read the listed instructions before editing. If a request spans workflows, complete them in dependency order: inspect the pack, fix or install the mod, update player documentation, rebuild the handbook, then handle any requested distribution or GitHub work. Do not stop after updating only one artifact of a combined request.

## Start here

- Read `README.md` for the pack's Minecraft, NeoForge, Java, installer, and client/server folder rules.
- Check `git status --short` and the actual JARs in `mods/` and `mod store/`. Preserve unrelated user changes and ignored backups.
- `mods/` is the server-compatible pack; `mod store/` is for client-side JARs. Never infer side compatibility, dependencies, or a mod's behavior from its filename alone. Inspect JAR metadata and the mod's official page when those facts matter.
- For a repack of an existing mod/version, keep its current active JAR filename and replace its contents. The installer compares Git content hashes, so changed bytes are detected without a new filename. Rename a JAR only for an actual mod/version change or when the user explicitly asks.
- The separate desktop server and the user's personal `.minecraft` are outside this repository. Use isolated test instances unless the user asks to change those installations.
- Use skills when available and relevant: `caveman:investigate-first` or `superpowers:systematic-debugging` for reported defects; `living-docs` for documentation synchronization; `pdf:pdf` for PDF inspection; `quality-gate` for requested verification. The folder instructions below remain the pack-specific source of truth. A named skill from the user also applies.

## Route by request

| User request mentions | Read first | Work to carry through |
| --- | --- | --- |
| Handbook, guide, mod entries, added/removed mods, categories, controls, keybinds, icons, PDF | `docs/HANDBOOK_MAINTENANCE.md`, then `docs/MODLIST.md`; for images, `docs/assets/mod-icons/README.md` or `docs/assets/screenshots/README.md` | Reconcile the installed pack with canonical entries, alphabetical and gameplay-tag indexes, official links, counts, controls, table of contents, and PDF. Follow the handbook checklist's build and visual review steps. |
| Crash, bug, incompatibility, port, repack, pending/fixed pack | `mod fixing/README.md`, then the target mod's `README.md` or source notes under `mod fixing/` if present | Identify the exact installed JAR and root cause, build the final replacement, remove superseded active JARs, and document the fix. Use the folder's pending/fixed/retired rules. |
| Server errors or warnings | `mod fixing/server bugs.md`, `mod fixing/README.md`, and `README.md` | Inspect the complete relevant log and distinguish startup blockers from warnings. Test on an isolated server when the user requests testing. |
| Mod version updates, duplicates, replacements, client/server placement | `mod fixing/mod update report.md` if present, `mod fixing/README.md`, and `README.md` | Check version, loader, dependencies, side, and duplicate JARs before replacing entries. Keep backups ignored. If player-facing content changes, also follow the handbook route. |
| Installer, automatic controls, release asset | `README.md`, then inspect `installer/` | Keep the installer aligned with the current pack and documented keybind profile. Build or publish only as the request calls for. |
| Optional Farmer's Delight add-ons | `mod fixing/README.md`, then `mod fixing/delight extras (optional)/README.md` | Follow that folder's port and packaging guidance. |

## Combined changes and completion

- For a mod fix or update that changes player-visible behavior or the installed mod set, check whether `docs/MODLIST.md`, its indexes, keybind mentions, source links, icons, `modpack-player-handbook.pdf`, and `README.md` need corresponding changes. Keep the handbook concise for minor entries and detailed for major mechanics as directed by `docs/HANDBOOK_MAINTENANCE.md`.
- For handbook-only work, do not modify JARs unless the user also requested pack changes. Use the installed pack as the authority for what players actually receive. Derive recommended keys from the pack's documented controls or verified profile, not an upstream default. Do not invent a shortcut for an unbound action.
- When a request includes testing, use the smallest isolated client/server mod set that can exercise the behavior, record exact versions and observed results, and keep personal worlds untouched. Do not claim a runtime fix from compilation alone.
- The root `.gitignore` intentionally excludes local workspaces, backups, server data, and most `mod fixing/` contents. Check `git status` and the ignore rules before staging. Commit or push when the user asks for GitHub synchronization; never include ignored backups or private configuration.
- When a tracked PDF is open and Windows prevents replacement, finish the build and other checks first, then ask the user to close the viewer so the new file can be installed.
