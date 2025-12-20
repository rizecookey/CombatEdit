package net.rizecookey.combatedit.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.client.configscreen.InvalidConfigScreen;
import net.rizecookey.combatedit.client.event.ClientEvents;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class CombatEditClient extends CombatEdit {
    private static @Nullable CombatEditClient INSTANCE;

    public CombatEditClient() {
        super();
        INSTANCE = this;
    }

    @Override
    protected void onSettingsLoadError(InvalidConfigurationException exception) {
        LOGGER.error("Settings validation failed", exception);
        setCurrentSettings(Settings.loadDefault());
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.combatEdit$addInitScreen(onClose -> new InvalidConfigScreen(exception, onClose)));
    }

    @Override
    protected void onSettingsLoadError(IOException exception) {
        LOGGER.error("Failed to load the settings file", exception);
        LOGGER.warn("Using default settings.");
        setCurrentSettings(Settings.loadDefault());
        ClientEvents.CLIENT_FINISHED_LOADING.register(client -> sendErrorNotification(client, "settings_load_error"));
    }

    @Override
    public void warnAboutItemIncompatibility(List<Item> items) {
        super.warnAboutItemIncompatibility(items);
        sendErrorNotification(Minecraft.getInstance(), "incompatibility_item");
    }

    @Override
    public void warnAboutEntityIncompatibility(List<EntityType<? extends LivingEntity>> entities) {
        super.warnAboutEntityIncompatibility(entities);
        sendErrorNotification(Minecraft.getInstance(), "incompatibility_entity");
    }

    public static void sendErrorNotification(Minecraft client, String errorKey) {
        SystemToast toast = SystemToast.multiline(client, SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("notification.combatedit.%s.title".formatted(errorKey)), Component.translatable("notification.combatedit.%s.description".formatted(errorKey)));
        client.getToastManager().addToast(toast);
    }

    public static CombatEditClient getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("CombatEdit client has not been loaded yet");
        }
        return INSTANCE;
    }
}
