package de.kamma.fusion420.wartung;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Sieht einmal beim Start nach, ob es eine neuere Freigabe gibt.
 *
 * <p>Ohne das merkt niemand, wenn eine Fassung veraltet ist — und veraltet
 * heisst hier nicht nur "fehlende Verbesserungen", sondern womoeglich falsche
 * Gewinne, weil Hypixel die Fusionstabelle geaendert hat.
 */
public final class Versionspruefung {

	private static final String ENDPUNKT =
			"https://api.github.com/repos/DjKamma420/Fusion_420/releases/latest";

	private Versionspruefung() {
	}

	/**
	 * @return die neuere Versionsnummer, oder {@code null} wenn die eigene
	 *         aktuell ist oder nichts zu erreichen war
	 */
	public static CompletableFuture<String> neuereFassung(String eigeneVersion) {
		HttpClient klient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		HttpRequest anfrage = HttpRequest.newBuilder(URI.create(ENDPUNKT))
				.timeout(Duration.ofSeconds(15))
				.header("Accept", "application/vnd.github+json")
				.header("User-Agent", "fusion_420 (Fabric client mod)")
				.GET()
				.build();

		return klient.sendAsync(anfrage, HttpResponse.BodyHandlers.ofString())
				.thenApply(antwort -> {
					// 404 heisst schlicht: es gibt noch keine Freigabe.
					if (antwort.statusCode() != 200) {
						return null;
					}
					JsonObject o = JsonParser.parseString(antwort.body()).getAsJsonObject();
					if (!o.has("tag_name")) {
						return null;
					}
					String neueste = o.get("tag_name").getAsString();
					return istNeuer(neueste, eigeneVersion) ? bereinige(neueste) : null;
				})
				.exceptionally(fehler -> null);
	}

	/**
	 * Vergleicht zwei Versionsnummern Zahl fuer Zahl.
	 *
	 * <p>Bewusst nicht ueber Zeichenkettenvergleich: "0.10.0" ist neuer als
	 * "0.9.0", alphabetisch waere es umgekehrt.
	 */
	public static boolean istNeuer(String kandidat, String bezug) {
		if (kandidat == null || bezug == null) {
			return false;
		}
		int[] a = teile(kandidat);
		int[] b = teile(bezug);
		for (int i = 0; i < Math.max(a.length, b.length); i++) {
			int links = i < a.length ? a[i] : 0;
			int rechts = i < b.length ? b[i] : 0;
			if (links != rechts) {
				return links > rechts;
			}
		}
		return false;
	}

	/** Schneidet ein fuehrendes "v" und alles ab einem Bindestrich weg. */
	static String bereinige(String version) {
		String v = version.trim();
		if (v.startsWith("v") || v.startsWith("V")) {
			v = v.substring(1);
		}
		int strich = v.indexOf('-');
		return strich > 0 ? v.substring(0, strich) : v;
	}

	private static int[] teile(String version) {
		String[] stuecke = bereinige(version).split("\\.");
		int[] zahlen = new int[stuecke.length];
		for (int i = 0; i < stuecke.length; i++) {
			try {
				zahlen[i] = Integer.parseInt(stuecke[i].replaceAll("\\D.*$", ""));
			} catch (NumberFormatException e) {
				zahlen[i] = 0;
			}
		}
		return zahlen;
	}
}
