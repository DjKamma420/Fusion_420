package de.kamma.fusion420.daten;

import de.kamma.fusion420.einstellungen.Einstellungen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Beschafft die Rezeptdaten — in dieser Reihenfolge: aus dem Netz, sonst aus
 * dem lokalen Zwischenspeicher, sonst aus der mitgelieferten Fassung.
 *
 * <p>Hypixel aendert die Fusionstabelle mit jedem Inhaltsupdate. Ohne den
 * Abruf aus dem Netz waere die Mod schon nach wenigen Wochen falsch, und ein
 * neues Release nur wegen einer Datendatei ist Unfug.
 */
public final class Rezeptquelle {

	private static final Logger LOG = LoggerFactory.getLogger("Fusion 420");
	private static final String MITGELIEFERT = "/daten/fusion-data.json";

	private Rezeptquelle() {
	}

	public static CompletableFuture<Rezeptbuch> laden(Einstellungen einstellungen, Path zwischenspeicher) {
		ExecutorService faden = Executors.newSingleThreadExecutor(auftrag -> {
			Thread t = new Thread(auftrag, "fusion_420-rezepte");
			t.setDaemon(true);
			return t;
		});
		return CompletableFuture
				.supplyAsync(() -> beschaffen(einstellungen, zwischenspeicher), faden)
				.whenComplete((ergebnis, fehler) -> faden.shutdown());
	}

	private static Rezeptbuch beschaffen(Einstellungen einstellungen, Path zwischenspeicher) {
		if (einstellungen.rezepteOnlineLaden) {
			try {
				String json = ausDemNetz(einstellungen.rezeptQuelleUrl);
				Rezeptbuch buch = Rezeptbuch.ausJson(json);
				ablegen(zwischenspeicher, json);
				LOG.info("Rezepte aus dem Netz geladen: {} Shards, {} Rezepte",
						buch.shardAnzahl(), buch.rezeptAnzahl());
				return buch;
			} catch (Exception e) {
				LOG.warn("Rezepte konnten nicht aus dem Netz geladen werden ({}), nutze Zwischenspeicher",
						e.toString());
			}
		}

		try {
			if (Files.isRegularFile(zwischenspeicher)) {
				Rezeptbuch buch = Rezeptbuch.ausJson(Files.readString(zwischenspeicher, StandardCharsets.UTF_8));
				LOG.info("Rezepte aus dem Zwischenspeicher geladen: {} Rezepte", buch.rezeptAnzahl());
				return buch;
			}
		} catch (Exception e) {
			LOG.warn("Zwischenspeicher unbrauchbar ({}), nutze mitgelieferte Fassung", e.toString());
		}

		try (InputStream strom = Rezeptquelle.class.getResourceAsStream(MITGELIEFERT)) {
			if (strom == null) {
				throw new IllegalStateException("Mitgelieferte Rezeptdaten fehlen im Jar");
			}
			Rezeptbuch buch = Rezeptbuch.ausJson(new String(strom.readAllBytes(), StandardCharsets.UTF_8));
			LOG.info("Mitgelieferte Rezepte geladen: {} Rezepte", buch.rezeptAnzahl());
			return buch;
		} catch (Exception e) {
			throw new IllegalStateException("Rezeptdaten sind aus keiner Quelle ladbar", e);
		}
	}

	private static String ausDemNetz(String url) throws Exception {
		HttpClient klient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		HttpRequest anfrage = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(30))
				.header("Accept", "application/json")
				.header("User-Agent", "fusion_420 (Fabric client mod)")
				.GET()
				.build();
		HttpResponse<String> antwort = klient.send(anfrage, HttpResponse.BodyHandlers.ofString());
		if (antwort.statusCode() != 200) {
			throw new IllegalStateException("HTTP " + antwort.statusCode());
		}
		return antwort.body();
	}

	private static void ablegen(Path ziel, String json) {
		try {
			Path ordner = ziel.getParent();
			if (ordner != null) {
				Files.createDirectories(ordner);
			}
			Files.writeString(ziel, json, StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOG.warn("Zwischenspeicher liess sich nicht schreiben: {}", e.toString());
		}
	}
}
