package de.kamma.fusion420;

import de.kamma.fusion420.daten.Rezeptbuch;
import de.kamma.fusion420.daten.Rezeptquelle;
import de.kamma.fusion420.einstellungen.Einstellungen;
import de.kamma.fusion420.gui.Overlay;
import de.kamma.fusion420.markt.Bazaar;
import de.kamma.fusion420.markt.Preisbuch;
import de.kamma.fusion420.wartung.Versionspruefung;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Einstiegspunkt. Laedt die Konfiguration, stoesst Rezeptabruf und
 * Versionspruefung an und haengt das Overlay an die Bildschirm-Ereignisse.
 *
 * <p>Nichts davon blockiert den Start: die Rezepte kommen im Hintergrund,
 * die Preise sowieso erst, wenn ein Fusions-GUI offen ist.
 */
public final class Fusion420Client implements ClientModInitializer {

	public static final String MOD_ID = "fusion_420";

	private static final Logger LOG = LoggerFactory.getLogger("Fusion 420");

	private static volatile Rezeptbuch rezeptbuch;
	private static volatile String neuereFassung;

	@Override
	public void onInitializeClient() {
		Path ordner = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
		Path konfig = ordner.resolve("einstellungen.json");

		Einstellungen einstellungen = Einstellungen.laden(konfig);
		einstellungen.speichern(konfig);

		Preisbuch preisbuch = new Preisbuch(new Bazaar());

		Rezeptquelle.laden(einstellungen, ordner.resolve("fusion-data.json"))
				.whenComplete((buch, fehler) -> {
					if (fehler != null) {
						LOG.error("Rezepte konnten nicht geladen werden", fehler);
					} else {
						rezeptbuch = buch;
					}
				});

		if (einstellungen.aufAktualisierungPruefen) {
			Versionspruefung.neuereFassung(eigeneVersion())
					.thenAccept(fassung -> {
						neuereFassung = fassung;
						if (fassung != null) {
							LOG.info("Neuere Fassung verfuegbar: {}", fassung);
						}
					});
		}

		Overlay.verdrahten(new Kontext(
				einstellungen,
				preisbuch,
				() -> rezeptbuch,
				() -> neuereFassung,
				() -> einstellungen.speichern(konfig)));

		LOG.info("Fusion 420 {} bereit — Titelmuster \"{}\", Modus {}",
				eigeneVersion(), einstellungen.titelMuster, einstellungen.preisModus);
	}

	private static String eigeneVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(behaelter -> behaelter.getMetadata().getVersion().getFriendlyString())
				.orElse("0.0.0");
	}
}
