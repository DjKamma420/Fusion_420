package de.kamma.fusion420.rechner;

import de.kamma.fusion420.daten.Rezept;
import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.markt.Preis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Rechnet aus, welche Fusionen der vorhandenen Shards sich lohnen. */
public final class Gewinnrechner {

	private Gewinnrechner() {
	}

	/**
	 * @param vorhanden die im GUI erkannten Shards, ohne Doppelte
	 * @param einkauf Preisbasis fuer die Eingaben; GEFARMT setzt deren Kosten auf 0
	 * @param verkauf Preisbasis fuer die Ausgabe
	 */
	public static List<Fusion> beste(
			List<Shard> vorhanden,
			Rezeptbuch buch,
			Map<String, Preis> preise,
			Einkaufsmodus einkauf,
			Verkaufsmodus verkauf,
			double steuerProzent,
			long mindestVolumen,
			int anzahl) {

		if (vorhanden == null || vorhanden.isEmpty() || buch == null || preise == null || preise.isEmpty()) {
			return List.of();
		}

		double nachSteuer = 1.0 - Math.clamp(steuerProzent, 0.0, 100.0) / 100.0;
		List<Fusion> gefunden = new ArrayList<>();

		for (int i = 0; i < vorhanden.size(); i++) {
			Shard a = vorhanden.get(i);
			Preis preisA = preise.get(a.bazaarId());
			if (preisA == null || (!preisA.handelbar() && einkauf != Einkaufsmodus.GEFARMT)) continue;

			for (int j = i; j < vorhanden.size(); j++) {
				Shard b = vorhanden.get(j);
				Preis preisB = preise.get(b.bazaarId());
				if (preisB == null || (!preisB.handelbar() && einkauf != Einkaufsmodus.GEFARMT)) continue;

				double kosten = a.fusionsMenge() * einkauf.preis(preisA)
						+ b.fusionsMenge() * einkauf.preis(preisB);
				for (Rezept rezept : buch.fusionen(a.schluessel(), b.schluessel())) {
					Preis preisAus = preise.get(rezept.ausgabe().bazaarId());
					if (preisAus == null || !preisAus.handelbar()) continue;
					long volumen = preisAus.engpassVolumenWoche();
					if (volumen < mindestVolumen) continue;

					double erloes = rezept.ausgabeMenge() * verkauf.preis(preisAus) * nachSteuer;
					double gewinn = erloes - kosten;
					double rendite = kosten > 0.0 ? gewinn / kosten : Double.POSITIVE_INFINITY;
					gefunden.add(new Fusion(rezept, kosten, erloes, gewinn, rendite, volumen));
				}
			}
		}

		// Bei GEFARMT ist Gewinn == Verkaufserloes. Damit werden wirklich die
		// besten Verkaufsziele gezeigt, nicht eine sinnlose Prozent-Rendite.
		gefunden.sort(Comparator.comparingDouble(Fusion::gewinn).reversed());
		return List.copyOf(gefunden.subList(0, Math.min(anzahl, gefunden.size())));
	}

	/** Alte API fuer vorhandene Tests/Integrationen. */
	public static List<Fusion> beste(List<Shard> vorhanden, Rezeptbuch buch, Map<String, Preis> preise,
			Modus modus, double steuerProzent, long mindestVolumen, int anzahl) {
		return beste(vorhanden, buch, preise,
				switch (modus) {
					case SOFORT, GEMISCHT -> Einkaufsmodus.SOfort_PLACEHOLDER;
					case ORDER -> Einkaufsmodus.ORDER;
				},
				switch (modus) {
					case SOFORT -> Verkaufsmodus.SOFORT;
					case ORDER, GEMISCHT -> Verkaufsmodus.ORDER;
				}, steuerProzent, mindestVolumen, anzahl);
	}
}
