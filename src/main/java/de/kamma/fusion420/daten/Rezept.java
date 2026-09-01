package de.kamma.fusion420.daten;

/**
 * Eine Fusion: zwei Eingabe-Shards ergeben {@code ausgabeMenge} Stueck der Ausgabe.
 *
 * <p>Die Reihenfolge der Eingaben ist fuer den Gewinn ohne Bedeutung; das
 * Rezeptbuch liefert sie stets nach Schluessel sortiert.
 */
public record Rezept(Shard eingabeA, Shard eingabeB, Shard ausgabe, int ausgabeMenge) {
}
