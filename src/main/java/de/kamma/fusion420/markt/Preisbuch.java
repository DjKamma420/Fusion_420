package de.kamma.fusion420.markt;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Haelt den zuletzt abgerufenen Preisstand und stoesst Auffrischungen an.
 *
 * <p>Alle Methoden duerfen aus dem Renderfaden gerufen werden: sie warten
 * nie. Ein laufender Abruf verhindert einen zweiten, sonst wuerde jedes
 * gezeichnete Bild eine neue Anfrage ausloesen.
 */
public final class Preisbuch {

	private final Preisquelle quelle;
	private final AtomicBoolean laeuft = new AtomicBoolean(false);

	private volatile Map<String, Preis> preise = Map.of();
	private volatile long standMillis = 0L;
	private volatile String letzterFehler;

	public Preisbuch(Preisquelle quelle) {
		this.quelle = quelle;
	}

	public Map<String, Preis> preise() {
		return preise;
	}

	/** Zeitpunkt des letzten erfolgreichen Abrufs, {@code 0} wenn noch keiner. */
	public long standMillis() {
		return standMillis;
	}

	public boolean laedt() {
		return laeuft.get();
	}

	public String letzterFehler() {
		return letzterFehler;
	}

	public void aktualisiereWennAelterAls(long hoechstalterMillis) {
		if (System.currentTimeMillis() - standMillis >= hoechstalterMillis) {
			anstossen();
		}
	}

	public void erzwingeAktualisierung() {
		standMillis = 0L;
		anstossen();
	}

	private void anstossen() {
		if (!laeuft.compareAndSet(false, true)) {
			return;
		}
		quelle.abrufen().whenComplete((neu, fehler) -> {
			try {
				if (fehler != null) {
					letzterFehler = kurzfassung(fehler);
				} else {
					preise = neu;
					standMillis = System.currentTimeMillis();
					letzterFehler = null;
				}
			} finally {
				laeuft.set(false);
			}
		});
	}

	private static String kurzfassung(Throwable fehler) {
		Throwable ursache = fehler.getCause() != null ? fehler.getCause() : fehler;
		String text = ursache.getMessage();
		return text == null || text.isBlank() ? ursache.getClass().getSimpleName() : text;
	}
}
