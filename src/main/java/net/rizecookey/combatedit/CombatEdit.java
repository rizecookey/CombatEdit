package net.rizecookey.combatedit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationProvider;
import net.rizecookey.combatedit.utils.serializers.AttributeModifierSlotSerializer;
import net.rizecookey.combatedit.utils.serializers.EntityAttributeModifier$OperationSerializer;
import net.rizecookey.combatedit.utils.serializers.IdentifierSerializer;
import net.rizecookey.combatedit.utils.serializers.TextSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CombatEdit implements ModInitializer {
    private static CombatEdit INSTANCE;
    public static final Path DEFAULT_SETTINGS_PATH = FabricLoader.getInstance().getConfigDir().resolve("combatedit/settings.json");
    public static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(AttributeModifierSlot.class, new AttributeModifierSlotSerializer())
            .registerTypeAdapter(EntityAttributeModifier.Operation.class, new EntityAttributeModifier$OperationSerializer())
            .registerTypeAdapter(Text.class, new TextSerializer())
            .create();

    private ServerConfigurationProvider serverConfigurationProvider;
    private Settings settings;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        loadSettings();
        registerServerConfigurationProvider();

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    public void loadSettings() {
        LOGGER.info("Loading settings...");
        try {
            if (!Files.exists(DEFAULT_SETTINGS_PATH)) {
                LOGGER.info("No settings file found, loading and saving default...");
                setSettings(loadDefaultAndSave(DEFAULT_SETTINGS_PATH));
                LOGGER.info("Done.");
                return;
            }

            setSettings(Settings.load(DEFAULT_SETTINGS_PATH));
            LOGGER.info("Done.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerServerConfigurationProvider() {
        this.serverConfigurationProvider = new ServerConfigurationProvider(this);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(serverConfigurationProvider);
    }

    public Settings getSettings() {
        return settings;
    }

    private void setSettings(Settings settings) {
        this.settings = settings;
    }

    private Settings loadDefaultAndSave(Path savePath) throws IOException {
        var settings = Settings.loadDefault();
        settings.save(savePath);

        return settings;
    }

    public void resetSettings() throws IOException {
        LOGGER.info("Resetting settings...");
        setSettings(loadDefaultAndSave(DEFAULT_SETTINGS_PATH));
        LOGGER.info("Done.");
    }

    public ServerConfigurationProvider getServerConfigurationProvider() {
        return serverConfigurationProvider;
    }

    public static CombatEdit getInstance() {
        return INSTANCE;
    }
}
