package de.kamma.fusion420.gui;

import de.kamma.fusion420.Kontext;
import de.kamma.fusion420.Zahlen;
import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import de.kamma.fusion420.einstellungen.Einstellungen;
import de.kamma.fusion420.gui.Fusionserkennung.Fund;
import de.kamma.fusion420.markt.Preisbuch;
import de.kamma.fusion420.mixin.ContainerScreenZugriff;
import de.kamma.fusion420.rechner.Einkaufsmodus;
import de.kamma.fusion420.rechner.Fusion;
import de.kamma.fusion420.rechner.Gewinnrechner;
import de.kamma.fusion420.rechner.Verkaufsmodus;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Fusion-Overlay mit getrennten Marktfiltern, Drag-and-Drop und Slot-Hover. */
public final class Overlay {
	private static final int ZEILE = 10;
	private static final int RAND = 5;
	private static final int STEUER_HOEHE = 15;
	private static final int BUTTON_HOEHE = 13;
	private static final int FARBE_GRUND = 0xD8101018;
	private static final int FARBE_RAHMEN = 0xFF3C4A6E;
	private static final int FARBE_TITEL = 0xFFF0BA4A;
	private static final int FARBE_TEXT = 0xFFFFFFFF;
	private static final int FARBE_LEISE = 0xFF9AA4BC;
	private static final int FARBE_WARNUNG = 0xFFFFAA33;
	private static final int FARBE_KOSTEN = 0xFFFFAA55;
	private static final int FARBE_GEWINN = 0xFF55FF88;
	private static final int FARBE_VERLUST = 0xFFFF6666;
	private static final int FARBE_BUTTON = 0xFF222A3A;
	private static final int FARBE_BUTTON_HOVER = 0xFF34415A;
	private static final int FARBE_MARKE = 0x6655FF88;
	private static final long ABTASTABSTAND_MS = 250L;

	private Overlay() { }

	public static void verdrahten(Kontext kontext) {
		ScreenEvents.AFTER_INIT.register((klient, bildschirm, breite, hoehe) -> {
			if (!(bildschirm instanceof AbstractContainerScreen<?> behaelter)) return;
			Einstellungen e = kontext.einstellungen();
			if (!e.titelPasst(bildschirm.getTitle().getString())) return;
			if (e.nurAufHypixel && !Fusionserkennung.aufHypixel(klient)) return;

			Zustand zustand = new Zustand(Einkaufsmodus.ausText(e.einkaufsModus), Verkaufsmodus.ausText(e.verkaufsModus));
			ScreenEvents.beforeExtract(bildschirm).register(
					(s, grafik, mausX, mausY, tick) -> zeichnen(behaelter, grafik, kontext, zustand, mausX, mausY));
			ScreenEvents.afterBackground(bildschirm).register(
					(s, grafik, mausX, mausY, tick) -> markieren(behaelter, grafik, kontext, zustand, mausX, mausY));
			ScreenKeyboardEvents.afterKeyPress(bildschirm).register(
					(s, ereignis) -> tasteGedrueckt(ereignis, kontext, zustand));
			ScreenMouseEvents.beforeMouseClick(bildschirm).register(
					(s, ereignis) -> mausKlick(behaelter, kontext, zustand, ereignis));
			ScreenMouseEvents.afterMouseDrag(bildschirm).register(
					(s, ereignis, dx, dy) -> mausDrag(behaelter, kontext, zustand, ereignis));
			ScreenMouseEvents.afterMouseRelease(bildschirm).register(
					(s, ereignis, handled) -> mausLoslassen(kontext, zustand, ereignis));
		});
	}

	private static void tasteGedrueckt(KeyEvent ereignis, Kontext kontext, Zustand zustand) {
		Einstellungen e = kontext.einstellungen();
		if (ereignis.key() == e.tasteAktualisieren) {
			kontext.preisbuch().erzwingeAktualisierung();
			zustand.veralten();
		}
	}

