package de.kamma.fusion420.gui;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Shard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Erkennt das Fusions-GUI und liest heraus, welche Shards darin liegen. */
public final class Fusionserkennung {

	/** Spielerinventar plus Schnellleiste am Ende jedes Kistenmenues. */
	private static final int EIGENE_SLOTS = 36;

	private Fusionserkennung() {
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
	 * Die Shards im offenen Menue, ohne Doppelte.
	 *
	 * @param nurKiste wenn wahr, bleibt das eigene Inventar aussen vor
	 */
	public static List<Shard> shards(AbstractContainerScreen<?> bildschirm, Rezeptbuch buch, boolean nurKiste) {
		if (buch == null || bildschirm == null || bildschirm.getMenu() == null) {
			return List.of();
		}
		List<Slot> slots = bildschirm.getMenu().slots;
		int grenze = nurKiste ? Math.max(0, slots.size() - EIGENE_SLOTS) : slots.size();

		Map<String, Shard> gefunden = new LinkedHashMap<>();
		for (int i = 0; i < grenze; i++) {
			ItemStack stapel = slots.get(i).getItem();
			if (stapel.isEmpty()) {
				continue;
			}
			Shard shard = buch.nachAnzeigename(stapel.getHoverName().getString());
			if (shard != null) {
				gefunden.putIfAbsent(shard.schluessel(), shard);
			}
		}
		return List.copyOf(gefunden.values());
	}
}
