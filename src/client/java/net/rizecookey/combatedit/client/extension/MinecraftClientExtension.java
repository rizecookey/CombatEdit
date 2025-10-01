package net.rizecookey.combatedit.client.extension;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public interface MinecraftClientExtension {
    default void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
