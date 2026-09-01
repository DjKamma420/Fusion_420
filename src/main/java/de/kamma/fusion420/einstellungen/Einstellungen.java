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

/**
 * Alle Stellschrauben der Mod.
 *
 * <p>Bewusst von Hand aus JSON gelesen statt ueber Gsons Reflexion: der
 * Zugriff auf private Felder fremder Klassen ist unter Javas Modulsystem
 * eine Dauerbaustelle, und fehlende Schluessel sollen still den Standard
 * behalten statt {@code null} zu setzen.
 */
public final class Einstellungen {

	/**
	 * Warum ein Muster und keine feste Zeichenkette: Hypixel benennt
	 * Menuetitel gern um. Ein Regex in der Konfig laesst sich nachziehen,
	 * ohne dass eine neue Version noetig waere.
	 */
	public String titelMuster = "(?i)fusion";

	/** Auf anderen Servern schweigt die Mod. */
	public boolean nurAufHypixel = true;

	/** Nur die Kiste auswerten, nicht zusaetzlich das eigene Inventar. */
	public boolean nurContainerSlots = true;

	/** SOFORT, ORDER oder GEMISCHT — siehe {@code rechner.Modus}. */
	public String preisModus = "SOFORT";

	/** Bazaar-Verkaufssteuer in Prozent. Sinkt mit Community-Upgrades. */
	public double bazaarSteuerProzent = 1.25;

	/** Ausgaben unter dieser Wochenmenge gelten als zu illiquide. */
	public long mindestVolumenWoche = 5000L;

	public int anzahlEintraege = 3;
	public int aktualisierungSekunden = 60;

	public int overlayVersatzX = 6;
	public int overlayVersatzY = 0;
	public int overlayBreite = 200;

	public boolean rezepteOnlineLaden = true;
	public String rezeptQuelleUrl =
			"https://raw.githubusercontent.com/DjKamma420/Fusion_420/main/src/main/resources/daten/fusion-data.json";

	/** Beim Start nachsehen, ob es eine neuere Freigabe der Mod gibt. */
	public boolean aufAktualisierungPruefen = true;

	/** GLFW-Tastencodes. 77 = M, 82 = R. */
	public int tasteModusWechsel = 77;
	public int tasteAktualisieren = 82;

	private transient Pattern titelRegex;

	public boolean titelPasst(String titel) {
		if (titel == null) {
			return false;
		}
		if (titelRegex == null) {
			try {
				titelRegex = Pattern.compile(titelMuster);
			} catch (PatternSyntaxException e) {
				titelRegex = Pattern.compile("(?i)fusion");
			}
		}
		return titelRegex.matcher(titel).find();
	}

	/** Nach einer Aenderung des Musters muss das kompilierte Regex weg. */
	public void musterVerworfen() {
		titelRegex = null;
	}

	public static Einstellungen laden(Path datei) {
		Einstellungen e = new Einstellungen();
		try {
			if (Files.isRegularFile(datei)) {
				JsonObject o = JsonParser.parseString(Files.readString(datei, StandardCharsets.UTF_8))
						.getAsJsonObject();
				e.titelMuster = text(o, "titelMuster", e.titelMuster);
				e.nurAufHypixel = flagge(o, "nurAufHypixel", e.nurAufHypixel);
				e.nurContainerSlots = flagge(o, "nurContainerSlots", e.nurContainerSlots);
				e.preisModus = text(o, "preisModus", e.preisModus);
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
		} catch (Exception ignoriert) {
			// Eine kaputte Konfig darf den Start nicht verhindern. Standard genuegt.
		}
		e.gesundschrumpfen();
		return e;
	}

	public void speichern(Path datei) {
		JsonObject o = new JsonObject();
		o.addProperty("titelMuster", titelMuster);
		o.addProperty("nurAufHypixel", nurAufHypixel);
		o.addProperty("nurContainerSlots", nurContainerSlots);
		o.addProperty("preisModus", preisModus);
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
			if (ordner != null) {
				Files.createDirectories(ordner);
			}
			Files.writeString(datei, new GsonBuilder().setPrettyPrinting().create().toJson(o),
					StandardCharsets.UTF_8);
		} catch (IOException ignoriert) {
			// Nicht schreiben zu koennen ist aergerlich, aber kein Grund abzustuerzen.
		}
	}

	/** Haelt Werte in Bereichen, in denen das Overlay noch benutzbar bleibt. */
	private void gesundschrumpfen() {
		anzahlEintraege = Math.clamp(anzahlEintraege, 1, 10);
		aktualisierungSekunden = Math.clamp(aktualisierungSekunden, 10, 3600);
		overlayBreite = Math.clamp(overlayBreite, 120, 400);
		bazaarSteuerProzent = Math.clamp(bazaarSteuerProzent, 0.0, 25.0);
		mindestVolumenWoche = Math.max(0L, mindestVolumenWoche);
	}

	private static String text(JsonObject o, String schluessel, String standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive()
				? o.get(schluessel).getAsString() : standard;
	}

	private static boolean flagge(JsonObject o, String schluessel, boolean standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive()
				? o.get(schluessel).getAsBoolean() : standard;
	}

	private static double kommazahl(JsonObject o, String schluessel, double standard) {
		return o.has(schluessel) && o.get(schluessel).isJsonPrimitive()
				? o.get(schluessel).getAsDouble() : standard;
	}
}
