package net.rizecookey.combatedit.configuration.provider;

import net.fabricmc.fabric.api.resource.v1.reloader.SimpleResourceReloader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.modification.PropertyModifier;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ConfigurationManager extends SimpleResourceReloader<ConfigurationManager.LoadResult> {
    private static ConfigurationManager INSTANCE;

    private final CombatEdit combatEdit;

    private Configuration configuration;
    private final ItemStackAttributeHelper attributeHelper;
    private final PropertyModifier propertyModifier;
    private final Map<ResourceLocation, List<ProfileExtensionProvider>> registeredProfileExtensions;

    private Map<ResourceLocation, BaseProfile> baseProfiles;

    private List<EntityAttributes> oldEntityAttributes;
    private List<ItemAttributes> oldItemAttributes;
    private List<ItemComponents> oldItemComponents;
    private long lastAttributeReload = Long.MIN_VALUE;

    public ConfigurationManager(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.propertyModifier = new PropertyModifier(this);
        this.attributeHelper = new ItemStackAttributeHelper(this);
        this.registeredProfileExtensions = new HashMap<>();

        INSTANCE = this;
    }

    @Override
    protected LoadResult prepare(SharedState store) {
        ResourceManager manager = store.resourceManager();
        var settings = loadSettings(combatEdit);
        var baseProfiles = loadBaseProfiles(manager);
        ResourceLocation selectedProfile = settings.getSelectedBaseProfile();
        if (!baseProfiles.containsKey(selectedProfile)) {
            LOGGER.error("No base profile with id {} found! Using default profile.", settings.getSelectedBaseProfile());
            selectedProfile = Settings.loadDefault().getSelectedBaseProfile();
            if (!baseProfiles.containsKey(selectedProfile)) {
                LOGGER.error("Failed to load CombatEdit configuration resources: Default base profile does not exist");
                return null;
            }
        }
        LOGGER.info("Selected base profile: {}", selectedProfile.toString());
        var profileExtensions = loadProfileExtensions(manager, selectedProfile);

        return new LoadResult(settings, baseProfiles, profileExtensions);
    }

    @Override
    protected void apply(LoadResult prepared, SharedState store) {
        if (prepared == null) {
            updateConfiguration(null);
            return;
        }

        baseProfiles = prepared.baseProfiles();

        List<ProfileExtension> withCustom = new ArrayList<>(prepared.profileExtensions());
        registeredProfileExtensions.getOrDefault(prepared.settings().getSelectedBaseProfile(), new ArrayList<>())
                .forEach(provider -> withCustom.add(provider.provideExtension(
                        prepared.baseProfiles().get(prepared.settings().getSelectedBaseProfile()),
                        getModifier()
                )));

        updateConfiguration(new LoadResult(prepared.settings(), prepared.baseProfiles(), withCustom));
        boolean modificationsChanged = !Objects.equals(oldItemAttributes, configuration.getItemAttributes())
                || !Objects.equals(oldEntityAttributes, configuration.getEntityAttributes())
                || !Objects.equals(oldItemComponents, configuration.getItemComponents());

        if (modificationsChanged) {
            adjustModifications();
        }
    }

    public record LoadResult(Settings settings, Map<ResourceLocation, BaseProfile> baseProfiles, List<ProfileExtension> profileExtensions) {}

    public CombatEdit getCombatEdit() {
        return combatEdit;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ItemStackAttributeHelper getAttributeHelper() {
        return attributeHelper;
    }

    public PropertyModifier getModifier() {
        return propertyModifier;
    }

    public MinecraftServer getCurrentServer() {
        return combatEdit.getCurrentServer();
    }

    public Map<ResourceLocation, BaseProfile> getBaseProfiles() {
        return baseProfiles;
    }

    public long getLastAttributeReload() {
        return lastAttributeReload;
    }

    private static Settings loadSettings(CombatEdit combatEdit) {
        return combatEdit.getCurrentSettings();
    }

    private static Map<ResourceLocation, BaseProfile> loadBaseProfiles(ResourceManager manager) {
        var result = BaseProfile.find(manager);
        LOGGER.info("Found {} base profiles: {}", result.size(),
                result.keySet()
                        .stream()
                        .map(ResourceLocation::toString)
                        .collect(Collectors.joining(", ")));

        return result;
    }

    private static List<ProfileExtension> loadProfileExtensions(ResourceManager manager, ResourceLocation baseProfileSelected) {
        var result = ProfileExtension.findForProfile(manager, baseProfileSelected);
        LOGGER.info("Found {} base profile extensions for {}", result.size(), baseProfileSelected.toString());

        return result;
    }

    private void updateConfiguration(LoadResult data) {
        if (data == null) {
            configuration = MutableConfiguration.loadDefault();
            LOGGER.warn("Default configuration loaded.");
            return;
        }
        List<Configuration> prioritizedConfigurationList = new ArrayList<>();
        prioritizedConfigurationList.add(data.settings().getConfigurationOverrides());
        prioritizedConfigurationList.addAll(data.profileExtensions().stream()
                .sorted(Comparator.comparingInt(ProfileExtension::getPriority).reversed())
                .map(ProfileExtension::getConfigurationOverrides)
                .toList());
        prioritizedConfigurationList.add(data.baseProfiles().get(data.settings().getSelectedBaseProfile()).getConfiguration());

        configuration = new ConfigurationView(prioritizedConfigurationList.toArray(new Configuration[0])).compileCurrentState();
        LOGGER.info("Configuration updated.");
    }

    private void adjustModifications() {
        propertyModifier.makeModifications();

        oldItemAttributes = List.copyOf(configuration.getItemAttributes());
        oldEntityAttributes = List.copyOf(configuration.getEntityAttributes());
        oldItemComponents = List.copyOf(configuration.getItemComponents());
        lastAttributeReload = System.currentTimeMillis();
        LOGGER.info("Adjusted attribute modifications.");
    }

    public void registerProfileExtension(ResourceLocation profileId, ProfileExtensionProvider extensionProvider) {
        this.registeredProfileExtensions.computeIfAbsent(profileId, key -> new ArrayList<>()).add(extensionProvider);
    }

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }
}
