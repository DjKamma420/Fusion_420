package de.kamma.fusion420.rechner;

import de.kamma.fusion420.daten.Rezept;
import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.markt.Preis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Calculates profitable fusion combinations. */
public final class Gewinnrechner {
	private Gewinnrechner() { }

	public static List<Fusion> beste(List<Shard> vorhanden, Rezeptbuch buch, Map<String, Preis> preise,
			Einkaufsmodus einkauf, Verkaufsmodus verkauf, double steuerProzent,
			long mindestVolumen, int anzahl) {
		return beste(vorhanden, buch, preise, einkauf, verkauf, steuerProzent, mindestVolumen, anzahl, null);
	}

	/**
	 * Calculates the best fusions. If focusShardKey is set, every returned recipe
	 * must use that shard as one of its two inputs. This is used when the player
	 * selects a single shard in the fusion GUI.
	 */
	public static List<Fusion> beste(List<Shard> vorhanden, Rezeptbuch buch, Map<String, Preis> preise,
			Einkaufsmodus einkauf, Verkaufsmodus verkauf, double steuerProzent,
			long mindestVolumen, int anzahl, String focusShardKey) {
		if (vorhanden == null || vorhanden.isEmpty() || buch == null || preise == null || preise.isEmpty()) return List.of();
		double nachSteuer = 1.0 - Math.clamp(steuerProzent, 0.0, 100.0) / 100.0;
		List<Fusion> gefunden = new ArrayList<>();
		for (int i = 0; i < vorhanden.size(); i++) {
			Shard a = vorhanden.get(i);
			if (focusShardKey != null && !focusShardKey.equals(a.schluessel())) continue;
			Preis preisA = preise.get(a.bazaarId());
			if (einkauf != Einkaufsmodus.GEFARMT && (preisA == null || !preisA.handelbar())) continue;
			for (int j = 0; j < vorhanden.size(); j++) {
				if (focusShardKey == null && j < i) continue;
				Shard b = vorhanden.get(j);
				if (focusShardKey != null && a.schluessel().equals(b.schluessel()) && vorhanden.size() > 1) continue;
				Preis preisB = preise.get(b.bazaarId());
				if (einkauf != Einkaufsmodus.GEFARMT && (preisB == null || !preisB.handelbar())) continue;

				for (Rezept rezept : buch.fusionen(a.schluessel(), b.schluessel())) {
					Preis preisAus = preise.get(rezept.ausgabe().bazaarId());
					if (preisAus == null || !preisAus.handelbar()) continue;
					long volumen = preisAus.engpassVolumenWoche();
					if (volumen < mindestVolumen) continue;

					double kosten = einkauf == Einkaufsmodus.GEFARMT ? 0.0
							: rezept.eingabeA().fusionsMenge() * einkauf.preis(preisA)
							+ rezept.eingabeB().fusionsMenge() * einkauf.preis(preisB);
					double erloes = rezept.ausgabeMenge() * verkauf.preis(preisAus) * nachSteuer;
					double gewinn = erloes - kosten;
					double rendite = kosten > 0.0 ? gewinn / kosten : Double.POSITIVE_INFINITY;
					gefunden.add(new Fusion(rezept, kosten, erloes, gewinn, rendite, volumen));
				}
			}
		}
		gefunden.sort(Comparator.comparingDouble(Fusion::gewinn).reversed());
		if (anzahl <= 0 || anzahl >= gefunden.size()) return List.copyOf(gefunden);
		return List.copyOf(gefunden.subList(0, anzahl));
	}

	/** Compatibility overload for existing callers. */
	public static List<Fusion> beste(List<Shard> vorhanden, Rezeptbuch buch, Map<String, Preis> preise,
			Modus modus, double steuerProzent, long mindestVolumen, int anzahl) {
		Einkaufsmodus einkauf = switch (modus) {
			case SOFORT, GEMISCHT -> Einkaufsmodus.SOFORT;
			case ORDER -> Einkaufsmodus.ORDER;
		};
		Verkaufsmodus verkauf = switch (modus) {
			case SOFORT -> Verkaufsmodus.SOFORT;
			case ORDER, GEMISCHT -> Verkaufsmodus.ORDER;
		};
		return beste(vorhanden, buch, preise, einkauf, verkauf, steuerProzent, mindestVolumen, anzahl);
	}
}