	private static void mausKlick(AbstractContainerScreen<?> behaelter, Kontext kontext, Zustand zustand, MouseButtonEvent ereignis) {
		if (ereignis.button() != GLFW.GLFW_MOUSE_BUTTON_1 || !imOverlay(zustand, ereignis.x(), ereignis.y())) return;
		double mausX = ereignis.x();
		double mausY = ereignis.y();
		if (mausY >= zustand.y && mausY < zustand.y + STEUER_HOEHE) {
			zustand.ziehen = true;
			zustand.ziehStartX = mausX;
			zustand.ziehStartY = mausY;
			return;
		}
		if (inRechteck(mausX, mausY, zustand.einkaufX, zustand.controlY, zustand.controlBreite, BUTTON_HOEHE)) {
			zustand.dropdown = zustand.dropdown == 1 ? -1 : 1;
			return;
		}
		if (inRechteck(mausX, mausY, zustand.verkaufX, zustand.controlY, zustand.controlBreite, BUTTON_HOEHE)) {
			zustand.dropdown = zustand.dropdown == 2 ? -1 : 2;
			return;
		}
		if (zustand.dropdown == 1) {
			int auswahl = menueAuswahl(mausY, zustand.controlY + BUTTON_HOEHE, Einkaufsmodus.values().length);
			if (inRechteck(mausX, mausY, zustand.einkaufX, zustand.controlY + BUTTON_HOEHE,
					zustand.controlBreite, Einkaufsmodus.values().length * ZEILE) && auswahl >= 0) {
				zustand.einkauf = Einkaufsmodus.values()[auswahl];
				speichern(kontext, zustand);
				return;
			}
		}
		if (zustand.dropdown == 2) {
			int auswahl = menueAuswahl(mausY, zustand.controlY + BUTTON_HOEHE, Verkaufsmodus.values().length);
			if (inRechteck(mausX, mausY, zustand.verkaufX, zustand.controlY + BUTTON_HOEHE,
					zustand.controlBreite, Verkaufsmodus.values().length * ZEILE) && auswahl >= 0) {
				zustand.verkauf = Verkaufsmodus.values()[auswahl];
				speichern(kontext, zustand);
				return;
			}
		}
		zustand.dropdown = -1;
	}

	private static void mausDrag(AbstractContainerScreen<?> behaelter, Kontext kontext, Zustand zustand, MouseButtonEvent ereignis) {
		if (!zustand.ziehen || ereignis.button() != GLFW.GLFW_MOUSE_BUTTON_1) return;
		ContainerScreenZugriff masse = (ContainerScreenZugriff) (Object) behaelter;
		Einstellungen e = kontext.einstellungen();
		int containerX = masse.fusion420$linkerRand() + masse.fusion420$breite();
		int containerY = masse.fusion420$obererRand();
		e.overlayVersatzX = Math.clamp((int) Math.round(ereignis.x() - containerX - (zustand.ziehStartX - zustand.x)), -1000, 1000);
		e.overlayVersatzY = Math.clamp((int) Math.round(ereignis.y() - containerY - (zustand.ziehStartY - zustand.y)), -1000, 1000);
	}

	private static void mausLoslassen(Kontext kontext, Zustand zustand, MouseButtonEvent ereignis) {
		if (ereignis.button() != GLFW.GLFW_MOUSE_BUTTON_1) return;
		if (zustand.ziehen) kontext.einstellungenSichern().run();
		zustand.ziehen = false;
	}

	private static void speichern(Kontext kontext, Zustand zustand) {
		Einstellungen e = kontext.einstellungen();
		e.einkaufsModus = zustand.einkauf.name();
		e.verkaufsModus = zustand.verkauf.name();
		zustand.dropdown = -1;
		zustand.veralten();
		kontext.einstellungenSichern().run();
	}

