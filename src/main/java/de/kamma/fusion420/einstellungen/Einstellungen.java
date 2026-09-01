package de.kamma.fusion420.einstellungen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Alle Stellschrauben der Mod. */
public final class Einstellungen {
	public String titelMuster = "(?i)fusion";
	public boolean nurAufHypixel = true;
	public boolean nurContainerSlots = true;
	/** Altkompatibles Feld; neue Konfiguration nutzt Einkauf/Verkauf getrennt. */
	public String preisModus = "SOFORT";
	public String einkaufsModus = "SOFORT";
	public String verkaufsModus = "SOFORT";
	public double bazaarSteuerProzent = 1.25;
	public long mindestVolumenWoche = 5000L;
	public int anzahlEintraege = 6;
	public int aktualisierungSekunden = 60;
	public int overlayVersatzX = 6;
	public int overlayVersatzY = 0;
	public int overlayBreite = 230;
	public boolean rezepteOnlineLaden = true;
	public String rezeptQuelleUrl = "https://raw.githubusercontent.com/DjKamma420/Fusion_420/main/src/main/resources/daten/fusion-data.json";
	public boolean aufAktualisierungPruefen = true;
	public int tasteModusWechsel = 77;
	public int tasteAktualisieren = 82;

	private transient Pattern titelRegex;

	public boolean titelPasst(String titel) {
		if (titel == null) return false;
		if (titelRegex == null) {
			try { titelRegex = Pattern.compile(titelMuster); }
			catch (PatternSyntaxException e) { titelRegex = Pattern.compile("(?i)fusion"); }
		}
		return titelRegex.matcher(titel).find();
	}

	public void musterVerworfen() { titelRegex = null; }

	public static Einstellungen laden(Path datei) {
		Einstellungen e = new Einstellungen();
		try {
			if (Files.isRegularFile(datei)) {
				JsonObject o = JsonParser.parseString(Files.readString(datei, StandardCharsets.UTF_8)).getAsJsonObject();
				e.titelMuster = text(o, "titelMuster", e.titelMuster);
				e.nurAufHypixel = flagge(o, "nurAufHypixel", e.nurAufHypixel);
				e.nurContainerSlots = flagge(o, "nurContainerSlots", e.nurContainerSlots);
				e.preisModus = text(o, "preisModus", e.preisModus);
				boolean neueMarktWahl = o.has("einkaufsModus") || o.has("verkaufsModus");
				e.einkaufsModus = text(o, "einkaufsModus", e.einkaufsModus);
				e.verkaufsModus = text(o, "verkaufsModus", e.verkaufsModus);
				if (!neueMarktWahl) {
					switch (e.preisModus.toUpperCase()) {
						case "ORDER" -> { e.einkaufsModus = "ORDER"; e.verkaufsModus = "ORDER"; }
						case "GEMISCHT" -> { e.einkaufsModus = "SOFORT"; e.verkaufsModus = "ORDER"; }
						default -> { e.einkaufsModus = "SOFORT"; e.verkaufsModus = "SOFORT"; }
					}
				}
				e.bazaarSteuerProzent = kommazahl(o, "bazaarSteuerProzent", e.bazaarSteuerProzent);
				e.mindestVolumenWoche = (long) kommazahl(o, "mindestVolumenWoche", e.mindestVolumenWoche);
				e.anzahlEintraege = (int) kommazahl(o, "anzahlEintraege", e.anzahlEintraege);
				e.aktualisierungSekunden = (int) kommazahl(o, "aktualisierungSekunden", e.aktualisierungSekunden);
				e.overlayVersatzX = (int) kommazahl(o, "overlayVersatzX", e.overlayVersatzX);
				e.overlayVersatzY = (int) kommazahl(o, "overlayVersatzY", e.overlayVersatzY);
				e.overlayBreite = (int) kommazahl(o, "overlayBreite", e.overlayBreite);
				e.rezepteOnlineLaden = flagge(o, "rezepteOnlineLaden", e.rezepteOnlineLaden);
				e.rezeptQuelleUrl = text(o, "rezeptQuelleUrl", e.rezeptQuelleUrl);
				e.aufAktualisierungPruefen = flagge(o, "aufAktualisierungPruefen", e.aufAktualisierungPruefen);
				e.tasteModusWechsel = (int) kommazahl(o, "tasteModusWechsel", e.tasteModusWechsel);
				e.tasteAktualisieren = (int) kommazahl(o, "tasteAktualisieren", e.tasteAktualisieren);
			}
		} catch (Exception ignoriert) { }
		e.gesundschrumpfen();
		return e;
	}

	public void speichern(Path datei) {
		JsonObject o = new JsonObject();
		o.addProperty("titelMuster", titelMuster);
		o.addProperty("nurAufHypixel", nurAufHypixel);
		o.addProperty("nurContainerSlots", nurContainerSlots);
		o.addProperty("einkaufsModus", einkaufsModus);
		o.addProperty("verkaufsModus", verkaufsModus);
		o.addProperty("bazaarSteuerProzent", bazaarSteuerProzent);
		o.addProperty("mindestVolumenWoche", mindestVolumenWoche);
		o.addProperty("anzahlEintraege", anzahlEintraege);
		o.addProperty("aktualisierungSekunden", aktualisierungSekunden);
		o.addProperty("overlayVersatzX", overlayVersatzX);
		o.addProperty("overlayVersatzY", overlayVersatzY);
		o.addProperty("overlayBreite", overlayBreite);
		o.addProperty("rezepteOnlineLaden", rezepteOnlineLaden);
		o.addProperty("rezeptQuelleUrl", rezeptQuelleUrl);
		o.addProperty("aufAktualisierungPruefen", aufAktualisierungPruefen);
		o.addProperty("tasteModusWechsel", tasteModusWechsel);
		o.addProperty("tasteAktualisieren", tasteAktualisieren);
		try {
			Path ordner = datei.getParent();
			if (ordner != null) Files.createDirectories(ordner);
			Files.writeString(datei, new GsonBuilder().setPrettyPrinting().create().toJson(o), StandardCharsets.UTF_8);
		} catch (IOException ignoriert) { }
	}

	private void gesundschrumpfen() {
		anzahlEintraege = Math.clamp(anzahlEintraege, 1, 10);
		aktualisierungSekunden = Math.clamp(aktualisierungSekunden, 10, 3600);
		overlayBreite = Math.clamp(overlayBreite, 180, 500);
		bazaarSteuerProzent = Math.clamp(bazaarSteuerProzent, 0.0, 25.0);
		mindestVolumenWoche = Math.max(0L, mindestVolumenWoche);
	}

	private static String text(JsonObject o, String schluessel, String standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive() ? o.get(schluessel).getAsString() : standard;
	}
	private static boolean flagge(JsonObject o, String schluessel, boolean standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive() ? o.get(schluessel).getAsBoolean() : standard;
	}
	private static double kommazahl(JsonObject o, String schluessel, double standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive() ? o.get(schluessel).getAsDouble() : standard;
	}
}
