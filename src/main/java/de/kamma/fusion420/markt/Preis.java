package de.kamma.fusion420.markt;

/**
 * Marktlage eines Bazaar-Produkts.
 *
 * <p>Zur Bedeutung der beiden Preise: Hypixels {@code quick_status.buyPrice}
 * ist der Preis, den man beim Sofortkauf <em>zahlt</em>, {@code sellPrice}
 * der, den man beim Sofortverkauf <em>bekommt</em>. {@code buyPrice} liegt
 * also stets ueber {@code sellPrice} — die Spanne dazwischen ist der Bereich,
 * in dem Auftraege liegen.
 */
public record Preis(
		double sofortKaufPreis,
		double sofortVerkaufPreis,
		long kaufVolumenWoche,
		long verkaufVolumenWoche) {

	public boolean handelbar() {
		return sofortKaufPreis > 0.0 && sofortVerkaufPreis > 0.0;
	}

	/** Die knappere der beiden Wochenmengen — die begrenzt einen Kreislauf. */
	public long engpassVolumenWoche() {
		return Math.min(kaufVolumenWoche, verkaufVolumenWoche);
	}
}
