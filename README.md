# Fusion 420

Fabric-Client-Mod für **Minecraft 26.1.2**. Sie zeigt neben dem
Fusion-Machine-GUI in Hypixel Skyblock live an, welche Fusionen der Shards in
der Box tatsächlich Gewinn bringen.

Reine Anzeige. Die Mod klickt nichts, automatisiert nichts und schreibt
nichts ins Spiel.

## Was sie tut

Sobald das Fusions-GUI offen ist, liest die Mod die Shards darin aus, schlägt
für jedes Paar alle möglichen Fusionen nach, holt die Bazaar-Preise und
rechnet aus, was übrig bleibt. Rechts neben dem GUI erscheint ein Kasten mit
den besten Kombinationen:

```
Fusion 420  -  Sofort
1. 2x Grove + 5x Verdant
   -> 1x Phanpyre
   1,2M -> 1,8M
   +612k  (+51%)
2. ...
Preise 12s alt  -  Rezepte: online
M Modus wechseln  -  R neu laden
```

Kosten in Orange, Gewinn in Grün, Verlust in Rot.

## Einbauen in Prism Launcher

### 1. Instanz anlegen

1. Prism Launcher öffnen, oben links auf **Add Instance**.
2. Links **Vanilla** wählen, in der Versionsliste **26.1.2** anklicken.
   Falls sie fehlt: oben rechts den Haken bei *Releases* setzen und die Liste
   mit dem Knopf daneben neu laden.
3. Rechts bei **Mod Loader** auf **Fabric** klicken und die vorgeschlagene
   Version übernehmen (**0.19.3** oder neuer).
4. Der Instanz oben einen Namen geben, z. B. `Skyblock 26.1.2`, dann **OK**.

Fragt Prism nach Java, lass es die vorgeschlagene Fassung herunterladen —
*Settings → Java → Auto-detect* bzw. den angebotenen Download.

### 2. Fabric API installieren

Ohne sie startet Fusion 420 nicht.

1. Die Instanz anklicken, rechts auf **Edit**.
2. Links **Mods**, dann oben **Download mods**.
3. Nach `Fabric API` suchen, den Treffer von *FabricMC* auswählen,
   **Select mod for download**, dann unten **Review and confirm** → **OK**.

### 3. Fusion 420 installieren

**Weg A — über Modrinth** (sobald die Mod dort steht, siehe unten):
im selben Dialog **Download mods** nach `Fusion 420` suchen und wie eben
bestätigen.

**Weg B — Jar von Hand** (geht immer):

