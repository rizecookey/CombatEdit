package net.rizecookey.combatedit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
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
import java.io.InputStream;
import java.io.InputStreamReader;

public class CombatEdit implements ModInitializer {
    private static CombatEdit INSTANCE;
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

        LOGGER.info("Loading config...");
        config = loadConfig();

        modifier = new RegistriesModifier(this);
        attributeHelper = new ItemStackAttributeHelper(this);

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    // TODO load config from mod config dir etc...
    private Configuration loadConfig() {
        return loadDefaultConfig(); /* TODO */
    }

    private Configuration loadDefaultConfig() {
        try (InputStream in = getClass().getResourceAsStream("/config.json")) {
            if (in == null) {
                throw new IOException("Resource was not bundled correctly");
            }
            return GSON.fromJson(new InputStreamReader(in), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load default config", e);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void resetConfig() {
        this.config = loadDefaultConfig();
        this.saveConfig();
    }

    public void saveConfig() {
        /* TODO */
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
