package net.rizecookey.combatedit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.api.CombatEditApi;
import net.rizecookey.combatedit.api.CombatEditInitListener;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import net.rizecookey.combatedit.extension.DynamicDefaultAttributeContainer;
import net.rizecookey.combatedit.utils.serializers.AttributeModifierSlotSerializer;
import net.rizecookey.combatedit.utils.serializers.EntityAttributeModifier$OperationSerializer;
import net.rizecookey.combatedit.utils.serializers.IdentifierSerializer;
import net.rizecookey.combatedit.utils.serializers.MutableConfigurationTypeAdapterFactory;
import net.rizecookey.combatedit.utils.serializers.TextSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CombatEdit implements CombatEditApi {
    private static CombatEdit INSTANCE;
    public static final Path DEFAULT_SETTINGS_PATH = FabricLoader.getInstance().getConfigDir().resolve("combatedit/settings.json");
    public static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new MutableConfigurationTypeAdapterFactory())
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(AttributeModifierSlot.class, new AttributeModifierSlotSerializer())
            .registerTypeAdapter(EntityAttributeModifier.Operation.class, new EntityAttributeModifier$OperationSerializer())
            .registerTypeAdapter(Text.class, new TextSerializer())
            .create();

    private boolean modificationsEnabled;
    private Settings settings;
    private final ConfigurationManager configurationManager;
    private @Nullable MinecraftServer currentServer;

    public CombatEdit() {
        INSTANCE = this;

        this.modificationsEnabled = false;
        this.configurationManager = new ConfigurationManager(this);

        try {
            loadSettings();
        } catch (IOException e) {
            onSettingsLoadError(e);
        } catch (InvalidConfigurationException e) {
            onSettingsLoadError(e);
        }

        registerListeners();
        FabricLoader.getInstance().invokeEntrypoints("combatedit", CombatEditInitListener.class, listener -> listener.onCombatEditInit(this));

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    public Settings getCurrentSettings() {
        return settings;
    }

    protected void setCurrentSettings(Settings settings) {
        this.settings = settings;
    }

    public void loadSettings() throws IOException, InvalidConfigurationException {
        if (!Files.exists(DEFAULT_SETTINGS_PATH)) {
            LOGGER.info("No settings file found, loading and saving default.");
            loadDefaultSettingsAndSave();
        } else {
            setCurrentSettings(Settings.load(DEFAULT_SETTINGS_PATH));
        }
        getCurrentSettings().validate();
        LOGGER.info("Settings loaded.");
    }

    public void saveSettings(Settings settings) throws IOException {
        settings.save(DEFAULT_SETTINGS_PATH);
        setCurrentSettings(settings);
        LOGGER.info("Settings saved.");
    }

    private void useDefaultSettings() {
        setCurrentSettings(Settings.loadDefault());
    }

    private void loadDefaultSettingsAndSave() throws IOException {
        useDefaultSettings();
        saveSettings(getCurrentSettings());
    }

    public void resetSettings() throws IOException {
        loadDefaultSettingsAndSave();
        LOGGER.info("Reset settings.");
    }

    protected void onSettingsLoadError(InvalidConfigurationException exception) {
        LOGGER.error("Settings validation failed", exception);
        LOGGER.warn("Using default settings.");
        settings = Settings.loadDefault();
    }

    protected void onSettingsLoadError(IOException exception) {
        LOGGER.error("Failed to load the settings file", exception);
        LOGGER.warn("Using default settings.");
        settings = Settings.loadDefault();
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public @Nullable MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public void registerListeners() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(configurationManager);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.currentServer = server;
            setModificationsEnabled(true);
            LOGGER.info("Turned on modifications.");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            setModificationsEnabled(false);
            this.currentServer = null;
            LOGGER.info("Turned off modifications.");
        });
    }

    public boolean areModificationsEnabled() {
        return modificationsEnabled;
    }

    public void setModificationsEnabled(boolean enabled) {
        modificationsEnabled = enabled;
        DynamicComponentMap.setUseExchangeable(enabled);
        DynamicDefaultAttributeContainer.setUseExchangeable(enabled);
    }

    public static CombatEdit getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerProfileExtension(Identifier profileId, ProfileExtensionProvider extensionProvider) {
        configurationManager.registerProfileExtension(profileId, extensionProvider);
    }
}
