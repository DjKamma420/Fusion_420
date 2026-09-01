package de.kamma.fusion420.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Oeffnet die geschuetzten Masse des Behaelter-Bildschirms.
 *
 * <p>Bewusst nur ein Zugriff und kein {@code @Inject}: das Overlay wird ueber
 * {@code ScreenEvents} gezeichnet, nicht durch einen Eingriff in
 * {@code render}. Genau dort geraten SkyHanni, NEU und Patcher sich sonst
 * gegenseitig in den Weg. Ein Zugriff aendert keinen Kontrollfluss und kann
 * mit keiner anderen Mod kollidieren.
 */
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenZugriff {

	@Accessor("leftPos")
	int fusion420$linkerRand();

	@Accessor("topPos")
	int fusion420$obererRand();

	@Accessor("imageWidth")
	int fusion420$breite();

	@Accessor("imageHeight")
	int fusion420$hoehe();
}
