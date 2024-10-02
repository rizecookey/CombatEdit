package net.rizecookey.combatedit.client.extension;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public interface MinecraftClientExtension {
    void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider);
}
