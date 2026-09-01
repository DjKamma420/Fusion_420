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
Preise 12s alt  -  M Modus, R neu
```

Kosten in Orange, Gewinn in Grün, Verlust in Rot.

## Einbauen

Gebraucht werden **Fabric Loader 0.19.3** oder neuer für **Minecraft 26.1.2**
und die **Fabric API**.

### Prism Launcher

1. Instanz mit Minecraft `26.1.2` und Fabric anlegen.
2. *Bearbeiten → Mods → Mods hinzufügen* → nach `Fusion 420` suchen
   (sobald die Mod auf Modrinth steht), oder:
3. Das Jar aus dem
   [neuesten Release](https://github.com/DjKamma420/Fusion_420/releases/latest)
   laden und über *Bearbeiten → Mods → Ordner öffnen* dort ablegen.
4. Die **Fabric API** nicht vergessen — ohne sie startet die Mod nicht.

## Einstellen

Beim ersten Start entsteht
`.minecraft/config/fusion_420/einstellungen.json`. Im Spiel wechselt **M**
den Preismodus und **R** erzwingt einen neuen Preisabruf.

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
| `tasteModusWechsel` / `tasteAktualisieren` | `77` / `82` | GLFW-Tastencodes (M / R) |

### Die drei Preismodi

| Modus | Einkauf | Verkauf | wofür |
|---|---|---|---|
| `SOFORT` | Sofortkauf | Sofortverkauf | konservativ, sofort realisierbar |
| `ORDER` | Kaufauftrag | Verkaufsangebot | größere Spanne, braucht Geduld und Order-Slots |
| `GEMISCHT` | Sofortkauf | Verkaufsangebot | der übliche Mittelweg |

Die Wahl verändert das Ergebnis stärker als alles andere. `SOFORT` ist der
Gewinn, den man wirklich in der Hand hält.

## Verträglich mit anderen Mods

Gezeichnet wird im Ereignis `ScreenEvents.afterExtract` der Fabric API, nicht
durch einen eigenen Eingriff in den Renderweg. Minecraft 26.1 baut die
Oberfläche in einem Auslesedurchgang zusammen; `afterExtract` ist dessen Ende
und liegt damit über allem — Hintergrund, Gegenständen, Text und Tooltips. An
dasselbe Ereignis können sich beliebig viele Mods hängen, dort geht niemandem
etwas verloren. Der einzige Mixin ist ein `@Accessor` auf die Maße des GUI —
der ändert keinen Kontrollfluss und kann mit nichts kollidieren.

## Rezeptdaten

Die Hypixel-API liefert **keine** Fusionsrezepte. Sie kommen aus
[SkyShards](https://github.com/Campionnn/SkyShards) (MIT, siehe
[THIRD_PARTY.md](THIRD_PARTY.md)): 321 Shards, über 130 000 Rezeptzeilen.
Ein Workflow zieht die Datei wöchentlich nach, die Mod holt sie beim Start
aus diesem Repo und fällt auf die mitgelieferte Fassung zurück, wenn kein
Netz da ist.

## Selbst bauen

```
./gradlew build
```

Braucht **JDK 25**. Das Jar liegt danach in `build/libs/`.

## Regeln

Hypixel erlaubt Mods, die nur anzeigen. Diese hier liest ausschließlich das
GUI, das ohnehin auf dem Bildschirm steht, und öffentliche Marktdaten. Sie
klickt nicht, bewegt nichts und tut nichts, was ein Mensch nicht auch tun
könnte. Automatisierung wäre ein Bannrund — die ist hier bewusst nicht drin.

## Lizenz

[MIT](LICENSE)
