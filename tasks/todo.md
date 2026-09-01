# Fusion 420 — Stand

## Erledigt

- [x] Projektgerüst: `build.gradle`, `settings.gradle`, `gradle.properties`,
      Gradle-Wrapper 9.5.1 (übernommen aus `fabric-example-mod` @ 26.1.2)
- [x] `fabric.mod.json` + `fusion_420.mixins.json` (`compatibilityLevel: JAVA_25`)
- [x] Versionen gegen den echten `fabric-example-mod` @ Tag 26.1.2 geprüft:
      MC 26.1.2 · Loader 0.19.3 · Loom 1.17-SNAPSHOT · Fabric API 0.155.2+26.1.2 · Java 25
- [x] `daten/` — Rezeptbuch mit sortiertem `long`-Index (130 554 Zeilen, ~1 MB,
      Aufbau 134 ms, 1035 Paarabfragen in 3 ms)
- [x] `markt/` — Bazaar über die schlüssellose Hypixel-API, asynchron, mit Cache
- [x] `rechner/` — Gewinnrechnung, drei Preismodi, Steuer, Volumenfilter
- [x] `gui/` — Overlay über `ScreenEvents.afterExtract`
- [x] `mixin/` — ein `@Accessor` auf `leftPos` / `topPos` / `imageWidth`
- [x] Render-API gegen den Quelltext von `FabricMC/fabric` Branch `26.1` belegt
      (nicht geraten): `afterExtract`, `GuiGraphicsExtractor`, `text(...)`,
      `AfterKeyPress(Screen, KeyEvent)`
- [x] 23 Tests, lokal grün — inklusive Parsen der echten 3,1-MB-Datendatei
- [x] Workflows `bauen`, `freigabe`, `daten-sync`
- [x] README, LICENSE (MIT), THIRD_PARTY (SkyShards, MIT)

- [x] Repo `DjKamma420/Fusion_420` angelegt (von Hand — die GitHub-App dieser
      Sitzung darf keine Repos erstellen) und bespielt
- [x] **CI grün beim ersten Lauf.** Damit ist belegt, dass der Gradle-Build
      übersetzt — das ließ sich hier nicht prüfen (Java 21 statt 25,
      `maven.fabricmc.net` vom Egress-Proxy gesperrt).
- [x] CI meldet jetzt die Testzahl und bricht ab, wenn null Tests laufen —
      sonst hätte ein leerer Lauf grün gemeldet.

## Offen

- [ ] **Modrinth** — Projekt anlegen, dann `MODRINTH_TOKEN` (Secret) und
      `MODRINTH_ID` (Variable) im Repo hinterlegen. Erst danach greift der
      Upload-Schritt in `freigabe.yml`; er ist bislang ungetestet.
- [ ] **Im Spiel prüfen.** Zwei Dinge lassen sich von außen nicht klären:
      der genaue Titel des Fusion-Machine-GUIs (deshalb Regex in der Konfig,
      Standard `(?i)fusion`) und ob Hypixel die Shards exakt als
      `<Name> Shard` benennt (Namensabgleich gegen die 321 Namen).

## Bewusst weggelassen

**Auktionshaus.** War im Plan vorgesehen, ist aber nachweislich überflüssig:
alle 321 Shards der Rezeptdatei haben eine `SHARD_*`-Bazaar-Kennung, keiner
läuft übers AH. Dazu kam, dass `sky.coflnet.com` vom Egress-Proxy dieser
Sitzung gesperrt ist — ich hätte die Schnittstelle nicht prüfen können und
hätte geratenen Code ausgeliefert. Stattdessen liegt die Naht bereit:
`markt/Preisquelle` ist eine Schnittstelle, `Bazaar` ihre einzige
Umsetzung. Eine zweite Quelle lässt sich ergänzen, ohne den Rechner
anzufassen.

## Erledigte Restrisiken

`AbstractContainerMenu#slots`, `Minecraft#getCurrentServer()` und
`ServerData#ip` waren gegen keinen echten 26.1-Quelltext belegbar. Der
erfolgreiche CI-Build beweist, dass es diese Namen gibt — sonst hätte der
Übersetzer sie angemahnt.

Was ein grüner Build **nicht** beweist: dass das Overlay im Spiel an der
richtigen Stelle erscheint und dass die Shard-Namen zu Hypixels Schreibweise
passen. Das entscheidet erst der erste Blick ins laufende Spiel.
