# Contributing

## Build

```bash
./gradlew build
```

The current target requires **JDK 25**. The JAR is written to `build/libs/`.

## Test

```bash
./gradlew test
```

The core packages — recipe indexing, profit calculation, Bazaar evaluation, and version comparison — intentionally do not depend on Minecraft and can therefore be tested without a running game.

Minecraft-facing code (`gui/`, `mixin/`) is validated by the CI build and must additionally be tested in-game before a release.

CI reports the executed test count and fails if zero tests ran. It also opens the built JAR and validates required entries, the mod identifier, the target version, and bundled recipe data.

## Project structure

| Package | Purpose |
|---|---|
| `daten/` | Shards, recipes, indexing, and data acquisition |
| `markt/` | Price sources, Bazaar, and caching |
| `rechner/` | Profit calculation and price modes, independent of Minecraft |
| `gui/` | Fusion Machine detection, overlay, and interaction |
| `mixin/` | Single `@Accessor` exposing container GUI dimensions |
| `wartung/` | Release-version checking |

## Development rules

1. **Keep the core independent of Minecraft.** `rechner/` and `daten/` should remain free of Minecraft imports wherever possible.
2. **Do not inject into the render path.** Use Fabric screen events instead of custom render injections.
3. **Do not automate gameplay.** Fusion 420 is a display-only tool. Do not add simulated clicks, inventory movement, purchases, or other gameplay automation.
4. **Do not manually edit generated recipe data** unless the data source itself requires a deliberate change. The `daten-sync` workflow refreshes `src/main/resources/daten/fusion-data.json`.
5. **Add tests for calculation and data logic.** Keep Minecraft-specific behavior isolated so the core remains easy to test.
6. **Keep public documentation and user-facing text in English.**

## Porting to a new Minecraft version

See the **Minecraft Updates** section in the [README](README.md).

Before publishing a port, run the complete test suite, verify the built JAR, and perform an in-game compatibility test.
