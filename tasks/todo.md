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

## Offen

- [ ] **Repo auf GitHub anlegen** — die GitHub-App dieser Sitzung darf keine
      Repos erstellen (`403 Resource not accessible by integration`).
      Muss von Hand passieren, dann kann gepusht werden.
- [ ] **CI grün bekommen.** Der Gradle-Build lässt sich hier nicht ausführen:
      Java 21 statt 25, und `maven.fabricmc.net` ist vom Egress-Proxy gesperrt.
      Der Actions-Lauf ist der einzige Beweis, dass es übersetzt.
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

## Restrisiken im Code

Zwei Minecraft-Zugriffe konnte ich gegen keinen echten 26.1-Quelltext
belegen; beides sind langlebige Namen und im Fehlerfall Einzeiler:

- `AbstractContainerMenu#slots` in `gui/Fusionserkennung`
- `Minecraft#getCurrentServer()` und `ServerData#ip`, ebenda
