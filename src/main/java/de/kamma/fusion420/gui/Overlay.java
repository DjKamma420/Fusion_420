package de.kamma.fusion420.gui;

import de.kamma.fusion420.Kontext;
import de.kamma.fusion420.Zahlen;
import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.einstellungen.Einstellungen;
import de.kamma.fusion420.gui.Fusionserkennung.Fund;
import de.kamma.fusion420.markt.Preisbuch;
import de.kamma.fusion420.mixin.ContainerScreenZugriff;
import de.kamma.fusion420.rechner.Fusion;
import de.kamma.fusion420.rechner.Gewinnrechner;
import de.kamma.fusion420.rechner.Modus;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Zeichnet die besten Fusionen rechts neben das offene Fusions-GUI.
 *
 * <p>Gezeichnet wird im Ereignis {@code ScreenEvents.afterExtract} statt in
 * einem eigenen Eingriff in den Renderweg. Minecraft 26.1 baut die Oberflaeche
 * in einem Auslesedurchgang zusammen; {@code afterExtract} ist dessen Ende und
 * liegt damit ueber allem — Hintergrund, Gegenstaenden, Text und Tooltips.
 * Beliebig viele Mods koennen sich an dasselbe Ereignis haengen, deshalb
 * vertraegt sich das mit SkyHanni, NEU und aehnlichen Oberflaechen-Mods.
 */
public final class Overlay {

	private static final int ZEILE = 10;
	private static final int RAND = 5;

	private static final int FARBE_GRUND = 0xD8101018;
	private static final int FARBE_RAHMEN = 0xFF3C4A6E;
	private static final int FARBE_TITEL = 0xFFF0BA4A;
	private static final int FARBE_TEXT = 0xFFFFFFFF;
	private static final int FARBE_LEISE = 0xFF9AA4BC;
	private static final int FARBE_WARNUNG = 0xFFFFAA33;
	private static final int FARBE_KOSTEN = 0xFFFFAA55;
	private static final int FARBE_GEWINN = 0xFF55FF88;
	private static final int FARBE_VERLUST = 0xFFFF6666;

	/** Neu abtasten hoechstens viermal je Sekunde; jedes Bild waere Verschwendung. */
	private static final long ABTASTABSTAND_MS = 250L;

	private Overlay() {
	}

	public static void verdrahten(Kontext kontext) {
		ScreenEvents.AFTER_INIT.register((klient, bildschirm, breite, hoehe) -> {
			if (!(bildschirm instanceof AbstractContainerScreen<?> behaelter)) {
				return;
			}
			Einstellungen e = kontext.einstellungen();
			if (!e.titelPasst(bildschirm.getTitle().getString())) {
				return;
			}
			if (e.nurAufHypixel && !Fusionserkennung.aufHypixel(klient)) {
				return;
			}

			Zustand zustand = new Zustand(Modus.ausText(e.preisModus));
			ScreenEvents.afterExtract(bildschirm).register(
					(s, grafik, mausX, mausY, tickFortschritt) -> zeichnen(behaelter, grafik, kontext, zustand));
			ScreenKeyboardEvents.afterKeyPress(bildschirm).register(
					(s, ereignis) -> tasteGedrueckt(ereignis, kontext, zustand));
		});
	}

	private static void tasteGedrueckt(KeyEvent ereignis, Kontext kontext, Zustand zustand) {
		Einstellungen e = kontext.einstellungen();
		int taste = ereignis.key();
		if (taste == e.tasteModusWechsel) {
			zustand.modus = zustand.modus.naechster();
			e.preisModus = zustand.modus.name();
			kontext.einstellungenSichern().run();
			zustand.veralten();
		} else if (taste == e.tasteAktualisieren) {
			kontext.preisbuch().erzwingeAktualisierung();
			zustand.veralten();
		}
	}

	private static void zeichnen(AbstractContainerScreen<?> behaelter, GuiGraphicsExtractor grafik,
			Kontext kontext, Zustand zustand) {
		Font schrift = Minecraft.getInstance().font;
		if (schrift == null) {
			return;
		}
		Einstellungen e = kontext.einstellungen();
		kontext.preisbuch().aktualisiereWennAelterAls(e.aktualisierungSekunden * 1000L);

		neuRechnenWennNoetig(behaelter, kontext, zustand);

		List<Zeile> zeilen = zeilenBauen(kontext, zustand);
		if (zeilen.isEmpty()) {
			return;
		}

		ContainerScreenZugriff masse = (ContainerScreenZugriff) (Object) behaelter;
		int x = masse.fusion420$linkerRand() + masse.fusion420$breite() + e.overlayVersatzX;
		int y = masse.fusion420$obererRand() + e.overlayVersatzY;
		int breite = e.overlayBreite;
		int hoehe = zeilen.size() * ZEILE + 2 * RAND;

		grafik.fill(x, y, x + breite, y + hoehe, FARBE_GRUND);
		rahmen(grafik, x, y, breite, hoehe);

		int textX = x + RAND;
		int textY = y + RAND;
		int textBreite = breite - 2 * RAND;
		for (Zeile zeile : zeilen) {
			grafik.text(schrift, kuerzen(schrift, zeile.text(), textBreite), textX, textY, zeile.farbe());
			textY += ZEILE;
		}
	}

