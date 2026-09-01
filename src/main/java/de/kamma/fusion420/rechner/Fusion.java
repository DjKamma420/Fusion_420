package de.kamma.fusion420.rechner;

import de.kamma.fusion420.daten.Rezept;

/**
 * Ein durchgerechnetes Rezept.
 *
 * @param kosten  was die Eingaben zusammen kosten
 * @param erloes  was die Ausgabe einbringt, Steuer bereits abgezogen
 * @param gewinn  {@code erloes - kosten}
 * @param rendite {@code gewinn / kosten}
 * @param volumenWoche knappste Wochenmenge der Ausgabe, als Mass fuer Liquiditaet
 */
public record Fusion(
		Rezept rezept,
		double kosten,
		double erloes,
		double gewinn,
		double rendite,
		long volumenWoche) {
}
