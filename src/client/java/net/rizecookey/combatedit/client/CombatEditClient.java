package net.rizecookey.combatedit.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.client.configscreen.InvalidConfigScreen;
import net.rizecookey.combatedit.client.event.ClientEvents;
import net.rizecookey.combatedit.client.extension.MinecraftClientExtension;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;

import java.io.IOException;

public class CombatEditClient extends CombatEdit {
    private static CombatEditClient INSTANCE;

    public CombatEditClient() {
        super();
        INSTANCE = this;
    }

    @Override
    protected void onSettingsLoadError(InvalidConfigurationException exception) {
        LOGGER.error("Settings validation failed", exception);
        setCurrentSettings(null);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ((MinecraftClientExtension) client).combatEdit$addInitScreen(onClose -> new InvalidConfigScreen(exception, onClose)));
    }

    @Override
    protected void onSettingsLoadError(IOException exception) {
        LOGGER.error("Failed to load the settings file", exception);
        LOGGER.warn("Using default settings.");
        setCurrentSettings(Settings.loadDefault());
        ClientEvents.CLIENT_FINISHED_LOADING.register(client -> sendErrorNotification(client, "settings_load_error"));
    }

    public static void sendErrorNotification(MinecraftClient client, String errorKey) {
        SystemToast toast = SystemToast.create(client, SystemToast.Type.PACK_LOAD_FAILURE, Text.translatable("notification.combatedit.%s.title".formatted(errorKey)), Text.translatable("notification.combatedit.%s.description".formatted(errorKey)));
        client.getToastManager().add(toast);
    }

    public static CombatEditClient getInstance() {
        return INSTANCE;
    }
}
