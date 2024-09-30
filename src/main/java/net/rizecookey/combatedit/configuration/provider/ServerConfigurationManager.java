package net.rizecookey.combatedit.configuration.provider;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.rizecookey.combatedit.AttributesModifier;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ServerConfigurationManager implements SimpleResourceReloadListener<Pair<Settings, Map<Identifier, BaseProfile>>> {
    private static ServerConfigurationManager INSTANCE;

    private final CombatEdit combatEdit;
    private MinecraftServer currentServer;

    private Configuration configuration;
    private final ItemStackAttributeHelper attributeHelper;
    private final AttributesModifier attributesModifier;

    private List<EntityAttributes> oldEntityAttributes;
    private List<ItemAttributes> oldItemAttributes;
    private long lastAttributeReload = Long.MIN_VALUE;

    public ServerConfigurationManager(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.attributesModifier = new AttributesModifier(this);
        this.attributeHelper = new ItemStackAttributeHelper(this);

        INSTANCE = this;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("combatedit", "server_configuration_provider");
    }

    @Override
    public CompletableFuture<Pair<Settings, Map<Identifier, BaseProfile>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        var settingsLoader = CompletableFuture.supplyAsync(() -> loadSettings(combatEdit), executor);
        var baseProfileLoader = CompletableFuture.supplyAsync(() -> loadBaseProfiles(manager), executor);

        return settingsLoader.thenCombineAsync(baseProfileLoader, Pair::new, executor)
                .exceptionally(e -> {
                    LOGGER.error("Configuration loading failed", e);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> apply(Pair<Settings, Map<Identifier, BaseProfile>> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            if (data == null) {
                return;
            }
            updateConfiguration(data.getLeft(), data.getRight());

            boolean previouslyModified = attributesModifier.areRegistriesModified();
            boolean attributeConfigChanged = !Objects.equals(oldItemAttributes, configuration.getItemAttributes()) || !Objects.equals(oldEntityAttributes, configuration.getEntityAttributes());

            if (attributeConfigChanged || !previouslyModified) {
                remakeModifications();
            }
        }, executor).exceptionally(e -> {
            LOGGER.error("Failed to apply the configuration", e);
            return null;
        });
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

    public void revertModifications() {
        attributesModifier.revertModifications();
        LOGGER.info("Reverted modifications.");
    }

    private void updateConfiguration(Settings settings, Map<Identifier, BaseProfile> baseProfiles) {
        BaseProfile selectedProfile = baseProfiles.get(settings.getSelectedBaseProfile());

        if (selectedProfile != null) {
            configuration = new ConfigurationView(settings.getConfigurationOverrides(), selectedProfile.getConfiguration());
        } else {
            LOGGER.error("No base profile with id {} found! Using default configuration.", settings.getSelectedBaseProfile());
            configuration = new ConfigurationView(settings.getConfigurationOverrides(), MutableConfiguration.loadDefault());
        }
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

    public static ServerConfigurationManager getInstance() {
        return INSTANCE;
    }
}
