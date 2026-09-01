package de.kamma.fusion420.daten;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alle bekannten Fusionsrezepte, nachschlagbar ueber ein Eingabepaar.
 *
 * <p>Die Quelldatei enthaelt ueber 130 000 Rezeptzeilen. Eine
 * {@code Map<Paar, Liste>} daraus kostet zweistellige Megabyte und viele
 * Millionen Zeiger. Stattdessen liegt jede Zeile als ein einziger
 * {@code long} in einem sortierten Feld:
 *
 * <pre>Bits 40-55 EingabeA | 24-39 EingabeB | 8-23 Ausgabe | 0-7 Menge</pre>
 *
 * Weil die Eingaben in den hohen Bits stehen, gruppiert schon das Sortieren
 * nach Paar; ein Nachschlagen ist eine binaere Suche plus ein kurzer Lauf.
 * Das Feld braucht rund ein Megabyte und keinen einzigen Zeiger.
 */
public final class Rezeptbuch {

	private static final int VERSATZ_A = 40;
	private static final int VERSATZ_B = 24;
	private static final int VERSATZ_AUSGABE = 8;

	private final Map<String, Shard> nachSchluessel;
	private final Map<String, Shard> nachName;
	private final Map<String, Integer> indexNachSchluessel;
	private final String[] schluesselNachIndex;
	private final long[] index;
	private final String herkunft;

	private Rezeptbuch(Map<String, Shard> nachSchluessel, Map<String, Shard> nachName,
			Map<String, Integer> indexNachSchluessel, String[] schluesselNachIndex, long[] index,
			String herkunft) {
		this.nachSchluessel = nachSchluessel;
		this.nachName = nachName;
		this.indexNachSchluessel = indexNachSchluessel;
		this.schluesselNachIndex = schluesselNachIndex;
		this.index = index;
		this.herkunft = herkunft;
	}

	public static Rezeptbuch ausJson(String json) {
		return ausJson(json, "unbekannt");
	}

	/**
	 * @param herkunft woher die Daten stammen; das Overlay zeigt es an, damit
	 *                 sichtbar wird, wenn nur die mitgelieferte — also
	 *                 moeglicherweise veraltete — Fassung greift
	 */
	public static Rezeptbuch ausJson(String json, String herkunft) {
		JsonObject wurzel = JsonParser.parseString(json).getAsJsonObject();
		JsonObject shardsJson = wurzel.getAsJsonObject("shards");
		JsonObject rezepteJson = wurzel.has("recipes") && wurzel.get("recipes").isJsonObject()
				? wurzel.getAsJsonObject("recipes") : new JsonObject();

		Map<String, Shard> nachSchluessel = new HashMap<>();
		Map<String, Shard> nachName = new HashMap<>();
		Map<String, Integer> indexNachSchluessel = new HashMap<>();
		List<String> schluessel = new ArrayList<>();

		for (Map.Entry<String, JsonElement> eintrag : shardsJson.entrySet()) {
			JsonObject s = eintrag.getValue().getAsJsonObject();
			Shard shard = new Shard(
					eintrag.getKey(),
					text(s, "name", eintrag.getKey()),
					text(s, "family", ""),
					text(s, "type", ""),
					text(s, "rarity", ""),
					s.has("fuse_amount") && !s.get("fuse_amount").isJsonNull()
							? s.get("fuse_amount").getAsInt() : 1,
					text(s, "internal_id", ""));
			nachSchluessel.put(shard.schluessel(), shard);
			nachName.putIfAbsent(normalisiere(shard.name()), shard);
			indexNachSchluessel.put(shard.schluessel(), schluessel.size());
			schluessel.add(shard.schluessel());
		}

		long[] roh = new long[1024];
		int anzahl = 0;
		for (Map.Entry<String, JsonElement> proAusgabe : rezepteJson.entrySet()) {
			Integer ausgabe = indexNachSchluessel.get(proAusgabe.getKey());
			if (ausgabe == null || !proAusgabe.getValue().isJsonObject()) {
				continue;
			}
			for (Map.Entry<String, JsonElement> proMenge : proAusgabe.getValue().getAsJsonObject().entrySet()) {
				int menge;
				try {
					menge = Integer.parseInt(proMenge.getKey());
				} catch (NumberFormatException e) {
					continue;
				}
				if (menge < 1 || menge > 255 || !proMenge.getValue().isJsonArray()) {
					continue;
				}
				for (JsonElement paarJson : proMenge.getValue().getAsJsonArray()) {
					if (!paarJson.isJsonArray()) {
						continue;
					}
					JsonArray paar = paarJson.getAsJsonArray();
					if (paar.size() != 2) {
						continue;
					}
					Integer a = indexNachSchluessel.get(paar.get(0).getAsString());
					Integer b = indexNachSchluessel.get(paar.get(1).getAsString());
					if (a == null || b == null) {
						continue;
					}
					if (anzahl == roh.length) {
						roh = Arrays.copyOf(roh, roh.length * 2);
					}
					roh[anzahl++] = kodiere(Math.min(a, b), Math.max(a, b), ausgabe, menge);
				}
			}
		}

		long[] index = Arrays.copyOf(roh, anzahl);
		Arrays.sort(index);
		return new Rezeptbuch(Map.copyOf(nachSchluessel), Map.copyOf(nachName),
				Map.copyOf(indexNachSchluessel), schluessel.toArray(new String[0]), index, herkunft);
	}

