package de.kamma.fusion420.daten;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RezeptbuchTest {

	private static final String JSON = """
			{
			  "shards": {
			    "A": {"name":"Alpha","family":"Bug Family","type":"Combat","rarity":"common",
			          "fuse_amount":2,"internal_id":"SHARD_ALPHA"},
			    "B": {"name":"Beta","family":"Bird Family","type":"Fishing","rarity":"rare",
			          "fuse_amount":5,"internal_id":"SHARD_BETA"},
			    "Z": {"name":"Zeta","family":"Demon Family","type":"Mining","rarity":"epic",
			          "fuse_amount":1,"internal_id":"SHARD_ZETA"}
			  },
			  "recipes": {
			    "Z": {"2": [["A","B"]], "1": [["A","A"]]},
			    "B": {"1": [["A","Z"]]}
			  }
			}
			""";

	@Test
	void liestShardsUndRezepte() {
		Rezeptbuch buch = Rezeptbuch.ausJson(JSON);
		assertEquals(3, buch.shardAnzahl());
		assertEquals(3, buch.rezeptAnzahl());
		assertEquals(2, buch.shard("A").fusionsMenge());
		assertEquals("SHARD_BETA", buch.shard("B").bazaarId());
	}

	@Test
	void findetFusionUnabhaengigVonDerReihenfolge() {
		Rezeptbuch buch = Rezeptbuch.ausJson(JSON);
		List<Rezept> vorwaerts = buch.fusionen("A", "B");
		List<Rezept> rueckwaerts = buch.fusionen("B", "A");

		assertEquals(1, vorwaerts.size());
		assertEquals(1, rueckwaerts.size());
		assertEquals("Zeta", vorwaerts.get(0).ausgabe().name());
		assertEquals("Zeta", rueckwaerts.get(0).ausgabe().name());
		assertEquals(2, vorwaerts.get(0).ausgabeMenge());
	}

	@Test
	void findetFusionEinesShardsMitSichSelbst() {
		Rezeptbuch buch = Rezeptbuch.ausJson(JSON);
		List<Rezept> treffer = buch.fusionen("A", "A");
		assertEquals(1, treffer.size());
		assertEquals("Zeta", treffer.get(0).ausgabe().name());
		assertEquals(1, treffer.get(0).ausgabeMenge());
	}

	@Test
	void liefertNichtsFuerUnbekanntePaare() {
		Rezeptbuch buch = Rezeptbuch.ausJson(JSON);
		assertTrue(buch.fusionen("B", "B").isEmpty());
		assertTrue(buch.fusionen("A", "XYZ").isEmpty());
	}

	@Test
	void loestSpielnamenMitFarbcodesAuf() {
		Rezeptbuch buch = Rezeptbuch.ausJson(JSON);
		assertNotNull(buch.nachAnzeigename("§fAlpha Shard"));
		assertEquals("A", buch.nachAnzeigename("§fAlpha Shard").schluessel());
		assertEquals("A", buch.nachAnzeigename("Alpha").schluessel());
		assertEquals("B", buch.nachAnzeigename("§9§lBETA SHARD").schluessel());
		assertNull(buch.nachAnzeigename("Diamond Sword"));
	}

	@Test
	void normalisiertNamen() {
		assertEquals("alpha", Rezeptbuch.normalisiere("§fAlpha Shard"));
		assertEquals("kingcod", Rezeptbuch.normalisiere("King Cod"));
		// "Shard" allein darf nicht zu einer leeren Kennung schrumpfen.
		assertEquals("shard", Rezeptbuch.normalisiere("Shard"));
	}
}