	private static void neuRechnenWennNoetig(AbstractContainerScreen<?> behaelter, Kontext kontext, Zustand zustand) {
		long jetzt = System.currentTimeMillis();
		Rezeptbuch buch = kontext.rezeptbuch();
		Einstellungen e = kontext.einstellungen();

		if (jetzt - zustand.letzteAbtastung >= ABTASTABSTAND_MS) {
			zustand.letzteAbtastung = jetzt;
			zustand.fund = Fusionserkennung.shards(behaelter, buch, e.nurContainerSlots);
		}

		int inhalt = zustand.fund.hashCode();
		long preisstand = kontext.preisbuch().standMillis();
		if (inhalt == zustand.inhaltPruefsumme && preisstand == zustand.preisstand) {
			return;
		}
		zustand.inhaltPruefsumme = inhalt;
		zustand.preisstand = preisstand;
		zustand.ergebnis = Gewinnrechner.beste(zustand.fund.erkannt(), buch, kontext.preisbuch().preise(),
				zustand.modus, e.bazaarSteuerProzent, e.mindestVolumenWoche, e.anzahlEintraege);
	}

	private static List<Zeile> zeilenBauen(Kontext kontext, Zustand zustand) {
		Preisbuch preisbuch = kontext.preisbuch();
		Rezeptbuch buch = kontext.rezeptbuch();
		List<Zeile> zeilen = new ArrayList<>();
		zeilen.add(new Zeile("Fusion 420  -  " + zustand.modus.anzeige(), FARBE_TITEL));

		String neuere = kontext.neuereFassung();
		if (neuere != null) {
			zeilen.add(new Zeile("! Version " + neuere + " verfuegbar", FARBE_WARNUNG));
		}

		if (buch == null) {
			zeilen.add(new Zeile("Rezepte werden geladen ...", FARBE_LEISE));
			return zeilen;
		}
		if (preisbuch.standMillis() == 0L) {
			zeilen.add(new Zeile(preisbuch.letzterFehler() == null
					? "Bazaar wird abgefragt ..."
					: "Bazaar: " + preisbuch.letzterFehler(), FARBE_LEISE));
			return zeilen;
		}

		// Ein Hypixel-Update bringt neue Shards, die die Rezeptdatei noch nicht
		// kennt. Ohne Hinweis wirkte das Overlay dann einfach unvollstaendig.
		if (zustand.fund.unbekannt() > 0) {
			zeilen.add(new Zeile("! " + zustand.fund.unbekannt()
					+ " unbekannte Shards - Rezepte veraltet?", FARBE_WARNUNG));
		}

		if (zustand.fund.erkannt().isEmpty()) {
			zeilen.add(new Zeile("Keine Shards erkannt", FARBE_LEISE));
		} else if (zustand.ergebnis.isEmpty()) {
			zeilen.add(new Zeile(zustand.fund.erkannt().size() + " Shards, keine lohnende Fusion", FARBE_LEISE));
		} else {
			int platz = 1;
			for (Fusion fusion : zustand.ergebnis) {
				Shard a = fusion.rezept().eingabeA();
				Shard b = fusion.rezept().eingabeB();
				zeilen.add(new Zeile(platz + ". " + a.fusionsMenge() + "x " + a.name()
						+ " + " + b.fusionsMenge() + "x " + b.name(), FARBE_TEXT));
				zeilen.add(new Zeile("   -> " + fusion.rezept().ausgabeMenge() + "x "
						+ fusion.rezept().ausgabe().name(), FARBE_LEISE));
				zeilen.add(new Zeile("   " + Zahlen.kurz(fusion.kosten()) + " -> "
						+ Zahlen.kurz(fusion.erloes()), FARBE_KOSTEN));
				zeilen.add(new Zeile("   " + Zahlen.mitVorzeichen(fusion.gewinn())
						+ "  (" + Zahlen.prozent(fusion.rendite()) + ")",
						fusion.gewinn() >= 0 ? FARBE_GEWINN : FARBE_VERLUST));
				platz++;
			}
		}

		long alter = (System.currentTimeMillis() - preisbuch.standMillis()) / 1000L;
		zeilen.add(new Zeile("Preise " + alter + "s alt  -  Rezepte: " + buch.herkunft(), FARBE_LEISE));
		zeilen.add(new Zeile("M Modus wechseln  -  R neu laden", FARBE_LEISE));
		return zeilen;
	}

	private static void rahmen(GuiGraphicsExtractor grafik, int x, int y, int breite, int hoehe) {
		grafik.fill(x, y, x + breite, y + 1, FARBE_RAHMEN);
		grafik.fill(x, y + hoehe - 1, x + breite, y + hoehe, FARBE_RAHMEN);
		grafik.fill(x, y, x + 1, y + hoehe, FARBE_RAHMEN);
		grafik.fill(x + breite - 1, y, x + breite, y + hoehe, FARBE_RAHMEN);
	}

	/** Schneidet zu lange Zeilen ab, damit nichts aus dem Kasten laeuft. */
	private static String kuerzen(Font schrift, String text, int hoechstbreite) {
		if (schrift.width(text) <= hoechstbreite) {
			return text;
		}
		String gekuerzt = text;
		while (!gekuerzt.isEmpty() && schrift.width(gekuerzt + "...") > hoechstbreite) {
			gekuerzt = gekuerzt.substring(0, gekuerzt.length() - 1);
		}
		return gekuerzt + "...";
	}

	private record Zeile(String text, int farbe) {
	}

	/** Zwischenstand je geoeffnetem Bildschirm. Nur der Renderfaden fasst ihn an. */
	private static final class Zustand {
		private Modus modus;
		private long letzteAbtastung;
		private int inhaltPruefsumme = Integer.MIN_VALUE;
		private long preisstand = -1L;
		private Fund fund = Fund.LEER;
		private List<Fusion> ergebnis = List.of();

		private Zustand(Modus modus) {
			this.modus = modus;
		}

		private void veralten() {
			inhaltPruefsumme = Integer.MIN_VALUE;
			preisstand = -1L;
			letzteAbtastung = 0L;
		}
	}
}