	/** Alle Fusionen, die aus genau diesem Paar entstehen koennen. */
	public List<Rezept> fusionen(String schluesselA, String schluesselB) {
		int a = indexVon(schluesselA);
		int b = indexVon(schluesselB);
		if (a < 0 || b < 0) {
			return List.of();
		}
		long unten = paarTeil(Math.min(a, b), Math.max(a, b));
		long oben = unten + (1L << VERSATZ_B);

		Shard eingabeA = nachSchluessel.get(schluesselNachIndex[Math.min(a, b)]);
		Shard eingabeB = nachSchluessel.get(schluesselNachIndex[Math.max(a, b)]);

		List<Rezept> treffer = new ArrayList<>();
		for (int i = untereGrenze(index, unten); i < index.length && index[i] < oben; i++) {
			long zeile = index[i];
			int ausgabe = (int) ((zeile >>> VERSATZ_AUSGABE) & 0xFFFF);
			int menge = (int) (zeile & 0xFF);
			Shard ziel = nachSchluessel.get(schluesselNachIndex[ausgabe]);
			if (ziel != null) {
				treffer.add(new Rezept(eingabeA, eingabeB, ziel, menge));
			}
		}
		return treffer;
	}

	/**
	 * Loest einen Namen aus dem Spiel auf, etwa {@code "§fGrove Shard"}.
	 * Bewusst ueber den Anzeigenamen statt ueber NBT: Namen ueberleben
	 * Minecraft-Versionen, die Datenkomponenten nicht.
	 */
	public Shard nachAnzeigename(String roh) {
		return roh == null ? null : nachName.get(normalisiere(roh));
	}

	public Shard shard(String schluessel) {
		return nachSchluessel.get(schluessel);
	}

	public Collection<Shard> alleShards() {
		return nachSchluessel.values();
	}

	public int shardAnzahl() {
		return nachSchluessel.size();
	}

	public int rezeptAnzahl() {
		return index.length;
	}

	public String herkunft() {
		return herkunft;
	}

	/** Farbcodes weg, alles klein, nur Buchstaben und Ziffern, ohne "shard" am Ende. */
	public static String normalisiere(String roh) {
		StringBuilder sb = new StringBuilder(roh.length());
		for (int i = 0; i < roh.length(); i++) {
			char c = roh.charAt(i);
			if (c == '§') {
				i++;
				continue;
			}
			if (Character.isLetterOrDigit(c)) {
				sb.append(Character.toLowerCase(c));
			}
		}
		String s = sb.toString();
		if (s.length() > 5 && s.endsWith("shard")) {
			s = s.substring(0, s.length() - 5);
		}
		return s;
	}

	private int indexVon(String schluessel) {
		Integer i = indexNachSchluessel.get(schluessel);
		return i == null ? -1 : i;
	}

	private static long paarTeil(int a, int b) {
		return ((long) a << VERSATZ_A) | ((long) b << VERSATZ_B);
	}

	private static long kodiere(int a, int b, int ausgabe, int menge) {
		return paarTeil(a, b) | ((long) ausgabe << VERSATZ_AUSGABE) | menge;
	}

	/** Erste Stelle, an der {@code feld[i] >= ziel} gilt. */
	private static int untereGrenze(long[] feld, long ziel) {
		int lo = 0;
		int hi = feld.length;
		while (lo < hi) {
			int mitte = (lo + hi) >>> 1;
			if (feld[mitte] < ziel) {
				lo = mitte + 1;
			} else {
				hi = mitte;
			}
		}
		return lo;
	}

	private static String text(JsonObject o, String schluessel, String standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive()
				? o.get(schluessel).getAsString() : standard;
	}
}
