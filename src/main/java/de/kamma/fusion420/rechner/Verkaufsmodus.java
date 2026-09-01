package de.kamma.fusion420.rechner;

import de.kamma.fusion420.markt.Preis;

/** Price basis for the output shards. */
public enum Verkaufsmodus {
	SOFORT("Instant Sell", "Sell instantly"),
	ORDER("Sell Order", "Sell via order");

	private final String anzeige;
	private final String detail;

	Verkaufsmodus(String anzeige, String detail) {
		this.anzeige = anzeige;
		this.detail = detail;
	}

	public String anzeige() { return anzeige; }
	public String detail() { return detail; }

	public double preis(Preis p) {
		return switch (this) {
			case SOFORT -> p.sofortVerkaufPreis();
			case ORDER -> p.sofortKaufPreis();
		};
	}

	public Verkaufsmodus naechster() {
		Verkaufsmodus[] alle = values();
		return alle[(ordinal() + 1) % alle.length];
	}

	public static Verkaufsmodus ausText(String text) {
		if (text != null) {
			for (Verkaufsmodus m : values()) {
				if (m.name().equalsIgnoreCase(text)) return m;
			}
		}
		return SOFORT;
	}
}
