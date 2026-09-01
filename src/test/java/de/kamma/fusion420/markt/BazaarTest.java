package de.kamma.fusion420.markt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarTest {

	private static final String ANTWORT = """
			{
			  "success": true,
			  "lastUpdated": 1770000000000,
			  "products": {
			    "SHARD_GROVE": {
			      "product_id": "SHARD_GROVE",
			      "quick_status": {
			        "productId": "SHARD_GROVE",
			        "sellPrice": 812.3, "sellVolume": 42, "sellMovingWeek": 120000,
			        "buyPrice": 941.7, "buyVolume": 99, "buyMovingWeek": 98000
			      }
			    },
			    "ENCHANTED_DIAMOND": {
			      "quick_status": {"sellPrice": 1.0, "buyPrice": 2.0,
			                       "sellMovingWeek": 5, "buyMovingWeek": 5}
			    },
			    "SHARD_KAPUTT": {"product_id": "SHARD_KAPUTT"}
			  }
			}
			""";

	@Test
	void liestNurShardsUndDeutetDiePreiseRichtig() {
		Map<String, Preis> preise = Bazaar.auswerten(ANTWORT);

		assertEquals(1, preise.size(), "nur Shards, und nur solche mit quick_status");
		assertTrue(preise.containsKey("SHARD_GROVE"));
		assertFalse(preise.containsKey("ENCHANTED_DIAMOND"));
		assertFalse(preise.containsKey("SHARD_KAPUTT"));

		Preis p = preise.get("SHARD_GROVE");
		assertEquals(941.7, p.sofortKaufPreis(), 1e-9);
		assertEquals(812.3, p.sofortVerkaufPreis(), 1e-9);
		assertEquals(98_000L, p.kaufVolumenWoche());
		assertEquals(120_000L, p.verkaufVolumenWoche());
		assertEquals(98_000L, p.engpassVolumenWoche());
		assertTrue(p.handelbar());
	}

	@Test
	void meldetMisserfolgDerApi() {
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> Bazaar.auswerten("{\"success\":false,\"cause\":\"Kein Zugriff\"}"));
		assertTrue(e.getMessage().contains("Kein Zugriff"));
	}

	@Test
	void meldetFehlendesProduktfeld() {
		assertThrows(IllegalStateException.class, () -> Bazaar.auswerten("{\"success\":true}"));
	}

	@Test
	void unhandelbarOhnePreis() {
		assertFalse(new Preis(0.0, 5.0, 10L, 10L).handelbar());
		assertFalse(new Preis(5.0, 0.0, 10L, 10L).handelbar());
	}
}
