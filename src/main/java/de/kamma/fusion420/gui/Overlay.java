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

/** Clean, interactive profit overlay for fusion combinations. */
public final class Overlay {
	private static final int PAD = 6;
	private static final int ROW = 10;
	private static final int RESULT_H = 40;
	private static final int BUTTON_H = 14;
	private static final int HEADER_H = 48;
	private static final int FOOTER_H = 20;
	private static final int VIEW_H = 190;
	private static final int TOTAL_H = HEADER_H + VIEW_H + FOOTER_H;
	private static final long SCAN_MS = 250L;

	// Brighter, high-contrast palette. Alpha is intentionally high so text stays readable over Minecraft GUIs.
	private static final int BG = 0xEE202838;
	private static final int PANEL = 0xF51A2030;
	private static final int BORDER = 0xFF7F9BC7;
	private static final int ACCENT = 0xFFFFC857;
	private static final int TEXT = 0xFFF7F9FF;
	private static final int MUTED = 0xFFB9C5D9;
	private static final int WARN = 0xFFFFB454;
	private static final int PROFIT = 0xFF63F59A;
	private static final int LOSS = 0xFFFF6B78;
	private static final int BUTTON = 0xFF303A4F;
	private static final int BUTTON_HOVER = 0xFF455672;
	private static final int BUTTON_SELECTED = 0xFF596D91;
	private static final int RESULT_HOVER = 0x334E9CFF;
	private static final int MARK_FILL = 0xD8FFD43B;
	private static final int MARK_EDGE = 0xFFFFFFFF;
	private static final int SELECTED = 0xFF59D8FF;

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
			ScreenMouseEvents.afterMouseScroll(screen).register((s, mx, my, hx, vy, consumed) -> scroll(z, mx, my, vy, consumed));
		});
	}

	private static void taste(KeyEvent key, Kontext k, Zustand z) {
		if (key.key() == k.einstellungen().tasteAktualisieren) {
			k.preisbuch().erzwingeAktualisierung();
			z.veraltet();
		}
	}

	private static boolean scroll(Zustand z, double mx, double my, double amount, boolean consumed) {
		if (!inRect(mx, my, z.x, z.resultTop, z.width, VIEW_H) || z.results.size() <= visibleResults()) return consumed;
		int max = Math.max(0, z.results.size() - visibleResults());
		z.scroll = Math.clamp(z.scroll - (int) Math.signum(amount), 0, max);
		return true;
	}

	private static void klick(AbstractContainerScreen<?> container, Kontext k, Zustand z, MouseButtonEvent e) {
		if (e.button() != GLFW.GLFW_MOUSE_BUTTON_1) return;
		double x = e.x(), y = e.y();

		// Clicking a shard focuses the calculator on that shard.
		if (!in(z, x, y)) {
			String clicked = shardAt(container, k.rezeptbuch(), k.einstellungen().nurContainerSlots, x, y);
			if (clicked != null) {
				z.selectedShardKey = clicked.equals(z.selectedShardKey) ? null : clicked;
				z.scroll = 0;
				z.veraltet();
			}
			return;
		}

		if (y < z.y + 17) {
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

	private static String shardAt(AbstractContainerScreen<?> container, Rezeptbuch book, boolean nurKiste, double mouseX, double mouseY) {
		if (book == null || container == null || container.getMenu() == null) return null;
		ContainerScreenZugriff c = (ContainerScreenZugriff) (Object) container;
		int grenze = nurKiste ? Math.max(0, container.getMenu().slots.size() - 36) : container.getMenu().slots.size();
		for (int i = 0; i < grenze; i++) {
			Slot slot = container.getMenu().slots.get(i);
			int sx = c.fusion420$linkerRand() + slot.x;
			int sy = c.fusion420$obererRand() + slot.y;
			if (mouseX < sx || mouseX >= sx + 16 || mouseY < sy || mouseY >= sy + 16) continue;
			if (slot.getItem().isEmpty()) return null;
			Shard shard = book.nachAnzeigename(slot.getItem().getHoverName().getString());
			return shard == null ? null : shard.schluessel();
		}
		return null;
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

		ContainerScreenZugriff c = (ContainerScreenZugriff) (Object) container;
		int x = c.fusion420$linkerRand() + c.fusion420$breite() + e.overlayVersatzX;
		int y = c.fusion420$obererRand() + e.overlayVersatzY;
		int width = Math.max(260, e.overlayBreite);
		int height = TOTAL_H;
		z.x = x; z.y = y; z.width = width; z.height = height;
		z.controlY = y + 29;
		z.controlW = (width - 3 * PAD) / 2;
		z.buyX = x + PAD;
		z.sellX = z.buyX + z.controlW + PAD;
		z.resultTop = y + HEADER_H;
		z.resultBottom = z.resultTop + VIEW_H;
		z.rects.clear();

		g.fill(x, y, x + width, y + height, BG);
		g.fill(x + 1, y + 1, x + width - 1, y + HEADER_H, PANEL);
		g.fill(x + 1, z.resultTop, x + width - 1, z.resultBottom, 0xCC111722);
		drawBorder(g, x, y, width, height);

		g.text(font, "Fusion 420", x + PAD, y + 6, ACCENT);
		String focus = selectedName(z, k.rezeptbuch());
		if (focus == null) g.text(font, "All detected shards", x + 72, y + 6, MUTED);
		else g.text(font, shorten(font, "Focus: " + focus, width - 82), x + 72, y + 6, SELECTED);

		g.text(font, "Buy", z.buyX, y + 18, MUTED);
		g.text(font, "Sell", z.sellX, y + 18, MUTED);
		drawButton(g, font, z.buyX, z.controlY, z.controlW, z.einkauf.anzeige(), mx, my);
		drawButton(g, font, z.sellX, z.controlY, z.controlW, z.verkauf.anzeige(), mx, my);

		if (z.results.isEmpty()) {
			String status = statusText(k, z);
			g.text(font, shorten(font, status, width - 2 * PAD), x + PAD, z.resultTop + 8, z.fund.unbekannt() > 0 ? WARN : MUTED);
		} else {
			g.enableScissor(x + 1, z.resultTop, x + width - 1, z.resultBottom);
			int first = z.scroll;
			int count = Math.min(visibleResults(), z.results.size() - first);
			for (int n = 0; n < count; n++) {
				int index = first + n;
				int ry = z.resultTop + n * RESULT_H;
				Fusion f = z.results.get(index);
				boolean hover = mx >= x && mx < x + width && my >= ry && my < ry + RESULT_H;
				if (hover) g.fill(x + 2, ry + 1, x + width - 2, ry + RESULT_H - 1, RESULT_HOVER);
				z.rects.add(new FusionRect(index, ry, RESULT_H));
				drawFusion(g, font, f, index + 1, x + PAD, ry, width - 2 * PAD, hover);
			}
			g.disableScissor();
		}

		if (z.results.size() > visibleResults()) drawScrollbar(g, x + width - 6, z.resultTop + 3, z.resultBottom - 3, z.scroll, z.results.size() - visibleResults());
		String footer = footerText(k, z);
		g.text(font, shorten(font, footer, width - 2 * PAD), x + PAD, z.resultBottom + 5, MUTED);
		if (z.results.size() > visibleResults()) {
			String scroll = "Scroll: " + (z.scroll + 1) + "-" + Math.min(z.scroll + visibleResults(), z.results.size()) + "/" + z.results.size();
			g.text(font, scroll, x + width - PAD - font.width(scroll), z.resultBottom + 5, MUTED);
		}

		if (z.dropdown == 1) drawMenu(g, font, z.buyX, z.controlY + BUTTON_H, z.controlW, Einkaufsmodus.values(), z.einkauf.ordinal(), mx, my);
		if (z.dropdown == 2) drawMenu(g, font, z.sellX, z.controlY + BUTTON_H, z.controlW, Verkaufsmodus.values(), z.verkauf.ordinal(), mx, my);
	}

	private static void drawFusion(GuiGraphicsExtractor g, Font font, Fusion f, int number, int x, int y, int width, boolean hover) {
		int main = f.gewinn() >= 0 ? PROFIT : LOSS;
		String input = number + ". " + f.rezept().eingabeA().fusionsMenge() + "x " + f.rezept().eingabeA().name()
				+ " + " + f.rezept().eingabeB().fusionsMenge() + "x " + f.rezept().eingabeB().name();
		String output = "-> " + f.rezept().ausgabeMenge() + "x " + f.rezept().ausgabe().name();
		String money = Zahlen.kurz(f.kosten()) + " -> " + Zahlen.kurz(f.erloes());
		String profit = Zahlen.mitVorzeichen(f.gewinn()) + "  (" + (Double.isInfinite(f.rendite()) ? "∞" : Zahlen.prozent(f.rendite())) + ")";
		g.text(font, shorten(font, input, width), x, y + 2, TEXT);
		g.text(font, shorten(font, output, width), x + 9, y + 12, MUTED);
		g.text(font, shorten(font, money, width), x + 9, y + 22, MUTED);
		g.text(font, shorten(font, profit, width), x + 9, y + 32, main);
	}

	private static void drawScrollbar(GuiGraphicsExtractor g, int x, int top, int bottom, int scroll, int maxScroll) {
		g.fill(x, top, x + 3, bottom, 0x664B566C);
		int track = bottom - top;
		int thumb = Math.max(16, track * visibleResults() / (visibleResults() + maxScroll));
		int pos = maxScroll == 0 ? 0 : (track - thumb) * scroll / maxScroll;
		g.fill(x, top + pos, x + 3, top + pos + thumb, 0xFFD4DEEE);
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
			g.fill(x, y, x + 16, y + 16, MARK_FILL);
			g.fill(x, y, x + 16, y + 2, MARK_EDGE);
			g.fill(x, y + 14, x + 16, y + 16, MARK_EDGE);
			g.fill(x, y, x + 2, y + 16, MARK_EDGE);
			g.fill(x + 14, y, x + 16, y + 16, MARK_EDGE);
		}
	}

	private static void neuBerechnen(AbstractContainerScreen<?> container, Kontext k, Zustand z) {
		long now = System.currentTimeMillis();
		Einstellungen e = k.einstellungen();
		Rezeptbuch book = k.rezeptbuch();
		if (now - z.lastScan >= SCAN_MS) {
			z.lastScan = now;
			z.fund = Fusionserkennung.shards(container, book, e.nurContainerSlots);
			if (z.selectedShardKey != null && z.fund.erkannt().stream().noneMatch(s -> z.selectedShardKey.equals(s.schluessel()))) z.selectedShardKey = null;
		}
		int contentHash = 31 * z.fund.hashCode() + (z.selectedShardKey == null ? 0 : z.selectedShardKey.hashCode());
		long priceStamp = k.preisbuch().standMillis();
		if (contentHash == z.contentHash && priceStamp == z.priceStamp) return;
		z.contentHash = contentHash; z.priceStamp = priceStamp;
		z.results = Gewinnrechner.beste(z.fund.erkannt(), book, k.preisbuch().preise(), z.einkauf, z.verkauf,
				e.bazaarSteuerProzent, e.mindestVolumenWoche, 0, z.selectedShardKey);
		z.scroll = Math.clamp(z.scroll, 0, Math.max(0, z.results.size() - visibleResults()));
	}

	private static String selectedName(Zustand z, Rezeptbuch book) {
		if (z.selectedShardKey == null || book == null) return null;
		for (Shard shard : z.fund.erkannt()) if (z.selectedShardKey.equals(shard.schluessel())) return shard.name();
		return null;
	}

	private static String statusText(Kontext k, Zustand z) {
		Preisbuch p = k.preisbuch();
		Rezeptbuch b = k.rezeptbuch();
		if (b == null) return "Loading recipes...";
		if (p.standMillis() == 0L) return p.letzterFehler() == null ? "Fetching Bazaar prices..." : "Bazaar: " + p.letzterFehler();
		if (z.fund.unbekannt() > 0) return "! " + z.fund.unbekannt() + " unknown shards - recipe data may be outdated";
		if (z.selectedShardKey != null) return "No profitable fusion found for the selected shard";
		return "No profitable fusion found";
	}

	private static String footerText(Kontext k, Zustand z) {
		Preisbuch p = k.preisbuch();
		Rezeptbuch b = k.rezeptbuch();
		if (p.standMillis() == 0L) return "Prices unavailable";
		long age = Math.max(0L, (System.currentTimeMillis() - p.standMillis()) / 1000L);
		return "Prices " + age + "s old | Recipes: " + b.herkunft() + (z.selectedShardKey != null ? " | Focused" : "");
	}

	private static int visibleResults() { return VIEW_H / RESULT_H; }
	private static int option(double y, int start, int count) { int i = (int) ((y - start) / ROW); return i >= 0 && i < count ? i : -1; }
	private static boolean in(Zustand z, double x, double y) { return inRect(x, y, z.x, z.y, z.width, z.height); }
	private static boolean inRect(double x, double y, int rx, int ry, int rw, int rh) { return x >= rx && x < rx + rw && y >= ry && y < ry + rh; }

	private static void drawButton(GuiGraphicsExtractor g, Font f, int x, int y, int w, String text, int mx, int my) {
		int bg = inRect(mx, my, x, y, w, BUTTON_H) ? BUTTON_HOVER : BUTTON;
		g.fill(x, y, x + w, y + BUTTON_H, bg);
		drawBorder(g, x, y, w, BUTTON_H, 0xFF7186A8);
		g.text(f, shorten(f, text, w - 14), x + 4, y + 2, TEXT);
		g.text(f, "▾", x + w - 9, y + 2, ACCENT);
	}

	private static void drawMenu(GuiGraphicsExtractor g, Font f, int x, int y, int w, Object[] values, int selected, int mx, int my) {
		for (int i = 0; i < values.length; i++) {
			String text = values[i] instanceof Einkaufsmodus e ? e.anzeige() : ((Verkaufsmodus) values[i]).anzeige();
			int yy = y + i * ROW;
			int bg = i == selected ? BUTTON_SELECTED : (inRect(mx, my, x, yy, w, ROW) ? BUTTON_HOVER : BUTTON);
			g.fill(x, yy, x + w, yy + ROW, bg);
			g.text(f, text, x + 4, yy + 1, i == selected ? ACCENT : TEXT);
		}
	}

	private static void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h) { drawBorder(g, x, y, w, h, BORDER); }
	private static void drawBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	private static String shorten(Font f, String text, int max) {
		if (max <= 0 || f.width(text) <= max) return text;
		String s = text;
		while (!s.isEmpty() && f.width(s + "...") > max) s = s.substring(0, s.length() - 1);
		return s + "...";
	}

	private record FusionRect(int index, int y, int height) { }

	private static final class Zustand {
		Einkaufsmodus einkauf;
		Verkaufsmodus verkauf;
		long lastScan;
		int contentHash = Integer.MIN_VALUE;
		long priceStamp = -1;
		Fund fund = Fund.LEER;
		List<Fusion> results = List.of();
		final List<FusionRect> rects = new ArrayList<>();
		String selectedShardKey;
		int x, y, width, height, controlY, controlW, buyX, sellX, resultTop, resultBottom, scroll, dropdown = -1;
		boolean ziehen;
		double grabX, grabY;

		Zustand(Einkaufsmodus e, Verkaufsmodus v) { einkauf = e; verkauf = v; }
		void veraltet() { contentHash = Integer.MIN_VALUE; priceStamp = -1; lastScan = 0; }
	}
}
