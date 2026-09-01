package de.kamma.fusion420.daten;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wacht ueber die mitgelieferte Rezeptdatei. Sie wird von einem Workflow
 * automatisch nachgezogen — ohne diese Pruefung koennte eine kaputte oder
 * leere Fassung unbemerkt ins Release wandern.
 */
class MitgelieferteDatenTest {

	@Test
	void mitgelieferteDatenSindBrauchbar() throws Exception {
		String json;
		try (InputStream strom = MitgelieferteDatenTest.class.getResourceAsStream("/daten/fusion-data.json")) {
			assertNotNull(strom, "fusion-data.json fehlt in den Ressourcen");
			json = new String(strom.readAllBytes(), StandardCharsets.UTF_8);
		}

		Rezeptbuch buch = Rezeptbuch.ausJson(json);

		assertTrue(buch.shardAnzahl() >= 300,
				"zu wenige Shards: " + buch.shardAnzahl());
		assertTrue(buch.rezeptAnzahl() >= 100_000,
				"zu wenige Rezepte: " + buch.rezeptAnzahl());

		for (Shard shard : buch.alleShards()) {
			assertTrue(shard.bazaarId().startsWith("SHARD_"),
					shard.name() + " hat keine Bazaar-Kennung: " + shard.bazaarId());
			assertTrue(shard.fusionsMenge() >= 1,
					shard.name() + " hat eine unbrauchbare Fusionsmenge");
		}
	}
}
