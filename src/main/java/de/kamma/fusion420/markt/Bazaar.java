package de.kamma.fusion420.markt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Der Bazaar von Hypixel. Der Endpunkt ist oeffentlich und braucht keinen
 * API-Schluessel; er liefert in einem Zug alle Produkte.
 *
 * <p>Diese Klasse protokolliert bewusst nicht und kennt Minecraft nicht —
 * so laesst sich {@link #auswerten(String)} in der CI ohne Spiel pruefen.
 */
public final class Bazaar implements Preisquelle {

	public static final String ENDPUNKT = "https://api.hypixel.net/v2/skyblock/bazaar";

	/** Nur Shards interessieren; alles andere waere Ballast im Speicher. */
	private static final String SHARD_PRAEFIX = "SHARD_";

	private final HttpClient klient;
	private final URI endpunkt;

	public Bazaar() {
		this(URI.create(ENDPUNKT));
	}

	public Bazaar(URI endpunkt) {
		this.endpunkt = endpunkt;
		this.klient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	@Override
	public String bezeichnung() {
		return "Bazaar";
	}

	@Override
	public CompletableFuture<Map<String, Preis>> abrufen() {
		HttpRequest anfrage = HttpRequest.newBuilder(endpunkt)
				.timeout(Duration.ofSeconds(20))
				.header("Accept", "application/json")
				.header("User-Agent", "fusion_420 (Fabric client mod)")
				.GET()
				.build();

		return klient.sendAsync(anfrage, HttpResponse.BodyHandlers.ofString())
				.thenApply(antwort -> {
					if (antwort.statusCode() != 200) {
						throw new CompletionException(
								new IllegalStateException("Bazaar antwortete mit HTTP " + antwort.statusCode()));
					}
					return auswerten(antwort.body());
				});
	}

	/** Wandelt die Antwort in Preise je Shard um. Sichtbar fuer die Tests. */
	static Map<String, Preis> auswerten(String json) {
		JsonObject wurzel = JsonParser.parseString(json).getAsJsonObject();
		if (wurzel.has("success") && !wurzel.get("success").getAsBoolean()) {
			throw new IllegalStateException("Bazaar meldet Misserfolg: "
					+ (wurzel.has("cause") ? wurzel.get("cause").getAsString() : "ohne Angabe"));
		}
		if (!wurzel.has("products") || !wurzel.get("products").isJsonObject()) {
			throw new IllegalStateException("Bazaar-Antwort ohne Feld \"products\"");
		}

		Map<String, Preis> preise = new HashMap<>();
		for (Map.Entry<String, JsonElement> eintrag : wurzel.getAsJsonObject("products").entrySet()) {
			if (!eintrag.getKey().startsWith(SHARD_PRAEFIX) || !eintrag.getValue().isJsonObject()) {
				continue;
			}
			JsonObject produkt = eintrag.getValue().getAsJsonObject();
			if (!produkt.has("quick_status") || !produkt.get("quick_status").isJsonObject()) {
				continue;
			}
			JsonObject lage = produkt.getAsJsonObject("quick_status");
			preise.put(eintrag.getKey(), new Preis(
					zahl(lage, "buyPrice"),
					zahl(lage, "sellPrice"),
					(long) zahl(lage, "buyMovingWeek"),
					(long) zahl(lage, "sellMovingWeek")));
		}
		return Map.copyOf(preise);
	}

	private static double zahl(JsonObject o, String schluessel) {
		if (!o.has(schluessel) || !o.get(schluessel).isJsonPrimitive()) {
			return 0.0;
		}
		try {
			return o.get(schluessel).getAsDouble();
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}
}
