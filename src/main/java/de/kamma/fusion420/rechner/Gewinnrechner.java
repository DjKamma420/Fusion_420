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
	private Gewinnrechner() { }

	public static List<Fusion> beste(List<Shard> vorhanden, Rezeptbuch buch, Map<String, Preis> preise,
			Einkaufsmodus einkauf, Verkaufsmodus verkauf, double steuerProzent,
			long mindestVolumen, int anzahl) {
		if (vorhanden == null || vorhanden.isEmpty() || buch == null || preise == null || preise.isEmpty()) return List.of();
		double nachSteuer = 1.0 - Math.clamp(steuerProzent, 0.0, 100.0) / 100.0;
		List<Fusion> gefunden = new ArrayList<>();
		for (int i = 0; i < vorhanden.size(); i++) {
			Shard a = vorhanden.get(i);
			Preis preisA = preise.get(a.bazaarId());
			if (einkauf != Einkaufsmodus.GEFARMT && (preisA == null || !preisA.handelbar())) continue;
			for (int j = i; j < vorhanden.size(); j++) {
				Shard b = vorhanden.get(j);
				Preis preisB = preise.get(b.bazaarId());
				if (einkauf != Einkaufsmodus.GEFARMT && (preisB == null || !preisB.handelbar())) continue;

				for (Rezept rezept : buch.fusionen(a.schluessel(), b.schluessel())) {
					Preis preisAus = preise.get(rezept.ausgabe().bazaarId());
					if (preisAus == null || !preisAus.handelbar()) continue;
					long volumen = preisAus.engpassVolumenWoche();
					if (volumen < mindestVolumen) continue;

					// Die Menge steht am konkreten Rezept. Das ist fuer Sonderregeln wie
					// Chameleon wichtig: dort ist die Eingabemenge nicht zwingend die
					// normale fuse_amount des Shards.
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
		return List.copyOf(gefunden.subList(0, Math.min(anzahl, gefunden.size())));
	}

	/** Kompatibilitaet fuer vorhandene Aufrufer. */
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
