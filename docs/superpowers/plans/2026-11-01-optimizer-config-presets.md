# Optimizer Config Presets Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan inline. No delegation is needed.

**Goal:** Prepare coordinated native client/server presets in the repository, without modifying personal installations.

**Architecture:** Explicit worker budgets using existing mod configuration formats. Preserve entity behavior and save settings; document manual merge/rollback.

**Tech Stack:** JSON, YAML, Java properties; installed Minecraft 26.2 optimizer builds.

**Spec:** `docs/patches/performance-audit/unification-design.md`, with the user's explicit repository-only preset selection.

## Tasks

- [x] Check installed config schemas and worker semantics in Sodium and ScalableLux.
- [x] Prepare native files in `docs/patches/performance-audit/presets/client/config/` and `server/config/`.
- [x] Document changed versus retained settings, client/integrated/dedicated placement, backup/merge/rollback and runtime limits.
- [x] Validate JSON/YAML/properties, consistency with the installer Vectors preset, relative links and repository-only changes. JSON/properties checks passed; both YAML files parsed with ServerCore's bundled SnakeYAML 2.6 and retained control settings passed assertions. Runtime benchmarking remains outstanding on the actual host.

No JAR, installer behavior, world, live configuration or handbook mechanics change is planned. Runtime performance remains to be measured on the actual shared host.
