package de.kamma.fusion420.rechner;

import de.kamma.fusion420.daten.Rezept;
import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.markt.Preis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Rechnet aus, welche Fusionen der vorhandenen Shards sich lohnen.
 *
 * <p>Reine Funktion ohne Minecraft und ohne Netz — genau deshalb laesst sich
 * die Rechnung in der CI pruefen, waehrend das Spiel selbst nirgends laeuft.
 */
public final class Gewinnrechner {

	private Gewinnrechner() {
	}

	/**
	 * @param vorhanden      die im GUI erkannten Shards, ohne Doppelte
	 * @param steuerProzent  Bazaar-Verkaufssteuer, wird vom Erloes abgezogen
	 * @param mindestVolumen Ausgaben mit weniger Wochenumsatz fallen raus
	 * @param anzahl         wie viele Ergebnisse zurueckkommen
	 * @return die besten Fusionen, nach Gewinn absteigend
	 */
	public static List<Fusion> beste(
			List<Shard> vorhanden,
			Rezeptbuch buch,
			Map<String, Preis> preise,
			Modus modus,
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
			if (preisA == null || !preisA.handelbar()) {
				continue;
			}

			// j beginnt bei i: ein Shard laesst sich auch mit sich selbst fusionieren.
			for (int j = i; j < vorhanden.size(); j++) {
				Shard b = vorhanden.get(j);
				Preis preisB = preise.get(b.bazaarId());
				if (preisB == null || !preisB.handelbar()) {
					continue;
				}

				double kosten = a.fusionsMenge() * modus.einkauf(preisA)
						+ b.fusionsMenge() * modus.einkauf(preisB);
				if (kosten <= 0.0) {
					continue;
				}

				for (Rezept rezept : buch.fusionen(a.schluessel(), b.schluessel())) {
					Preis preisAus = preise.get(rezept.ausgabe().bazaarId());
					if (preisAus == null || !preisAus.handelbar()) {
						continue;
					}
					long volumen = preisAus.engpassVolumenWoche();
					if (volumen < mindestVolumen) {
						continue;
					}

					double erloes = rezept.ausgabeMenge() * modus.verkauf(preisAus) * nachSteuer;
					double gewinn = erloes - kosten;
					gefunden.add(new Fusion(rezept, kosten, erloes, gewinn, gewinn / kosten, volumen));
				}
			}
		}

		gefunden.sort(Comparator.comparingDouble(Fusion::gewinn).reversed());
		return List.copyOf(gefunden.subList(0, Math.min(anzahl, gefunden.size())));
	}
}
