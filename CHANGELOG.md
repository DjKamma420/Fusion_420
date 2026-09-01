# Änderungen

Das Format folgt [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
die Nummerierung [Semantic Versioning](https://semver.org/lang/de/).

## [Unveröffentlicht]

### Hinzugefügt

- Profit-Overlay neben dem Fusion-Machine-GUI: die besten Kombinationen der
  vorhandenen Shards mit Kosten, Erlös, Gewinn und Rendite.
- Drei Preismodi (Sofort, Order, Gemischt), im Spiel mit **M** umschaltbar.
- Bazaar-Abruf über die schlüssellose Hypixel-API, zwischengespeichert,
  mit **R** erzwingbar.
- Rezeptdaten aus [SkyShards](https://github.com/Campionnn/SkyShards):
  321 Shards, über 130 000 Rezeptzeilen. Beim Start aus dem Netz geholt,
  lokal zwischengespeichert, mitgelieferte Fassung als Rückfall.
- Hinweis im Overlay, wenn Shards im GUI liegen, die die Rezeptdaten nicht
  kennen — so wird ein Hypixel-Update sichtbar, bevor es falsch rechnet.
- Anzeige, woher die Rezepte stammen (online, Zwischenspeicher, mitgeliefert).
- Prüfung auf neuere Freigaben beim Start, abschaltbar.
- Wöchentlicher Arbeitsablauf, der die Rezeptdaten nachzieht.
