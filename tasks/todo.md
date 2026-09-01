# Fusion 420 — Status

## Completed

- [x] Project scaffold: `build.gradle`, `settings.gradle`, `gradle.properties`, Gradle Wrapper 9.5.1
- [x] `fabric.mod.json` and `fusion_420.mixins.json` with Java 25 compatibility
- [x] Verified Minecraft 26.1.2, Loader 0.19.3, Loom 1.17-SNAPSHOT, Fabric API 0.155.2+26.1.2, and Java 25
- [x] `daten/` — recipe book with sorted `long` index and bundled recipe data
- [x] `markt/` — asynchronous Bazaar access through the keyless public Hypixel API with caching
- [x] `rechner/` — profit calculation, buy/sell modes, tax, and volume filtering
- [x] `gui/` — Fusion Machine detection, overlay, dropdown controls, hover highlighting, and drag-and-drop positioning
- [x] `mixin/` — single `@Accessor` for container GUI dimensions
- [x] Render API verified against the Fabric 26.1 source rather than guessed
- [x] Unit tests for recipe indexing, bundled data, Bazaar evaluation, profit calculation, and version comparison
- [x] Workflows for build, release, and recipe-data synchronization
- [x] README, MIT license, third-party notices, security policy, contribution guide, issue templates, Dependabot, and editor configuration
- [x] Startup release-version check and outdated-recipe warning
- [x] Recipe-source indicator: online, cache, or bundled
- [x] English user-facing text and public documentation

## Open

- [ ] **Remove the real name and private email address from Git history.** Existing historical commits still contain the old author identity. This requires rewriting the history and force-updating `main`. GitHub account email privacy must also be enabled before making further local commits.
- [ ] **Modrinth** — create the project and configure the repository's `MODRINTH_TOKEN` secret and `MODRINTH_ID` variable. The release workflow already contains the upload stage, but it remains untested until the credentials are configured.
- [ ] **In-game validation** — verify the exact Fusion Machine title, shard-name matching, Chameleon behavior, tooltip layering, hover highlighting, draggable overlay, and price-mode calculations on a live server.

## Intentionally omitted

**Auction House support.** The current recipe data maps all supported shards to Bazaar identifiers, so an Auction House source is unnecessary for the current design. The price-source interface is kept separate so another source can be added later without changing the calculation core.

## Known limitations of CI

A green build proves that the project compiles, tests execute, and the JAR structure is valid. It does not prove that the overlay looks correct at runtime or that Hypixel's live GUI text and item metadata still match the current data. Those require an in-game test.
