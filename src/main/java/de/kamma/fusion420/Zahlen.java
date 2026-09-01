package de.kamma.fusion420;

import java.util.Locale;

/** Muenzbetraege kurz und lesbar, in deutscher Schreibweise. */
public final class Zahlen {

	private Zahlen() {
	}

	public static String kurz(double wert) {
		double betrag = Math.abs(wert);
		String vorzeichen = wert < 0 ? "-" : "";
		if (betrag >= 1_000_000_000.0) {
			return vorzeichen + format(betrag / 1_000_000_000.0) + "B";
		}
		if (betrag >= 1_000_000.0) {
			return vorzeichen + format(betrag / 1_000_000.0) + "M";
		}
		if (betrag >= 1_000.0) {
			return vorzeichen + format(betrag / 1_000.0) + "k";
		}
		return vorzeichen + String.format(Locale.GERMANY, "%.0f", betrag);
	}

	public static String mitVorzeichen(double wert) {
		return (wert > 0 ? "+" : "") + kurz(wert);
	}

	public static String prozent(double anteil) {
		return String.format(Locale.GERMANY, "%+.0f%%", anteil * 100.0);
	}

	/** Unter zehn eine Nachkommastelle, darueber waere sie nur Rauschen. */
	private static String format(double wert) {
		return wert < 10.0
				? String.format(Locale.GERMANY, "%.1f", wert)
				: String.format(Locale.GERMANY, "%.0f", wert);
	}
}
