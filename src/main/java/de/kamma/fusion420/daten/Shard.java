package de.kamma.fusion420.daten;

/**
 * Ein Attribute-Shard.
 *
 * @param schluessel  interner Schluessel der Rezeptdaten, z.B. {@code C1}
 * @param name        Anzeigename ohne das Wort "Shard", z.B. {@code Grove}
 * @param familie     Familie laut Rezeptdaten
 * @param art         Faehigkeitszweig, z.B. {@code Combat}
 * @param seltenheit  {@code common} bis {@code legendary}
 * @param fusionsMenge wie viele Stueck eine Fusion von diesem Shard verbraucht
 * @param bazaarId    Produkt-Kennung im Bazaar, z.B. {@code SHARD_GROVE}
 */
public record Shard(
		String schluessel,
		String name,
		String familie,
		String art,
		String seltenheit,
		int fusionsMenge,
		String bazaarId) {
}
