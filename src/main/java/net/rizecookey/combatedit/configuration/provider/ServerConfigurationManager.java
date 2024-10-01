package net.rizecookey.combatedit.configuration.provider;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.rizecookey.combatedit.AttributesModifier;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;

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

public class ServerConfigurationManager implements SimpleResourceReloadListener<ServerConfigurationManager.LoadResult> {
    private static ServerConfigurationManager INSTANCE;

    private final CombatEdit combatEdit;
    private MinecraftServer currentServer;

    private Configuration configuration;
    private final ItemStackAttributeHelper attributeHelper;
    private final AttributesModifier attributesModifier;
    private final Map<Identifier, List<ProfileExtensionProvider>> registeredProfileExtensions;

    private List<EntityAttributes> oldEntityAttributes;
    private List<ItemAttributes> oldItemAttributes;
    private long lastAttributeReload = Long.MIN_VALUE;

    public ServerConfigurationManager(CombatEdit combatEdit) {
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

    public record LoadResult(Settings settings, BaseProfile baseProfile, List<ProfileExtension> profileExtensions) {}

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

            return new Pair<>(baseProfiles.get(selectedProfile), loadProfileExtensions(manager, selectedProfile));
        }, executor);

        return profileLoader.thenCombineAsync(settingsLoader, (profile, settings) -> new LoadResult(settings, profile.getLeft(), profile.getRight()), executor).exceptionallyAsync(e -> {
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

            List<ProfileExtension> withCustom = new ArrayList<>(data.profileExtensions());
            registeredProfileExtensions.getOrDefault(data.settings().getSelectedBaseProfile(), new ArrayList<>())
                    .forEach(provider -> withCustom.add(provider.provideExtension(
                            data.baseProfile(),
                            item -> this.getModifier().getOriginalDefaults(item),
                            type -> this.getModifier().getOriginalDefaults(type)
                    )));

            updateConfiguration(new LoadResult(data.settings(), data.baseProfile(), withCustom));

            boolean previouslyModified = attributesModifier.areRegistriesModified();
            boolean attributeConfigChanged = !Objects.equals(oldItemAttributes, configuration.getItemAttributes()) || !Objects.equals(oldEntityAttributes, configuration.getEntityAttributes());

            if (attributeConfigChanged || !previouslyModified) {
                remakeModifications();
            }
        }, executor).exceptionallyAsync(e -> {
            LOGGER.error("Failed to apply the configuration", e);
            updateConfiguration(null);
            return null;
        }, executor);
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
        return currentServer;
    }

    public long getLastAttributeReload() {
        return lastAttributeReload;
    }

    public void setCurrentServer(MinecraftServer currentServer) {
        this.currentServer = currentServer;
    }

    private static Settings loadSettings(CombatEdit combatEdit) {
        LOGGER.info("Loading settings...");
        var settings = combatEdit.loadSettings();
        try {
            settings.validate();
            LOGGER.info("Loaded settings file.");
            return settings;
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Settings file is invalid, using defaults", e);
            return Settings.loadDefault();
        }
    }

    private static Map<Identifier, BaseProfile> loadBaseProfiles(ResourceManager manager) {
        LOGGER.info("Loading base profiles...");
        var result = BaseProfile.find(manager);
        LOGGER.info("Found {} base profiles: {}", result.size(),
                result.keySet()
                        .stream()
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")));

        return result;
    }

    private static List<ProfileExtension> loadProfileExtensions(ResourceManager manager, Identifier baseProfileSelected) {
        LOGGER.info("Loading profile extensions...");
        var result = ProfileExtension.findForProfile(manager, baseProfileSelected);
        LOGGER.info("Found {} base profile extensions for {}", result.size(), baseProfileSelected.toString());

        return result;
    }

    public void revertModifications() {
        attributesModifier.revertModifications();
        LOGGER.info("Reverted modifications.");
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
        prioritizedConfigurationList.add(data.baseProfile().getConfiguration());

        configuration = new ConfigurationView(prioritizedConfigurationList.toArray(new Configuration[0]));
        LOGGER.info("Configuration updated.");
    }

    private void remakeModifications() {
        attributesModifier.makeModifications();
        updateAttributesToClients();

        oldItemAttributes = List.copyOf(configuration.getItemAttributes());
        oldEntityAttributes = List.copyOf(configuration.getEntityAttributes());
        lastAttributeReload = System.currentTimeMillis();
        LOGGER.info("Applied attribute modifications.");
    }

    private void updateAttributesToClients() {
        if (currentServer == null) {
            return;
        }

        for (var player : currentServer.getPlayerManager().getPlayerList()) {
            player.currentScreenHandler.updateToClient();
        }
    }

    public void registerProfileExtension(Identifier profileId, ProfileExtensionProvider extensionProvider) {
        this.registeredProfileExtensions.computeIfAbsent(profileId, key -> new ArrayList<>()).add(extensionProvider);
    }

    public static ServerConfigurationManager getInstance() {
        return INSTANCE;
    }
}
