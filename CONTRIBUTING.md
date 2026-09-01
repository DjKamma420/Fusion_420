# Mitarbeiten

## Bauen

```
./gradlew build
```

Braucht **JDK 25**. Das Jar landet in `build/libs/`.

## Prüfen

```
./gradlew test
```

Der Kern — Rezeptindex, Gewinnrechnung, Bazaar-Auswertung, Versionsvergleich —
kommt bewusst ohne Minecraft aus und ist deshalb ohne laufendes Spiel prüfbar.
Was Minecraft anfasst (`gui/`, `mixin/`), prüft erst der Build in der CI und
danach der Blick ins Spiel.

Die CI weist die Testzahl aus und bricht ab, wenn null Tests liefen — ein
grüner Lauf ohne ausgeführte Tests wäre wertlos. Sie öffnet außerdem das
gebaute Jar und prüft Pflichteinträge, Mod-Kennung und eingesetzte Version.

## Aufbau

| Paket | Inhalt |
|---|---|
| `daten/` | Shards und Rezepte, Index, Beschaffung |
| `markt/` | Preisquellen, Bazaar, Zwischenspeicher |
| `rechner/` | Gewinnrechnung, Preismodi — ohne Minecraft |
| `gui/` | Erkennung des Fusions-GUI, Overlay |
| `mixin/` | ein einziger `@Accessor` auf die Maße des GUI |
| `wartung/` | Prüfung auf neuere Freigaben |

## Regeln

1. **Deutsche Bezeichner.** Kommentare erklären das Warum, nicht das Was.
2. **`rechner/` und `daten/` bleiben frei von Minecraft-Importen.** Das ist
   der Teil, der sich ohne Spiel prüfen lässt; er soll es bleiben.
3. **Kein `@Inject` in den Renderweg.** Gezeichnet wird über `ScreenEvents`.
   Ein eigener Eingriff in `AbstractContainerScreen` kollidiert mit anderen
   Oberflächen-Mods — das ist der Hauptgrund für den jetzigen Aufbau.
4. **Nichts automatisieren.** Die Mod zeigt an. Klicks, Bewegungen oder
   Eingaben zu simulieren wäre ein Bannrund und kommt nicht rein.
5. **Rezeptdaten nicht von Hand ändern.** `src/main/resources/daten/fusion-data.json`
   zieht der Arbeitsablauf `daten-sync` nach.

## Auf eine neue Minecraft-Version heben

Siehe den Abschnitt "Neue Minecraft-Version" in der [README](README.md).
