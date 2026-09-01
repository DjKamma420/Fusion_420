# Fusion 420

Fabric client-side mod for **Minecraft 26.1.2** that calculates profitable Attribute Fusions in Hypixel SkyBlock.

Fusion 420 reads the shards currently visible in the Fusion Machine, evaluates available recipes, fetches public Bazaar prices, and displays the most profitable combinations.

It is display-only: the mod does not click, automate, move items, or send game actions.

## Features

- Live profit calculation using Hypixel Bazaar prices
- Up to 6 profitable fusion results
- Independent **Buy Price** and **Sell Price** selection
- Buy modes: **Instant Buy**, **Buy Order**, **Self-Farmed**
- Sell modes: **Instant Sell**, **Sell Order**
- Special handling for Chameleon Shards and dynamic output IDs
- Highlights required input shards when hovering a fusion result
- Draggable overlay with saved position
- Overlay is rendered behind Minecraft item tooltips
- Warns when visible shards are missing from the current recipe data
- Shows whether recipe data came from Online, Cache, or Bundled data
- Automatic recipe-data refresh
- Optional release-version check on startup
- No account credentials, tokens, or API keys required
- Fully client-side and display-only

## Installation with Prism Launcher

### 1. Create the instance

1. Open Prism Launcher and select **Add Instance**.
2. Choose **Vanilla** and select Minecraft **26.1.2**.
3. Set the mod loader to **Fabric**.
4. Give the instance a name and create it.
5. If Prism asks for Java, use the supported Java version offered by Prism for this Minecraft release.

### 2. Install Fabric API

1. Right-click the instance and select **Edit**.
2. Open **Mods**.
3. Select **Download Mods**.
4. Search for **Fabric API** from FabricMC.
5. Install the version compatible with Minecraft 26.1.2.

### 3. Install Fusion 420

Once a release is available on Modrinth, install it through Prism's **Download Mods** dialog.

For manual installation, download the latest release JAR from the [GitHub Releases](https://github.com/DjKamma420/Fusion_420/releases) page and add it under **Edit → Mods → Add File**.

Do not install a `-sources.jar` file. That file contains source code rather than the playable mod.

### 4. Start Minecraft

1. Start the instance.
2. Connect to Hypixel.
3. Enter SkyBlock.
4. Open a Fusion Machine.
5. The Fusion 420 overlay appears next to the container.

## In-Game Overlay

The overlay shows the most profitable currently possible fusions based on the selected price modes.

### Buy Price

- **Instant Buy** — values the input shards at the current Bazaar instant-buy price.
- **Buy Order** — values the input shards at the current Bazaar instant-sell price, representing a buy order.
- **Self-Farmed** — input cost is zero. Use this when you already own or farmed the required shards.

### Sell Price

- **Instant Sell** — values the result at the current Bazaar instant-sell price.
- **Sell Order** — values the result at the current Bazaar instant-buy price, representing a sell order.

### Recommended setting for self-farmed shards

If you farmed all required input shards yourself and only want to find the highest possible selling profit:

```text
Buy Price:  Self-Farmed
Sell Price: Sell Order
```

This sets input cost to zero and ranks results by absolute post-tax selling profit.

### Moving the overlay

Drag the **Fusion 420 title bar** to move the overlay. Its position is saved automatically.

### Hover highlighting

Hover over a fusion result in the overlay. Fusion 420 highlights the required input shards in the Fusion Machine so you can immediately see which items are needed.

The overlay is drawn before Minecraft's item tooltip extraction, so normal item tooltips remain visible above the overlay.

## Configuration

The configuration file is created at:

`config/fusion_420/einstellungen.json`

Important settings:

| Key | Default | Description |
|---|---:|---|
| `einkaufsModus` | `SOFORT` | `SOFORT`, `ORDER`, or `GEFARMT` |
| `verkaufsModus` | `SOFORT` | `SOFORT` or `ORDER` |
| `bazaarSteuerProzent` | `1.25` | Bazaar selling tax percentage |
| `mindestVolumenWoche` | `5000` | Minimum weekly Bazaar volume |
| `anzahlEintraege` | `6` | Number of results shown |
| `aktualisierungSekunden` | `60` | Minimum interval between Bazaar refreshes |
| `titelMuster` | `(?i)fusion` | Regex used to identify the Fusion Machine GUI |
| `nurAufHypixel` | `true` | Only show the overlay on Hypixel |
| `nurContainerSlots` | `true` | Ignore the player's own inventory |
| `overlayVersatzX` / `overlayVersatzY` | `6` / `0` | Saved overlay position offset |
| `overlayBreite` | `230` | Overlay width |
| `rezepteOnlineLaden` | `true` | Load fresh recipe data when available |
| `aufAktualisierungPruefen` | `true` | Check for newer Fusion 420 releases |

Changes to the configuration take effect after restarting Minecraft.

## Updates

Fusion 420 treats three types of updates separately.

### Recipe data

Recipe data is refreshed automatically. The repository's scheduled data workflow pulls current data from [SkyShards](https://github.com/Campionnn/SkyShards), while the mod downloads the latest available copy when it starts.

If the online request fails, Fusion 420 falls back to its local cache and finally to the bundled data inside the JAR.

If a visible shard is not present in the recipe data, the overlay warns that the recipe data may be outdated.

### Mod updates

Fusion 420 can check GitHub Releases on startup. If a newer release exists, the overlay reports it.

When distributed through Modrinth, Prism Launcher can handle the mod update through its normal mod update workflow.

### Minecraft updates

A new Minecraft version requires a compatible Fusion 420 build. The mod intentionally does not load on unsupported Minecraft versions.

Porting to a new Minecraft release requires updating the Minecraft/Fabric dependencies, fixing API or mapping changes, running the full test suite, and testing the resulting JAR in-game before release.

## Compatibility

Fusion 420 uses Fabric screen events instead of injecting custom rendering logic into Minecraft's render path. Its container accessor only exposes GUI dimensions and does not change control flow.

Compatibility with other GUI-heavy mods should still be verified on each supported Minecraft version.

## Privacy and Network Access

Fusion 420 is client-side and does not require a Minecraft account token, password, API key, or external authentication.

It can contact these public endpoints:

| Endpoint | Purpose | User data sent |
|---|---|---|
| `api.hypixel.net/v2/skyblock/bazaar` | Bazaar prices | None |
| `raw.githubusercontent.com/.../fusion-data.json` | Recipe data | None |
| `api.github.com/repos/.../releases/latest` | Version check | None |

No player name, session token, password, or account identifier is intentionally transmitted by the mod.

See [SECURITY.md](SECURITY.md) for security reporting information and [THIRD_PARTY.md](THIRD_PARTY.md) for external components and data sources.

## Fair Play

Fusion 420 is a display-only client mod. It reads information already visible in the game and public Bazaar data. It does not automate clicks, inventory actions, purchases, sales, or gameplay.

Always verify the current rules of the server before using any client modification.

## Development

Build the project with:

```bash
./gradlew build
```

Run the unit tests with:

```bash
./gradlew test
```

The project requires **JDK 25** for the current Minecraft target.

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## License

Fusion 420 is licensed under the MIT License. See [LICENSE](LICENSE).

Recipe data is sourced from [SkyShards](https://github.com/Campionnn/SkyShards) under its own license; see [THIRD_PARTY.md](THIRD_PARTY.md).

Fusion 420 is an independent project and is not affiliated with Hypixel or Mojang Studios.
