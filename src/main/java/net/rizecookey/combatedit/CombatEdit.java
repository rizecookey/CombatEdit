package net.rizecookey.combatedit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.Configuration;
import net.rizecookey.combatedit.entity.EntityAttributeMap;
import net.rizecookey.combatedit.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.item.ItemAttributeMap;
import net.rizecookey.combatedit.item.ItemAttributeModifierProvider;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import net.rizecookey.combatedit.utils.serializers.AttributeModifierSlotSerializer;
import net.rizecookey.combatedit.utils.serializers.EntityAttributeModifier$OperationSerializer;
import net.rizecookey.combatedit.utils.serializers.IdentifierSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CombatEdit implements ModInitializer {
    private static CombatEdit INSTANCE;
    public static final Path DEFAULT_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("combatedit/config.json");
    public static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(AttributeModifierSlot.class, new AttributeModifierSlotSerializer())
            .registerTypeAdapter(EntityAttributeModifier.Operation.class, new EntityAttributeModifier$OperationSerializer())
            .create();

    private RegistriesModifier modifier;
    private Configuration config;
    private ItemAttributeModifierProvider currentItemModifierProvider;
    private EntityAttributeModifierProvider currentEntityModifierProvider;
    private ItemStackAttributeHelper attributeHelper;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        loadConfig();

        modifier = new RegistriesModifier(this);
        attributeHelper = new ItemStackAttributeHelper(this);

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    private void loadConfig() {
        LOGGER.info("Loading config...");
        try {
            if (!Files.exists(DEFAULT_CONFIG_PATH)) {
                LOGGER.info("No config found, loading and saving default...");
                this.config = loadDefaultAndSave(DEFAULT_CONFIG_PATH);
                LOGGER.info("Done.");
                return;
            }

            this.config = Configuration.load(DEFAULT_CONFIG_PATH);
            LOGGER.info("Done.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    private Configuration loadDefaultAndSave(Path savePath) throws IOException {
        var config = Configuration.loadDefault();
        config.save(savePath);

        return config;
    }

    public void resetConfig() throws IOException {
        LOGGER.info("Resetting config...");
        this.config = loadDefaultAndSave(DEFAULT_CONFIG_PATH);
        LOGGER.info("Done.");
    }

    public ItemStackAttributeHelper getAttributeHelper() {
        return attributeHelper;
    }

    public RegistriesModifier getModifier() {
        return modifier;
    }

    public static CombatEdit getInstance() {
        return INSTANCE;
    }

    public void reloadProviders() {
        currentEntityModifierProvider = EntityAttributeMap.fromConfiguration(config.getEntityAttributes(), DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY::get);
        currentItemModifierProvider = ItemAttributeMap.fromConfiguration(config.getItemAttributes(), item -> item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
    }

    public ItemAttributeModifierProvider getCurrentItemModifierProvider() {
        return currentItemModifierProvider;
    }

    public EntityAttributeModifierProvider getCurrentEntityModifierProvider() {
        return currentEntityModifierProvider;
    }
}
