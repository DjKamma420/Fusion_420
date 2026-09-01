package de.kamma.fusion420.rechner;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.markt.Preis;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GewinnrechnerTest {

	private static final String JSON = """
			{
			  "shards": {
			    "A": {"name":"Alpha","fuse_amount":2,"internal_id":"SHARD_ALPHA"},
			    "B": {"name":"Beta","fuse_amount":5,"internal_id":"SHARD_BETA"},
			    "Z": {"name":"Zeta","fuse_amount":1,"internal_id":"SHARD_ZETA"},
			    "M": {"name":"Mager","fuse_amount":1,"internal_id":"SHARD_MAGER"}
			  },
			  "recipes": {
			    "Z": {"2": [["A","B"]]},
			    "M": {"1": [["A","B"]]}
			  }
			}
			""";

	private static final Rezeptbuch BUCH = Rezeptbuch.ausJson(JSON);

	private static Map<String, Preis> preise() {
		return Map.of(
				"SHARD_ALPHA", new Preis(100.0, 80.0, 100_000L, 100_000L),
				"SHARD_BETA", new Preis(10.0, 8.0, 100_000L, 100_000L),
				"SHARD_ZETA", new Preis(500.0, 400.0, 90_000L, 80_000L),
				"SHARD_MAGER", new Preis(600.0, 500.0, 40L, 30L));
	}

	private static List<Shard> vorhanden() {
		return List.of(BUCH.shard("A"), BUCH.shard("B"));
	}

	@Test
	void rechnetSofortmodusMitMengenUndSteuer() {
		// Kosten: 2 Alpha zu 100 + 5 Beta zu 10 = 250
		// Erloes: 2 Zeta zu 400, davon 1,25 Prozent Steuer ab = 790
		List<Fusion> beste = Gewinnrechner.beste(vorhanden(), BUCH, preise(),
				Modus.SOFORT, 1.25, 1_000L, 3);

		assertEquals(1, beste.size(), "Mager faellt am Volumenfilter aus");
		Fusion f = beste.get(0);
		assertEquals("Zeta", f.rezept().ausgabe().name());
		assertEquals(250.0, f.kosten(), 1e-9);
		assertEquals(790.0, f.erloes(), 1e-9);
		assertEquals(540.0, f.gewinn(), 1e-9);
		assertEquals(540.0 / 250.0, f.rendite(), 1e-9);
	}

	@Test
	void ohneSteuerBleibtDerVolleErloes() {
		Fusion f = Gewinnrechner.beste(vorhanden(), BUCH, preise(), Modus.SOFORT, 0.0, 1_000L, 3).get(0);
		assertEquals(800.0, f.erloes(), 1e-9);
		assertEquals(550.0, f.gewinn(), 1e-9);
	}

	@Test
	void ordermodusKauftGuenstigerUndVerkauftTeurer() {
		// Einkauf zum Kaufauftrag: 2 mal 80 + 5 mal 8 = 200
		// Verkauf zum Angebot: 2 mal 500 = 1000
		Fusion f = Gewinnrechner.beste(vorhanden(), BUCH, preise(), Modus.ORDER, 0.0, 1_000L, 3).get(0);
		assertEquals(200.0, f.kosten(), 1e-9);
		assertEquals(1000.0, f.erloes(), 1e-9);
		assertEquals(800.0, f.gewinn(), 1e-9);
	}

	@Test
	void gemischtKauftSofortUndVerkauftPerAngebot() {
		Fusion f = Gewinnrechner.beste(vorhanden(), BUCH, preise(), Modus.GEMISCHT, 0.0, 1_000L, 3).get(0);
		assertEquals(250.0, f.kosten(), 1e-9);
		assertEquals(1000.0, f.erloes(), 1e-9);
		assertEquals(750.0, f.gewinn(), 1e-9);
	}

	@Test
	void volumenfilterLaesstDuenneMaerkteDurchWennErAusIst() {
		List<Fusion> beste = Gewinnrechner.beste(vorhanden(), BUCH, preise(), Modus.SOFORT, 0.0, 0L, 5);
		assertEquals(2, beste.size());
		// Nach Gewinn absteigend: Zeta (800 minus 250) vor Mager (500 minus 250).
		assertEquals("Zeta", beste.get(0).rezept().ausgabe().name());
		assertEquals("Mager", beste.get(1).rezept().ausgabe().name());
		assertTrue(beste.get(0).gewinn() > beste.get(1).gewinn());
	}

	@Test
	void begrenztAufDieGewuenschteAnzahl() {
		assertEquals(1, Gewinnrechner.beste(vorhanden(), BUCH, preise(), Modus.SOFORT, 0.0, 0L, 1).size());
	}

	@Test
	void ueberspringtShardsOhnePreis() {
		Map<String, Preis> luecke = Map.of(
				"SHARD_ALPHA", new Preis(100.0, 80.0, 100_000L, 100_000L),
				"SHARD_ZETA", new Preis(500.0, 400.0, 90_000L, 80_000L));
		assertTrue(Gewinnrechner.beste(vorhanden(), BUCH, luecke, Modus.SOFORT, 0.0, 0L, 3).isEmpty());
	}

	@Test
	void bleibtRuhigOhneEingaben() {
		assertTrue(Gewinnrechner.beste(List.of(), BUCH, preise(), Modus.SOFORT, 0.0, 0L, 3).isEmpty());
		assertTrue(Gewinnrechner.beste(vorhanden(), BUCH, Map.of(), Modus.SOFORT, 0.0, 0L, 3).isEmpty());
		assertTrue(Gewinnrechner.beste(vorhanden(), null, preise(), Modus.SOFORT, 0.0, 0L, 3).isEmpty());
	}

	@Test
	void modusLaeuftImKreis() {
		assertEquals(Modus.ORDER, Modus.SOFORT.naechster());
		assertEquals(Modus.GEMISCHT, Modus.ORDER.naechster());
		assertEquals(Modus.SOFORT, Modus.GEMISCHT.naechster());
		assertEquals(Modus.ORDER, Modus.ausText("order"));
		assertEquals(Modus.SOFORT, Modus.ausText("quatsch"));
	}
}
