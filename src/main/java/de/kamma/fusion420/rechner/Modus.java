package de.kamma.fusion420.rechner;

import de.kamma.fusion420.markt.Preis;

/**
 * Welche Seite des Bazaars fuer Einkauf und Verkauf angesetzt wird.
 *
 * <p>Die Wahl entscheidet ueber das Ergebnis mehr als jede andere
 * Stellschraube, deshalb ist sie im Spiel umschaltbar statt fest verdrahtet.
 */
public enum Modus {

	/** Sofort kaufen, sofort verkaufen. Konservativ und ohne Wartezeit. */
	SOFORT("Sofort"),

	/** Kaufauftrag und Verkaufsangebot. Groessere Spanne, aber nur mit Geduld. */
	ORDER("Order"),

	/** Sofort kaufen, per Angebot verkaufen. Der uebliche Mittelweg. */
	GEMISCHT("Gemischt");

	private final String anzeige;

	Modus(String anzeige) {
		this.anzeige = anzeige;
	}

	public String anzeige() {
		return anzeige;
	}

	/** Was eine Einheit im Einkauf kostet. */
	public double einkauf(Preis p) {
		return switch (this) {
			case SOFORT, GEMISCHT -> p.sofortKaufPreis();
			case ORDER -> p.sofortVerkaufPreis();
		};
	}

	/** Was eine Einheit im Verkauf einbringt, vor Steuer. */
	public double verkauf(Preis p) {
		return switch (this) {
			case SOFORT -> p.sofortVerkaufPreis();
			case ORDER, GEMISCHT -> p.sofortKaufPreis();
		};
	}

	public Modus naechster() {
		Modus[] alle = values();
		return alle[(ordinal() + 1) % alle.length];
	}

	public static Modus ausText(String text) {
		if (text != null) {
			for (Modus m : values()) {
				if (m.name().equalsIgnoreCase(text)) {
					return m;
				}
			}
		}
		return SOFORT;
	}
}
