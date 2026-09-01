package de.kamma.fusion420.rechner;

import de.kamma.fusion420.markt.Preis;

/** Preisbasis fuer die eingesetzten Shards. */
public enum Einkaufsmodus {
	SOFORT("Sofort", "Sofort kaufen"),
	ORDER("Order", "Kauforder"),
	GEFARMT("Gefarmt", "Eigene Shards (0 Coins)");

	private final String anzeige;
	private final String detail;

	Einkaufsmodus(String anzeige, String detail) {
		this.anzeige = anzeige;
		this.detail = detail;
	}

	public String anzeige() { return anzeige; }
	public String detail() { return detail; }

	public double preis(Preis p) {
		return switch (this) {
			case SOFORT -> p.sofortKaufPreis();
			case ORDER -> p.sofortVerkaufPreis();
			case GEFARMT -> 0.0;
		};
	}

	public Einkaufsmodus naechster() {
		Einkaufsmodus[] alle = values();
		return alle[(ordinal() + 1) % alle.length];
	}

	public static Einkaufsmodus ausText(String text) {
		if (text != null) {
			for (Einkaufsmodus m : values()) {
				if (m.name().equalsIgnoreCase(text)) return m;
			}
		}
		return SOFORT;
	}
}
