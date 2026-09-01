# Fremde Bestandteile

## Fusionsrezepte

`src/main/resources/daten/fusion-data.json` stammt aus **[SkyShards](https://github.com/Campionnn/SkyShards)** von
Campion, der Datenbasis hinter [skyshards.com](https://skyshards.com/).
Die Datei wird unveraendert uebernommen und vom Workflow `daten-sync`
woechentlich nachgezogen.

SkyShards steht unter der MIT-Lizenz:

```
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

## Preise

Preise kommen von der oeffentlichen Hypixel-API
(`https://api.hypixel.net/v2/skyblock/bazaar`). Der Endpunkt braucht keinen
API-Schluessel. Es werden keine Kontodaten gelesen und nichts uebertragen.

## Gerüst

`gradlew`, `gradlew.bat` und `gradle/wrapper/` stammen aus
[FabricMC/fabric-example-mod](https://github.com/FabricMC/fabric-example-mod)
(Tag `26.1.2`, CC0-1.0).
