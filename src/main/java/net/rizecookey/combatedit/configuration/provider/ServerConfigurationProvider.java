package net.rizecookey.combatedit.configuration.provider;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.RegistriesModifier;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.entity.EntityAttributeMap;
import net.rizecookey.combatedit.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.item.ItemAttributeMap;
import net.rizecookey.combatedit.item.ItemAttributeModifierProvider;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ServerConfigurationProvider implements SimpleSynchronousResourceReloadListener {
    private static ServerConfigurationProvider INSTANCE;

    private final CombatEdit combatEdit;
    private Map<Identifier, BaseProfile> baseProfiles;
    private Configuration configuration;
    private ItemAttributeModifierProvider currentItemModifierProvider;
    private EntityAttributeModifierProvider currentEntityModifierProvider;
    private ItemStackAttributeHelper attributeHelper;
    private final RegistriesModifier registriesModifier;

    private List<EntityAttributes> oldEntityAttributes;
    private List<ItemAttributes> oldItemAttributes;

    public ServerConfigurationProvider(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.registriesModifier = new RegistriesModifier(this);
        this.attributeHelper = new ItemStackAttributeHelper(this);

        INSTANCE = this;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("combatedit", "server_configuration_provider");
    }

    @Override
    public void reload(ResourceManager manager) {
        reloadBaseProfiles(manager);
        updateConfiguration();

        applyConfiguration();
    }

    public void unload() {
        LOGGER.info("Reverting entity and item attribute modifications...");
        this.registriesModifier.revertModifications();
        LOGGER.info("Done.");

        this.configuration = null;
        this.currentEntityModifierProvider = null;
        this.currentItemModifierProvider = null;
        this.attributeHelper = null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ItemAttributeModifierProvider getCurrentItemModifierProvider() {
        return currentItemModifierProvider;
    }

    public EntityAttributeModifierProvider getCurrentEntityModifierProvider() {
        return currentEntityModifierProvider;
    }

    public ItemStackAttributeHelper getAttributeHelper() {
        return attributeHelper;
    }

    public RegistriesModifier getModifier() {
        return registriesModifier;
    }

    private void reloadBaseProfiles(ResourceManager manager) {
        LOGGER.info("Loading base profiles...");
        baseProfiles = BaseProfile.find(manager);
        LOGGER.info("Found {} base profiles: {}", baseProfiles.size(),
                baseProfiles.keySet()
                        .stream()
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")));
    }

    private void updateConfiguration() {
        Settings settings = combatEdit.getSettings();
        BaseProfile selectedProfile = baseProfiles.get(settings.getSelectedBaseProfile());
        if (selectedProfile == null) {
            LOGGER.error("No base profile with id {} found! Using default configuration.", settings.getSelectedBaseProfile());
            configuration = MutableConfiguration.loadDefault();
            return;
        }

        configuration = new ConfigurationView(settings.getConfigurationOverrides(), selectedProfile.getConfiguration());
    }

    private void applyConfiguration() {
        if (registriesModifier.areRegistriesModified()) {
            if (!Objects.equals(oldItemAttributes, configuration.getItemAttributes()) || !Objects.equals(oldEntityAttributes, configuration.getEntityAttributes())) {
                LOGGER.warn("Hot reloading of attribute configurations is currently not supported! Please restart the server or rejoin the world to apply new settings");
            }

            return;
        }

        LOGGER.info("Applying entity and item attribute modifications...");
        currentEntityModifierProvider = EntityAttributeMap.fromConfiguration(configuration.getEntityAttributes(), DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY::get);
        currentItemModifierProvider = ItemAttributeMap.fromConfiguration(configuration.getItemAttributes(), item -> item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
        registriesModifier.makeModifications();
        LOGGER.info("Done.");

        oldItemAttributes = List.copyOf(configuration.getItemAttributes());
        oldEntityAttributes = List.copyOf(configuration.getEntityAttributes());
    }

    public static ServerConfigurationProvider getInstance() {
        return INSTANCE;
    }
}
