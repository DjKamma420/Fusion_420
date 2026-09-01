package de.kamma.fusion420;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.einstellungen.Einstellungen;
import de.kamma.fusion420.markt.Preisbuch;

import java.util.function.Supplier;

/**
 * Was das Overlay zum Arbeiten braucht, in einem Stueck.
 *
 * <p>Das Rezeptbuch kommt als {@link Supplier}, weil es beim Start noch
 * geladen wird — das Overlay soll derweil schon zeichnen koennen.
 */
public record Kontext(
		Einstellungen einstellungen,
		Preisbuch preisbuch,
		Supplier<Rezeptbuch> rezeptbuchZugriff,
		Runnable einstellungenSichern) {

	public Rezeptbuch rezeptbuch() {
		return rezeptbuchZugriff.get();
	}
}
