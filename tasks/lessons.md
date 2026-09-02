# Lehren

## Nie mit --force pushen, ohne vorher zu holen

**Vorfall, 02.09.2026.** Ein `git push --force` auf `main` hat 33 Commits
überschrieben, die zwischenzeitlich von einer parallelen Sitzung gepusht
worden waren — getrennte Ein- und Verkaufsmodi, verschiebbares Overlay,
Korrekturen an der MouseButtonEvent-API, Sicherheitsrichtlinie, komplette
Übersetzung ins Englische.

Die Ursache war nicht das Umschreiben der Historie, sondern der veraltete
Blick darauf: der zuletzt gesehene Fernstand war Stunden alt und wurde vor
dem Schieben nicht erneuert.

**Regel:** vor jedem Push, der Historie verändert, in dieser Reihenfolge

1. `git fetch origin <zweig>`
2. den geholten Stand mit dem erwarteten vergleichen; weicht er ab,
   **abbrechen und nachfragen** statt zu überschreiben
3. `git push --force-with-lease=<zweig>:<erwarteter Stand>` — niemals
   `--force` allein

`--force-with-lease` ohne expliziten erwarteten Stand genügt nicht, wenn
zwischendurch `filter-branch` gelaufen ist: das schreibt auch
`refs/remotes/origin/*` um und zerstört damit den Vergleichswert.

**Bevor eine Historie umgeschrieben wird:** immer erst ein Bündel anlegen
(`git bundle create rettung.bundle <zweig>`). Das hat den Schaden hier
begrenzt.

## Exitcodes prüfen, nicht auf `set -e` vertrauen

Eine kaputte YAML-Datei ist trotz `set -e` in einen Commit gelangt, weil der
fehlgeschlagene Prüfschritt den Abbruch nicht ausgelöst hat. Prüfschritte
brauchen einen echten Exitcode-Vergleich.

Und: `grep` liefert Exitcode 1, wenn es nichts findet. In einer Kette wie
`javac … | grep -v "^Picked up" || fehler=1` meldet das den **Erfolgsfall**
als Fehler. Exitcodes einzeln abfragen.

## Personendaten sind mit einem Umschreiben nicht weg

Nach dem Umschreiben halten weiterhin fest:

- Zweige und Tags, die auf die alte Historie zeigen
- **`refs/pull/*` — dauerhaft.** GitHub löscht sie nie; sie überleben das
  Schließen des Pull Requests und jedes Umschreiben.
- nicht mehr erreichbare Objekte, bis GitHub sie einsammelt

Vollständig entfernt wird das nur durch Löschen und Neuanlegen des Repos.
Deshalb: **vor dem ersten Push** die Kontoeinstellung "Keep my email
addresses private" prüfen, statt hinterher zu reparieren.
