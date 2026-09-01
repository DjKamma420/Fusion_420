package de.kamma.fusion420;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.einstellungen.Einstellungen;
import de.kamma.fusion420.markt.Preisbuch;

import java.util.function.Supplier;

/**
 * Was das Overlay zum Arbeiten braucht, in einem Stueck.
 *
 * <p>Rezeptbuch und Aktualisierungshinweis kommen als {@link Supplier}, weil
 * beide beim Start noch im Hintergrund geladen werden — das Overlay soll
 * derweil schon zeichnen koennen.
 */
public record Kontext(
		Einstellungen einstellungen,
		Preisbuch preisbuch,
		Supplier<Rezeptbuch> rezeptbuchZugriff,
		Supplier<String> aktualisierungZugriff,
		Runnable einstellungenSichern) {

	public Rezeptbuch rezeptbuch() {
		return rezeptbuchZugriff.get();
	}

	/** Versionsnummer einer neueren Freigabe, sonst {@code null}. */
	public String neuereFassung() {
		return aktualisierungZugriff.get();
	}
}
