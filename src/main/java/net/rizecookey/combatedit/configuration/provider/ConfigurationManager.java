package net.rizecookey.combatedit.configuration.provider;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.modification.AttributesModifier;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import net.rizecookey.combatedit.utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ConfigurationManager implements SimpleResourceReloadListener<ConfigurationManager.LoadResult> {
    private static ConfigurationManager INSTANCE;

    private final CombatEdit combatEdit;

    private Configuration configuration;
    private final ItemStackAttributeHelper attributeHelper;
    private final AttributesModifier attributesModifier;
    private final Map<Identifier, List<ProfileExtensionProvider>> registeredProfileExtensions;

    private Map<Identifier, BaseProfile> baseProfiles;

    private List<EntityAttributes> oldEntityAttributes;
    private List<ItemAttributes> oldItemAttributes;
    private long lastAttributeReload = Long.MIN_VALUE;

    public ConfigurationManager(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.attributesModifier = new AttributesModifier(this);
        this.attributeHelper = new ItemStackAttributeHelper(this);
        this.registeredProfileExtensions = new HashMap<>();

        INSTANCE = this;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("combatedit", "server_configuration_provider");
    }

    public record LoadResult(Settings settings, Map<Identifier, BaseProfile> baseProfiles, List<ProfileExtension> profileExtensions) {}

    @Override
    public CompletableFuture<LoadResult> load(ResourceManager manager, Profiler profiler, Executor executor) {
        var settingsLoader = CompletableFuture.supplyAsync(() -> loadSettings(combatEdit), executor);
        var baseProfileLoader = CompletableFuture.supplyAsync(() -> loadBaseProfiles(manager), executor);
        var profileLoader = settingsLoader.thenCombineAsync(baseProfileLoader, (settings, baseProfiles) -> {
            Identifier selectedProfile = settings.getSelectedBaseProfile();
            if (!baseProfiles.containsKey(selectedProfile)) {
                LOGGER.error("No base profile with id {} found! Using default profile.", settings.getSelectedBaseProfile());
                selectedProfile = Settings.loadDefault().getSelectedBaseProfile();
                if (!baseProfiles.containsKey(selectedProfile)) {
                    throw new IllegalStateException("Default base profile does not exist");
                }
            }
            LOGGER.info("Selected base profile: {}", selectedProfile.toString());

            return new Pair<>(baseProfiles, loadProfileExtensions(manager, selectedProfile));
        }, executor);

        return profileLoader.thenCombineAsync(settingsLoader, (profile, settings) -> new LoadResult(settings, profile.first(), profile.second()), executor).exceptionallyAsync(e -> {
            LOGGER.error("Failed to load CombatEdit configuration resources", e);
            return null;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(LoadResult data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            if (data == null) {
                throw new IllegalStateException("Apply stage did not provide valid data");
            }

            baseProfiles = data.baseProfiles();

            List<ProfileExtension> withCustom = new ArrayList<>(data.profileExtensions());
            registeredProfileExtensions.getOrDefault(data.settings().getSelectedBaseProfile(), new ArrayList<>())
                    .forEach(provider -> withCustom.add(provider.provideExtension(
                            data.baseProfiles().get(data.settings().getSelectedBaseProfile()),
                            item -> this.getModifier().getOriginalDefaults(item),
                            type -> this.getModifier().getOriginalDefaults(type)
                    )));

            updateConfiguration(new LoadResult(data.settings(), data.baseProfiles(), withCustom));
            boolean attributeConfigChanged = !Objects.equals(oldItemAttributes, configuration.getItemAttributes()) || !Objects.equals(oldEntityAttributes, configuration.getEntityAttributes());

            if (attributeConfigChanged) {
                adjustModifications();
            }
        }, executor).exceptionallyAsync(e -> {
            LOGGER.error("Failed to apply the configuration", e);
            updateConfiguration(null);
            return null;
        }, executor);
    }

    public CombatEdit getCombatEdit() {
        return combatEdit;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ItemStackAttributeHelper getAttributeHelper() {
        return attributeHelper;
    }

    public AttributesModifier getModifier() {
        return attributesModifier;
    }

    public MinecraftServer getCurrentServer() {
        return combatEdit.getCurrentServer();
    }

    public Map<Identifier, BaseProfile> getBaseProfiles() {
        return baseProfiles;
    }

    public long getLastAttributeReload() {
        return lastAttributeReload;
    }

    private static Settings loadSettings(CombatEdit combatEdit) {
        return combatEdit.getCurrentSettings();
    }

    private static Map<Identifier, BaseProfile> loadBaseProfiles(ResourceManager manager) {
        var result = BaseProfile.find(manager);
        LOGGER.info("Found {} base profiles: {}", result.size(),
                result.keySet()
                        .stream()
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")));

        return result;
    }

    private static List<ProfileExtension> loadProfileExtensions(ResourceManager manager, Identifier baseProfileSelected) {
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
        attributesModifier.makeModifications();

        oldItemAttributes = List.copyOf(configuration.getItemAttributes());
        oldEntityAttributes = List.copyOf(configuration.getEntityAttributes());
        lastAttributeReload = System.currentTimeMillis();
        LOGGER.info("Adjusted attribute modifications.");
    }

    public void registerProfileExtension(Identifier profileId, ProfileExtensionProvider extensionProvider) {
        this.registeredProfileExtensions.computeIfAbsent(profileId, key -> new ArrayList<>()).add(extensionProvider);
    }

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }
}
