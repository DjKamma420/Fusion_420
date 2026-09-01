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

/** Client entry point. Loads configuration, data, version checks, and the overlay. */
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
						LOG.error("Failed to load recipe data", fehler);
					} else {
						rezeptbuch = buch;
					}
				});

		if (einstellungen.aufAktualisierungPruefen) {
			Versionspruefung.neuereFassung(eigeneVersion())
					.thenAccept(fassung -> {
						neuereFassung = fassung;
						if (fassung != null) {
							LOG.info("Newer release available: {}", fassung);
						}
					});
		}

		Overlay.verdrahten(new Kontext(
				einstellungen,
				preisbuch,
				() -> rezeptbuch,
				() -> neuereFassung,
				() -> einstellungen.speichern(konfig)));

		LOG.info("Fusion 420 {} ready — title pattern \"{}\", buy mode {}, sell mode {}",
				eigeneVersion(), einstellungen.titelMuster, einstellungen.einkaufsModus, einstellungen.verkaufsModus);
	}

	private static String eigeneVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(behaelter -> behaelter.getMetadata().getVersion().getFriendlyString())
				.orElse("0.0.0");
	}
}