	private static void zeichnen(AbstractContainerScreen<?> behaelter, GuiGraphicsExtractor grafik, Kontext kontext,
			Zustand zustand, int mausX, int mausY) {
		Font schrift = Minecraft.getInstance().font;
		if (schrift == null) return;
		Einstellungen e = kontext.einstellungen();
		kontext.preisbuch().aktualisiereWennAelterAls(e.aktualisierungSekunden * 1000L);
		neuRechnenWennNoetig(behaelter, kontext, zustand);
		List<Zeile> zeilen = zeilenBauen(kontext, zustand);
		ContainerScreenZugriff masse = (ContainerScreenZugriff) (Object) behaelter;
		int x = masse.fusion420$linkerRand() + masse.fusion420$breite() + e.overlayVersatzX;
		int y = masse.fusion420$obererRand() + e.overlayVersatzY;
		int breite = e.overlayBreite;
		int inhaltZeilen = zeilen.stream().mapToInt(z -> z.fusionIndex() >= 0 ? 4 : 1).sum();
		int menuHoehe = zustand.dropdown == 1 ? Einkaufsmodus.values().length * ZEILE
				: zustand.dropdown == 2 ? Verkaufsmodus.values().length * ZEILE : 0;
		int hoehe = 45 + menuHoehe + inhaltZeilen * ZEILE + 2 * RAND;

		zustand.x = x;
		zustand.y = y;
		zustand.breite = breite;
		zustand.hoehe = hoehe;
		zustand.controlY = y + 27;
		zustand.controlBreite = (breite - 3 * RAND) / 2;
		zustand.einkaufX = x + RAND;
		zustand.verkaufX = zustand.einkaufX + zustand.controlBreite + RAND;
		zustand.fusionRects.clear();

		grafik.fill(x, y, x + breite, y + hoehe, FARBE_GRUND);
		rahmen(grafik, x, y, breite, hoehe);
		int textX = x + RAND;
		grafik.text(schrift, "Fusion 420", textX, y + RAND, FARBE_TITEL);
		grafik.text(schrift, "Einkauf", zustand.einkaufX, y + STEUER_HOEHE, FARBE_LEISE);
		grafik.text(schrift, "Verkauf", zustand.verkaufX, y + STEUER_HOEHE, FARBE_LEISE);
		button(grafik, schrift, zustand.einkaufX, zustand.controlY, zustand.controlBreite, zustand.einkauf.anzeige(), mausX, mausY);
		button(grafik, schrift, zustand.verkaufX, zustand.controlY, zustand.controlBreite, zustand.verkauf.anzeige(), mausX, mausY);

		int resultY = y + 45 + (menuHoehe > 0 ? menuHoehe + 2 : 0);
		int laufY = resultY;
		for (Zeile zeile : zeilen) {
			if (zeile.fusionIndex() < 0) {
				grafik.text(schrift, kuerzen(schrift, zeile.text().get(0), breite - 2 * RAND), textX, laufY, zeile.farbe());
				laufY += ZEILE;
			} else {
				zustand.fusionRects.add(new FusionRechteck(zeile.fusionIndex(), laufY, ZEILE * 4));
				for (int i = 0; i < zeile.text().size(); i++) {
					grafik.text(schrift, kuerzen(schrift, zeile.text().get(i), breite - 2 * RAND), textX, laufY + i * ZEILE, zeile.farbe());
				}
				laufY += ZEILE * 4;
			}
		}

		if (zustand.dropdown == 1) {
			menue(grafik, schrift, zustand.einkaufX, zustand.controlY + BUTTON_HOEHE, zustand.controlBreite,
					Einkaufsmodus.values(), zustand.einkauf.ordinal(), mausX, mausY);
		} else if (zustand.dropdown == 2) {
			menue(grafik, schrift, zustand.verkaufX, zustand.controlY + BUTTON_HOEHE, zustand.controlBreite,
					Verkaufsmodus.values(), zustand.verkauf.ordinal(), mausX, mausY);
		}
	}

