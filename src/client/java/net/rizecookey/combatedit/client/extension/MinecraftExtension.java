package net.rizecookey.combatedit.client.extension;

import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public interface MinecraftExtension {
    default void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
