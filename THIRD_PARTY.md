# Third-Party Components and Data

## Fusion Recipes

`src/main/resources/daten/fusion-data.json` is sourced from **[SkyShards](https://github.com/Campionnn/SkyShards)** by Campion, the data source behind [skyshards.com](https://skyshards.com/).

The file is consumed as provided and refreshed weekly by the `daten-sync` workflow.

SkyShards is licensed under the MIT License:

```text
MIT License

Copyright (c) 2026 Campion

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Bazaar Prices

Prices are retrieved from the public Hypixel API endpoint:

`https://api.hypixel.net/v2/skyblock/bazaar`

No API key is required. Fusion 420 does not send account data or player information to this endpoint.

## Fabric Project Template

`gradlew`, `gradlew.bat`, and `gradle/wrapper/` originate from [FabricMC/fabric-example-mod](https://github.com/FabricMC/fabric-example-mod), tag `26.1.2`, licensed under CC0-1.0.
