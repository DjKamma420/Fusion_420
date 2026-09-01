package de.kamma.fusion420.markt;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Woher Preise kommen. Bewusst als Naht angelegt, damit neben dem Bazaar
 * spaeter eine weitere Quelle treten koennte, ohne den Rechner anzufassen.
 */
public interface Preisquelle {

	/** Preise je Produkt-Kennung, etwa {@code SHARD_GROVE}. */
	CompletableFuture<Map<String, Preis>> abrufen();

	String bezeichnung();
}
