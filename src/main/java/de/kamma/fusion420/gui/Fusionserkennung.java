package de.kamma.fusion420.gui;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Erkennt das Fusions-GUI und liest heraus, welche Shards darin liegen. */
public final class Fusionserkennung {

	/** Spielerinventar plus Schnellleiste am Ende jedes Kistenmenues. */
	private static final int EIGENE_SLOTS = 36;

	private Fusionserkennung() {
	}

	/**
	 * Was im GUI gefunden wurde.
	 *
	 * @param erkannt   aufgeloeste Shards, ohne Doppelte
	 * @param unbekannt Anzahl verschiedener Gegenstaende, die wie ein Shard
	 *                  heissen, aber in den Rezeptdaten fehlen. Genau so
	 *                  macht sich ein Hypixel-Update bemerkbar, bevor die
	 *                  Rezeptdatei nachgezogen ist.
	 */
	public record Fund(List<Shard> erkannt, int unbekannt) {

		public static final Fund LEER = new Fund(List.of(), 0);
	}

	public static boolean aufHypixel(Minecraft mc) {
		if (mc == null) {
			return false;
		}
		ServerData server = mc.getCurrentServer();
		return server != null
				&& server.ip != null
				&& server.ip.toLowerCase(Locale.ROOT).contains("hypixel.net");
	}

	/**
	 * Die Shards im offenen Menue.
	 *
	 * @param nurKiste wenn wahr, bleibt das eigene Inventar aussen vor
	 */
	public static Fund shards(AbstractContainerScreen<?> bildschirm, Rezeptbuch buch, boolean nurKiste) {
		if (buch == null || bildschirm == null || bildschirm.getMenu() == null) {
			return Fund.LEER;
		}
		List<Slot> slots = bildschirm.getMenu().slots;
		int grenze = nurKiste ? Math.max(0, slots.size() - EIGENE_SLOTS) : slots.size();

		Map<String, Shard> gefunden = new LinkedHashMap<>();
		Set<String> unbekannt = new HashSet<>();

		for (int i = 0; i < grenze; i++) {
			ItemStack stapel = slots.get(i).getItem();
			if (stapel.isEmpty()) {
				continue;
			}
			String name = stapel.getHoverName().getString();
			Shard shard = buch.nachAnzeigename(name);
			if (shard != null) {
				gefunden.putIfAbsent(shard.schluessel(), shard);
			} else if (heisstWieEinShard(name)) {
				unbekannt.add(Rezeptbuch.normalisiere(name));
			}
		}
		return new Fund(List.copyOf(gefunden.values()), unbekannt.size());
	}

	/** Endet der Anzeigename auf "Shard", ohne dass die Daten ihn kennen? */
	static boolean heisstWieEinShard(String roh) {
		if (roh == null) {
			return false;
		}
		String klar = roh.replaceAll("§.", "").trim().toLowerCase(Locale.ROOT);
		return klar.endsWith("shard") && klar.length() > "shard".length();
	}
}