1. Auf der [Release-Seite](https://github.com/DjKamma420/Fusion_420/releases/latest)
   die Datei `fusion_420-<version>.jar` herunterladen.
   Nimm **nicht** die Datei mit `-sources` im Namen — das ist der Quelltext.
2. In Prism: Instanz → **Edit** → **Mods** → **Add file** → das Jar auswählen.
   Alternativ **Ordner öffnen** und das Jar in den geöffneten `mods`-Ordner
   ziehen.

### 4. Starten und prüfen

1. Instanz starten, mit deinem Konto einloggen.
2. **Multiplayer → Add Server**, Adresse `mc.hypixel.net`, verbinden.
3. Ins Skyblock, zur Fusion Machine bei Kysha in Galatea.
4. GUI öffnen — rechts daneben erscheint der Kasten.

### Wenn nichts erscheint

Der Reihe nach durchgehen:

1. **Läuft die Mod überhaupt?** Instanz → **Edit** → **Ordner öffnen** →
   `logs/latest.log` öffnen und nach `Fusion 420` suchen. Es muss eine Zeile
   `Fusion 420 0.1.0 bereit` geben. Fehlt sie, liegt das Jar nicht im
   `mods`-Ordner oder die Fabric API fehlt.
2. **Bist du auf Hypixel?** Die Mod schweigt auf anderen Servern. Zum Testen
   `nurAufHypixel` auf `false` setzen (siehe unten).
3. **Heißt das Menü anders?** Hypixel benennt Menüs gelegentlich um. In der
   Konfiguration `titelMuster` anpassen — `(?i)fusion` trifft alles, was
   „fusion" im Titel hat, unabhängig von Groß- und Kleinschreibung.
   `(?i).` trifft jedes Menü, gut zum Ausprobieren.

## Einstellen

Beim ersten Start entsteht
`einstellungen.json` im Ordner `config/fusion_420/` der Instanz
(Instanz → **Edit** → **Ordner öffnen** → `config/fusion_420/`).

Im Spiel wechselt **M** den Preismodus und **R** erzwingt einen neuen
Preisabruf. Änderungen an der Datei greifen nach einem Neustart.

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `preisModus` | `SOFORT` | `SOFORT`, `ORDER` oder `GEMISCHT` — siehe unten |
| `bazaarSteuerProzent` | `1.25` | Verkaufssteuer; sinkt mit Community-Upgrades |
| `mindestVolumenWoche` | `5000` | Ausgaben mit weniger Wochenumsatz fallen raus |
| `anzahlEintraege` | `3` | wie viele Kombinationen angezeigt werden |
| `aktualisierungSekunden` | `60` | Abstand der Bazaar-Abfragen |
| `titelMuster` | `(?i)fusion` | Regex auf den GUI-Titel |
| `nurAufHypixel` | `true` | anderswo bleibt die Mod stumm |
| `nurContainerSlots` | `true` | nur die Kiste, nicht das eigene Inventar |
| `overlayVersatzX` / `-Y` / `overlayBreite` | `6` / `0` / `200` | Lage und Breite des Kastens |
| `rezepteOnlineLaden` | `true` | Rezepte beim Start frisch holen |
| `aufAktualisierungPruefen` | `true` | beim Start nach neueren Freigaben sehen |
| `tasteModusWechsel` / `tasteAktualisieren` | `77` / `82` | GLFW-Tastencodes (M / R) |

### Die drei Preismodi

| Modus | Einkauf | Verkauf | wofür |
|---|---|---|---|
| `SOFORT` | Sofortkauf | Sofortverkauf | konservativ, sofort realisierbar |
| `ORDER` | Kaufauftrag | Verkaufsangebot | größere Spanne, braucht Geduld und Order-Slots |
| `GEMISCHT` | Sofortkauf | Verkaufsangebot | der übliche Mittelweg |

Die Wahl verändert das Ergebnis stärker als alles andere. `SOFORT` ist der
Gewinn, den man wirklich in der Hand hält.

## Aktualisieren

Drei Dinge veralten unabhängig voneinander. Nur eines davon macht Arbeit.

### Rezepte — läuft von allein

Hypixel ändert die Fusionstabelle mit jedem Inhaltsupdate. Deshalb steckt sie
nicht fest im Jar: ein Arbeitsablauf zieht sie **wöchentlich** aus
[SkyShards](https://github.com/Campionnn/SkyShards) nach, und die Mod holt sie
bei jedem Start frisch aus diesem Repo. Kein Neuinstallieren nötig.

Kommt kein Netz zustande, greift der lokale Zwischenspeicher, danach die im
Jar mitgelieferte Fassung. Welche gerade gilt, steht im Overlay hinter
„Rezepte:".

Liegen im GUI Shards, die die Rezeptdaten nicht kennen, warnt das Overlay:

```
! 3 unbekannte Shards - Rezepte veraltet?
```

Dann ist entweder gerade ein Hypixel-Update erschienen und der wöchentliche
Abgleich noch nicht gelaufen, oder es gab keinen Netzzugriff. Ein
[Fehlerbericht](https://github.com/DjKamma420/Fusion_420/issues) hilft.

### Die Mod selbst — ein Hinweis, ein Handgriff

Beim Start sieht die Mod nach, ob es eine neuere Freigabe gibt. Falls ja,
steht im Overlay:

```
! Version 0.2.0 verfuegbar
```

Zum Aktualisieren das neue Jar von der Release-Seite laden und das alte im
`mods`-Ordner ersetzen. Über Modrinth installiert, erledigt das Prism selbst
(*Edit → Mods → Check for updates*).

Abschaltbar über `aufAktualisierungPruefen`.

### Neue Minecraft-Version — braucht eine neue Fassung

Die Mod erklärt in `fabric.mod.json` `"minecraft": "~26.1.2"`. Das heißt:
sie läuft auf **26.1.x**, aber Fabric verweigert den Start auf 26.2. Das ist
Absicht — Minecraft ändert seine Oberflächen-Schnittstellen regelmäßig, und
eine Mod, die trotzdem startet, stürzt dann mitten im Spiel ab.

Für eine neue Version:

1. In `gradle.properties` `minecraft_version`, `loader_version`,
   `loom_version` und `fabric_api_version` auf die Werte von
   [fabricmc.net/develop](https://fabricmc.net/develop) heben.
2. In `fabric.mod.json` `minecraft` und ggf. `java` anpassen.
3. `./gradlew build` — was sich geändert hat, sagt der Übersetzer.
   Erfahrungsgemäß trifft es zuerst `gui/Overlay` und `mixin/`.
4. Tag `vX.Y.Z` setzen; der Rest läuft von allein.

Wie groß solche Brüche sein können, zeigt 26.1: dort verschwanden die
Mappings, `HandledScreen` hieß plötzlich `AbstractContainerScreen`, und die
gesamte Zeichen-Schnittstelle wurde auf einen Auslesedurchgang umgestellt.

## Verträglich mit anderen Mods

Gezeichnet wird im Ereignis `ScreenEvents.afterExtract` der Fabric API, nicht
durch einen eigenen Eingriff in den Renderweg. Minecraft 26.1 baut die
Oberfläche in einem Auslesedurchgang zusammen; `afterExtract` ist dessen Ende
und liegt damit über allem — Hintergrund, Gegenständen, Text und Tooltips. An
dasselbe Ereignis können sich beliebig viele Mods hängen, dort geht niemandem
etwas verloren. Der einzige Mixin ist ein `@Accessor` auf die Maße des GUI —
der ändert keinen Kontrollfluss und kann mit nichts kollidieren.

Getestet werden sollte trotzdem gegen SkyHanni und NEU, sobald beide für
26.1.2 vorliegen.

## Was die Mod über dich preisgibt

Nichts. Sie liest das GUI, das ohnehin auf dem Bildschirm steht, und ruft drei
öffentliche Adressen ab:

| Adresse | wofür | was mitgeht |
|---|---|---|
| `api.hypixel.net/v2/skyblock/bazaar` | Preise | nichts, kein Schlüssel nötig |
| `raw.githubusercontent.com/.../fusion-data.json` | Rezepte | nichts |
| `api.github.com/repos/.../releases/latest` | Versionshinweis | nichts |

Kein Konto, kein Spielername, keine Kennung. Alles außer dem Bazaar-Abruf
lässt sich abschalten (`rezepteOnlineLaden`, `aufAktualisierungPruefen`).

## Regeln

Hypixel erlaubt Mods, die nur anzeigen. Diese hier liest ausschließlich das
GUI und öffentliche Marktdaten. Sie klickt nicht, bewegt nichts und tut
nichts, was ein Mensch nicht auch tun könnte. Automatisierung wäre ein
Bannrund — die ist hier bewusst nicht drin.

## Selbst bauen

```
./gradlew build
```

Braucht **JDK 25**. Näheres in [CONTRIBUTING.md](CONTRIBUTING.md).

## Lizenz

[MIT](LICENSE) · Rezeptdaten aus [SkyShards](https://github.com/Campionnn/SkyShards)
(MIT), siehe [THIRD_PARTY.md](THIRD_PARTY.md).
