package net.rizecookey.combatedit.client.configscreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.rizecookey.combatedit.client.CombatEditClient;

import java.util.concurrent.CompletableFuture;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuApiImpl::loadingScreenWhileLoadingComponents;
    }

    private static Screen loadingScreenWhileLoadingComponents(Screen parentScreen) {
        Minecraft mc = Minecraft.getInstance();

        CompletableFuture.runAsync(() -> {
            // initialize data components
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                    .forEach(DataComponentInitializers.PendingComponents::apply);
        }).thenRun(() -> mc.schedule(() -> {
            CombatEditClient combatEditClient = CombatEditClient.getInstance();
            Screen configScreen = ConfigurationScreenBuilder.buildScreen(combatEditClient, parentScreen);
            mc.setScreenAndShow(configScreen);
        }));

        return new GenericMessageScreen(Component.translatable("screen.combatedit.component_initialization"));
    }
}