	private static void markieren(AbstractContainerScreen<?> behaelter, GuiGraphicsExtractor grafik, Kontext kontext,
			Zustand zustand, int mausX, int mausY) {
		int hovered = -1;
		for (FusionRechteck rect : zustand.fusionRects) {
			if (mausX >= zustand.x && mausX < zustand.x + zustand.breite && mausY >= rect.y() && mausY < rect.y() + rect.hoehe()) {
				hovered = rect.index();
				break;
			}
		}
		if (hovered < 0 || hovered >= zustand.ergebnis.size()) return;
		Fusion fusion = zustand.ergebnis.get(hovered);
		Map<String, List<Slot>> slots = Fusionserkennung.shardSlots(behaelter, kontext.rezeptbuch(), kontext.einstellungen().nurContainerSlots);
		ContainerScreenZugriff masse = (ContainerScreenZugriff) (Object) behaelter;
		markiereShard(grafik, masse, slots.get(fusion.rezept().eingabeA().schluessel()));
		markiereShard(grafik, masse, slots.get(fusion.rezept().eingabeB().schluessel()));
	}

	private static void markiereShard(GuiGraphicsExtractor grafik, ContainerScreenZugriff masse, List<Slot> slots) {
		if (slots == null) return;
		for (Slot slot : slots) {
			int x = masse.fusion420$linkerRand() + slot.x;
			int y = masse.fusion420$obererRand() + slot.y;
			grafik.fill(x, y, x + 16, y + 16, FARBE_MARKE);
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
		if (inhalt == zustand.inhaltPruefsumme && preisstand == zustand.preisstand) return;
		zustand.inhaltPruefsumme = inhalt;
		zustand.preisstand = preisstand;
		zustand.ergebnis = Gewinnrechner.beste(zustand.fund.erkannt(), buch, kontext.preisbuch().preise(),
				zustand.einkauf, zustand.verkauf, e.bazaarSteuerProzent, e.mindestVolumenWoche, e.anzahlEintraege);
	}

	private static List<Zeile> zeilenBauen(Kontext kontext, Zustand zustand) {
		Preisbuch preisbuch = kontext.preisbuch();
		Rezeptbuch buch = kontext.rezeptbuch();
		List<Zeile> zeilen = new ArrayList<>();
		if (buch == null) {
			zeilen.add(Zeile.status("Rezepte werden geladen ..."));
			return zeilen;
		}
		if (preisbuch.standMillis() == 0L) {
			zeilen.add(Zeile.status(preisbuch.letzterFehler() == null ? "Bazaar wird abgefragt ..." : "Bazaar: " + preisbuch.letzterFehler()));
			return zeilen;
		}
		if (zustand.fund.unbekannt() > 0) zeilen.add(Zeile.statusWarn("! " + zustand.fund.unbekannt() + " unbekannte Shards - Rezepte veraltet?"));
		if (zustand.fund.erkannt().isEmpty()) {
			zeilen.add(Zeile.status("Keine Shards erkannt"));
		} else if (zustand.ergebnis.isEmpty()) {
			zeilen.add(Zeile.status(zustand.fund.erkannt().size() + " Shards, keine lohnende Fusion"));
		} else {
			int platz = 1;
			for (Fusion f : zustand.ergebnis) {
				zeilen.add(Zeile.fusion(platz, List.of(
					platz + ". " + f.rezept().eingabeA().fusionsMenge() + "x " + f.rezept().eingabeA().name() + " + " + f.rezept().eingabeB().fusionsMenge() + "x " + f.rezept().eingabeB().name(),
					"   -> " + f.rezept().ausgabeMenge() + "x " + f.rezept().ausgabe().name(),
					"   " + Zahlen.kurz(f.kosten()) + " -> " + Zahlen.kurz(f.erloes()),
					"   " + Zahlen.mitVorzeichen(f.gewinn()) + "  (" + (Double.isInfinite(f.rendite()) ? "∞" : Zahlen.prozent(f.rendite())) + ")"
				), f.gewinn() >= 0 ? FARBE_GEWINN : FARBE_VERLUST));
				platz++;
			}
		}
		long alter = (System.currentTimeMillis() - preisbuch.standMillis()) / 1000L;
		zeilen.add(Zeile.status("Preise " + alter + "s alt  -  Rezepte: " + buch.herkunft()));
		zeilen.add(Zeile.status("Titel ziehen zum Verschieben"));
		return zeilen;
	}

	private static int menueAuswahl(double mouseY, int menuY, int count) {
		int i = (int) ((mouseY - menuY) / ZEILE);
		return i >= 0 && i < count ? i : -1;
	}
	private static boolean imOverlay(Zustand z, double x, double y) { return x >= z.x && x < z.x + z.breite && y >= z.y && y < z.y + z.hoehe; }
	private static boolean inRechteck(double x, double y, int rx, int ry, int rw, int rh) { return x >= rx && x < rx + rw && y >= ry && y < ry + rh; }

	private static void button(GuiGraphicsExtractor grafik, Font schrift, int x, int y, int breite, String text, int mausX, int mausY) {
		grafik.fill(x, y, x + breite, y + BUTTON_HOEHE, inRechteck(mausX, mausY, x, y, breite, BUTTON_HOEHE) ? FARBE_BUTTON_HOVER : FARBE_BUTTON);
		grafik.text(schrift, text + " v", x + 4, y + 2, FARBE_TEXT);
	}
	private static void menue(GuiGraphicsExtractor grafik, Font schrift, int x, int y, int breite, Object[] werte, int aktuell, int mausX, int mausY) {
		for (int i = 0; i < werte.length; i++) {
			String text = werte[i] instanceof Einkaufsmodus e ? e.anzeige() : ((Verkaufsmodus) werte[i]).anzeige();
			int yy = y + i * ZEILE;
			grafik.fill(x, yy, x + breite, yy + ZEILE, inRechteck(mausX, mausY, x, yy, breite, ZEILE) ? FARBE_BUTTON_HOVER : FARBE_BUTTON);
			grafik.text(schrift, text, x + 4, yy + 1, i == aktuell ? FARBE_TITEL : FARBE_TEXT);
		}
	}
	private static void rahmen(GuiGraphicsExtractor grafik, int x, int y, int breite, int hoehe) {
		grafik.fill(x, y, x + breite, y + 1, FARBE_RAHMEN);
		grafik.fill(x, y + hoehe - 1, x + breite, y + hoehe, FARBE_RAHMEN);
		grafik.fill(x, y, x + 1, y + hoehe, FARBE_RAHMEN);
		grafik.fill(x + breite - 1, y, x + breite, y + hoehe, FARBE_RAHMEN);
	}
	private static String kuerzen(Font schrift, String text, int hoechstbreite) {
		if (schrift.width(text) <= hoechstbreite) return text;
		String gekuerzt = text;
		while (!gekuerzt.isEmpty() && schrift.width(gekuerzt + "...") > hoechstbreite) gekuerzt = gekuerzt.substring(0, gekuerzt.length() - 1);
		return gekuerzt + "...";
	}

	private record Zeile(List<String> text, int farbe, int fusionIndex) {
		static Zeile status(String text) { return new Zeile(List.of(text), FARBE_LEISE, -1); }
		static Zeile statusWarn(String text) { return new Zeile(List.of(text), FARBE_WARNUNG, -1); }
		static Zeile fusion(int index, List<String> text, int farbe) { return new Zeile(text, farbe, index); }
	}
	private record FusionRechteck(int index, int y, int hoehe) { }

	private static final class Zustand {
		private Einkaufsmodus einkauf;
		private Verkaufsmodus verkauf;
		private long letzteAbtastung;
		private int inhaltPruefsumme = Integer.MIN_VALUE;
		private long preisstand = -1L;
		private Fund fund = Fund.LEER;
		private List<Fusion> ergebnis = List.of();
		private final List<FusionRechteck> fusionRects = new ArrayList<>();
		private int x, y, breite, hoehe, controlY, controlBreite, einkaufX, verkaufX;
		private int dropdown = -1;
		private boolean ziehen;
		private double ziehStartX, ziehStartY;
		private Zustand(Einkaufsmodus einkauf, Verkaufsmodus verkauf) { this.einkauf = einkauf; this.verkauf = verkauf; }
		private void veralten() { inhaltPruefsumme = Integer.MIN_VALUE; preisstand = -1L; letzteAbtastung = 0L; }
	}
}
