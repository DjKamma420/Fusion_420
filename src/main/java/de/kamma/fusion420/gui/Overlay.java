package de.kamma.fusion420.gui;

import de.kamma.fusion420.Kontext;
import de.kamma.fusion420.Zahlen;
import de.kamma.fusion420.daten.Rezeptbuch;
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

/** Ergebnis-Overlay fuer Fusionen, Marktfilter, Hover-Markierung und Drag-and-Drop. */
public final class Overlay {
	private static final int ROW = 10, PAD = 5, BUTTON_H = 13;
	private static final int BG = 0xD8101018, BORDER = 0xFF3C4A6E, TITLE = 0xFFF0BA4A;
	private static final int TEXT = 0xFFFFFFFF, MUTED = 0xFF9AA4BC, WARN = 0xFFFFAA33;
	private static final int PROFIT = 0xFF55FF88, LOSS = 0xFFFF6666;
	private static final int BUTTON = 0xFF222A3A, BUTTON_HOVER = 0xFF34415A, MARK = 0x6655FF88;
	private static final long SCAN_MS = 250L;

	private Overlay() { }

	public static void verdrahten(Kontext k) {
		ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
			if (!(screen instanceof AbstractContainerScreen<?> container)) return;
			Einstellungen e = k.einstellungen();
			if (!e.titelPasst(screen.getTitle().getString())) return;
			if (e.nurAufHypixel && !Fusionserkennung.aufHypixel(mc)) return;
			Zustand z = new Zustand(Einkaufsmodus.ausText(e.einkaufsModus), Verkaufsmodus.ausText(e.verkaufsModus));
			ScreenEvents.afterBackground(screen).register((s, g, mx, my, tick) -> markieren(container, g, k, z, mx, my));
			ScreenEvents.beforeExtract(screen).register((s, g, mx, my, tick) -> zeichnen(container, g, k, z, mx, my));
			ScreenKeyboardEvents.afterKeyPress(screen).register((s, key) -> taste(key, k, z));
			ScreenMouseEvents.beforeMouseClick(screen).register((s, event) -> klick(container, k, z, event));
			ScreenMouseEvents.afterMouseDrag(screen).register((s, event, dx, dy, handled) -> { drag(container, k, z, event); return handled; });
			ScreenMouseEvents.afterMouseRelease(screen).register((s, event, handled) -> { loslassen(k, z, event); return handled; });
		});
	}

	private static void taste(KeyEvent key, Kontext k, Zustand z) {
		if (key.key() == k.einstellungen().tasteAktualisieren) {
			k.preisbuch().erzwingeAktualisierung();
			z.veraltet();
		}
	}

	private static void klick(AbstractContainerScreen<?> container, Kontext k, Zustand z, MouseButtonEvent e) {
		if (e.button() != GLFW.GLFW_MOUSE_BUTTON_1 || !in(z, e.x(), e.y())) return;
		double x = e.x(), y = e.y();
		if (y < z.y + 15) {
			z.ziehen = true;
			z.grabX = x - z.x;
			z.grabY = y - z.y;
			return;
		}
		if (inRect(x, y, z.buyX, z.controlY, z.controlW, BUTTON_H)) { z.dropdown = z.dropdown == 1 ? -1 : 1; return; }
		if (inRect(x, y, z.sellX, z.controlY, z.controlW, BUTTON_H)) { z.dropdown = z.dropdown == 2 ? -1 : 2; return; }
		if (z.dropdown == 1) {
			int i = option(y, z.controlY + BUTTON_H, Einkaufsmodus.values().length);
			if (inRect(x, y, z.buyX, z.controlY + BUTTON_H, z.controlW, Einkaufsmodus.values().length * ROW) && i >= 0) {
				z.einkauf = Einkaufsmodus.values()[i]; speichern(k, z); return;
			}
		}
		if (z.dropdown == 2) {
			int i = option(y, z.controlY + BUTTON_H, Verkaufsmodus.values().length);
			if (inRect(x, y, z.sellX, z.controlY + BUTTON_H, z.controlW, Verkaufsmodus.values().length * ROW) && i >= 0) {
				z.verkauf = Verkaufsmodus.values()[i]; speichern(k, z); return;
			}
		}
		z.dropdown = -1;
	}

	private static void drag(AbstractContainerScreen<?> container, Kontext k, Zustand z, MouseButtonEvent e) {
		if (!z.ziehen || e.button() != GLFW.GLFW_MOUSE_BUTTON_1) return;
		ContainerScreenZugriff c = (ContainerScreenZugriff) (Object) container;
		Einstellungen s = k.einstellungen();
		int baseX = c.fusion420$linkerRand() + c.fusion420$breite();
		int baseY = c.fusion420$obererRand();
		s.overlayVersatzX = Math.clamp((int) Math.round(e.x() - z.grabX - baseX), -1000, 1000);
		s.overlayVersatzY = Math.clamp((int) Math.round(e.y() - z.grabY - baseY), -1000, 1000);
	}

	private static void loslassen(Kontext k, Zustand z, MouseButtonEvent e) {
		if (e.button() != GLFW.GLFW_MOUSE_BUTTON_1) return;
		if (z.ziehen) k.einstellungenSichern().run();
		z.ziehen = false;
	}

	private static void speichern(Kontext k, Zustand z) {
		Einstellungen e = k.einstellungen();
		e.einkaufsModus = z.einkauf.name();
		e.verkaufsModus = z.verkauf.name();
		z.dropdown = -1;
		z.veraltet();
		k.einstellungenSichern().run();
	}

	private static void zeichnen(AbstractContainerScreen<?> container, GuiGraphicsExtractor g, Kontext k, Zustand z, int mx, int my) {
		Font font = Minecraft.getInstance().font;
		if (font == null) return;
		Einstellungen e = k.einstellungen();
		k.preisbuch().aktualisiereWennAelterAls(e.aktualisierungSekunden * 1000L);
		neuBerechnen(container, k, z);
		List<Zeile> lines = zeilen(k, z);
		ContainerScreenZugriff c = (ContainerScreenZugriff) (Object) container;
		int x = c.fusion420$linkerRand() + c.fusion420$breite() + e.overlayVersatzX;
		int y = c.fusion420$obererRand() + e.overlayVersatzY;
		int menuH = z.dropdown == 1 ? Einkaufsmodus.values().length * ROW : z.dropdown == 2 ? Verkaufsmodus.values().length * ROW : 0;
		int contentRows = lines.stream().mapToInt(l -> l.fusionIndex >= 0 ? 4 : 1).sum();
		int width = e.overlayBreite;
		int height = 45 + menuH + contentRows * ROW + 2 * PAD;
		z.x = x; z.y = y; z.width = width; z.height = height;
		z.controlY = y + 27; z.controlW = (width - 3 * PAD) / 2;
		z.buyX = x + PAD; z.sellX = z.buyX + z.controlW + PAD;
		z.rects.clear();

		g.fill(x, y, x + width, y + height, BG);
		drawBorder(g, x, y, width, height);
		g.text(font, "Fusion 420", x + PAD, y + PAD, TITLE);
		g.text(font, "Einkauf", z.buyX, y + 15, MUTED);
		g.text(font, "Verkauf", z.sellX, y + 15, MUTED);
		drawButton(g, font, z.buyX, z.controlY, z.controlW, z.einkauf.anzeige(), mx, my);
		drawButton(g, font, z.sellX, z.controlY, z.controlW, z.verkauf.anzeige(), mx, my);

		int lineY = y + 45 + (menuH > 0 ? menuH + 2 : 0);
		for (Zeile line : lines) {
			if (line.fusionIndex < 0) {
				g.text(font, shorten(font, line.text.get(0), width - 2 * PAD), x + PAD, lineY, line.color);
				lineY += ROW;
			} else {
				z.rects.add(new FusionRect(line.fusionIndex, lineY, ROW * 4));
				for (int i = 0; i < line.text.size(); i++) g.text(font, shorten(font, line.text.get(i), width - 2 * PAD), x + PAD, lineY + i * ROW, line.color);
				lineY += ROW * 4;
			}
		}
		if (z.dropdown == 1) drawMenu(g, font, z.buyX, z.controlY + BUTTON_H, z.controlW, Einkaufsmodus.values(), z.einkauf.ordinal(), mx, my);
		if (z.dropdown == 2) drawMenu(g, font, z.sellX, z.controlY + BUTTON_H, z.controlW, Verkaufsmodus.values(), z.verkauf.ordinal(), mx, my);
	}

	private static void markieren(AbstractContainerScreen<?> container, GuiGraphicsExtractor g, Kontext k, Zustand z, int mx, int my) {
		int index = -1;
		for (FusionRect r : z.rects) if (mx >= z.x && mx < z.x + z.width && my >= r.y && my < r.y + r.height) { index = r.index; break; }
		if (index < 0 || index >= z.results.size()) return;
		Fusion f = z.results.get(index);
		Map<String, List<Slot>> slots = Fusionserkennung.shardSlots(container, k.rezeptbuch(), k.einstellungen().nurContainerSlots);
		ContainerScreenZugriff c = (ContainerScreenZugriff) (Object) container;
		markSlots(g, c, slots.get(f.rezept().eingabeA().schluessel()));
		markSlots(g, c, slots.get(f.rezept().eingabeB().schluessel()));
	}

	private static void markSlots(GuiGraphicsExtractor g, ContainerScreenZugriff c, List<Slot> slots) {
		if (slots == null) return;
		for (Slot slot : slots) {
			int x = c.fusion420$linkerRand() + slot.x, y = c.fusion420$obererRand() + slot.y;
			g.fill(x, y, x + 16, y + 16, MARK);
		}
	}

	private static void neuBerechnen(AbstractContainerScreen<?> container, Kontext k, Zustand z) {
		long now = System.currentTimeMillis();
		Einstellungen e = k.einstellungen();
		Rezeptbuch book = k.rezeptbuch();
		if (now - z.lastScan >= SCAN_MS) { z.lastScan = now; z.fund = Fusionserkennung.shards(container, book, e.nurContainerSlots); }
		int contentHash = z.fund.hashCode();
		long priceStamp = k.preisbuch().standMillis();
		if (contentHash == z.contentHash && priceStamp == z.priceStamp) return;
		z.contentHash = contentHash; z.priceStamp = priceStamp;
		z.results = Gewinnrechner.beste(z.fund.erkannt(), book, k.preisbuch().preise(), z.einkauf, z.verkauf,
				e.bazaarSteuerProzent, e.mindestVolumenWoche, e.anzahlEintraege);
	}

	private static List<Zeile> zeilen(Kontext k, Zustand z) {
		Preisbuch p = k.preisbuch(); Rezeptbuch b = k.rezeptbuch(); List<Zeile> out = new ArrayList<>();
		if (b == null) { out.add(Zeile.status("Rezepte werden geladen ...")); return out; }
		if (p.standMillis() == 0L) { out.add(Zeile.status(p.letzterFehler() == null ? "Bazaar wird abgefragt ..." : "Bazaar: " + p.letzterFehler())); return out; }
		if (z.fund.unbekannt() > 0) out.add(Zeile.warn("! " + z.fund.unbekannt() + " unbekannte Shards - Rezepte veraltet?"));
		if (z.fund.erkannt().isEmpty()) out.add(Zeile.status("Keine Shards erkannt"));
		else if (z.results.isEmpty()) out.add(Zeile.status(z.fund.erkannt().size() + " Shards, keine lohnende Fusion"));
		else {
			int n = 1;
			for (Fusion f : z.results) {
				out.add(Zeile.fusion(n, List.of(
					n + ". " + f.rezept().eingabeA().fusionsMenge() + "x " + f.rezept().eingabeA().name() + " + " + f.rezept().eingabeB().fusionsMenge() + "x " + f.rezept().eingabeB().name(),
					"   -> " + f.rezept().ausgabeMenge() + "x " + f.rezept().ausgabe().name(),
					"   " + Zahlen.kurz(f.kosten()) + " -> " + Zahlen.kurz(f.erloes()),
					"   " + Zahlen.mitVorzeichen(f.gewinn()) + "  (" + (Double.isInfinite(f.rendite()) ? "∞" : Zahlen.prozent(f.rendite())) + ")"
				), f.gewinn() >= 0 ? PROFIT : LOSS));
				n++;
			}
		}
		long age = (System.currentTimeMillis() - p.standMillis()) / 1000L;
		out.add(Zeile.status("Preise " + age + "s alt - Rezepte: " + b.herkunft()));
		out.add(Zeile.status("Titel ziehen zum Verschieben"));
		return out;
	}

	private static int option(double y, int start, int count) { int i = (int) ((y - start) / ROW); return i >= 0 && i < count ? i : -1; }
	private static boolean in(Zustand z, double x, double y) { return inRect(x, y, z.x, z.y, z.width, z.height); }
	private static boolean inRect(double x, double y, int rx, int ry, int rw, int rh) { return x >= rx && x < rx + rw && y >= ry && y < ry + rh; }
	private static void drawButton(GuiGraphicsExtractor g, Font f, int x, int y, int w, String text, int mx, int my) {
		g.fill(x, y, x + w, y + BUTTON_H, inRect(mx, my, x, y, w, BUTTON_H) ? BUTTON_HOVER : BUTTON);
		g.text(f, text + " v", x + 4, y + 2, TEXT);
	}
	private static void drawMenu(GuiGraphicsExtractor g, Font f, int x, int y, int w, Object[] values, int selected, int mx, int my) {
		for (int i = 0; i < values.length; i++) {
			String text = values[i] instanceof Einkaufsmodus e ? e.anzeige() : ((Verkaufsmodus) values[i]).anzeige();
			int yy = y + i * ROW;
			g.fill(x, yy, x + w, yy + ROW, inRect(mx, my, x, yy, w, ROW) ? BUTTON_HOVER : BUTTON);
			g.text(f, text, x + 4, yy + 1, i == selected ? TITLE : TEXT);
		}
	}
	private static void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		g.fill(x, y, x + w, y + 1, BORDER); g.fill(x, y + h - 1, x + w, y + h, BORDER);
		g.fill(x, y, x + 1, y + h, BORDER); g.fill(x + w - 1, y, x + w, y + h, BORDER);
	}
	private static String shorten(Font f, String text, int max) {
		if (f.width(text) <= max) return text;
		String s = text;
		while (!s.isEmpty() && f.width(s + "...") > max) s = s.substring(0, s.length() - 1);
		return s + "...";
	}

	private record Zeile(List<String> text, int color, int fusionIndex) {
		static Zeile status(String s) { return new Zeile(List.of(s), MUTED, -1); }
		static Zeile warn(String s) { return new Zeile(List.of(s), WARN, -1); }
		static Zeile fusion(int i, List<String> s, int color) { return new Zeile(s, color, i); }
	}
	private record FusionRect(int index, int y, int height) { }
	private static final class Zustand {
		Einkaufsmodus einkauf; Verkaufsmodus verkauf; long lastScan; int contentHash = Integer.MIN_VALUE; long priceStamp = -1;
		Fund fund = Fund.LEER; List<Fusion> results = List.of(); final List<FusionRect> rects = new ArrayList<>();
		int x, y, width, height, controlY, controlW, buyX, sellX, dropdown = -1; boolean ziehen; double grabX, grabY;
		Zustand(Einkaufsmodus e, Verkaufsmodus v) { einkauf = e; verkauf = v; }
		void veraltet() { contentHash = Integer.MIN_VALUE; priceStamp = -1; lastScan = 0; }
	}
}
