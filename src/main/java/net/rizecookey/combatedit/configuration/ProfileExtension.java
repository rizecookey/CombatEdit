package net.rizecookey.combatedit.configuration;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.rizecookey.combatedit.CombatEdit.GSON;
import static net.rizecookey.combatedit.CombatEdit.LOGGER;

/**
 * Represents a profile extension.
 */
public class ProfileExtension {
    public static final String PROFILE_EXTENSIONS_PATH = "combatedit/profile_extensions";
    public static final String PROFILE_EXTENSIONS_ENDING = ".json";

    private int priority;
    @SerializedName("configuration_overrides") private MutableConfiguration parsedConfigurationOverrides;
    private transient Configuration configurationOverrides;

    public ProfileExtension(Configuration configurationOverrides, int priority) {
        this.configurationOverrides = configurationOverrides;
        this.priority = priority;
    }

    protected ProfileExtension() {}

    /**
     * Returns the priority of the profile extension. A higher priority
     * means a given configuration from this profile extension takes
     * precedence over configurations made by other profile extensions
     * with a lower priority value.
     *
     * @return the priority of this profile extension
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the configuration overrides made by this profile extension.
     * Null values for an option generally indicate that their value
     * should be based on whatever values the base profile or other profile
     * extensions with a lower priority use.
     *
     * @return the configuration overrides made by this profile extension
     */
    public Configuration getConfigurationOverrides() {
        if (configurationOverrides == null) {
            configurationOverrides = parsedConfigurationOverrides != null ? parsedConfigurationOverrides : new MutableConfiguration();
        }

        return configurationOverrides;
    }

    public void validate() throws InvalidConfigurationException {
        getConfigurationOverrides().validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileExtension that)) return false;
        return getPriority() == that.getPriority() && Objects.equals(getConfigurationOverrides(), that.getConfigurationOverrides());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPriority(), getConfigurationOverrides());
    }

    public static List<ProfileExtension> findForProfile(ResourceManager resourceManager, Identifier baseProfile) {
        var profileExtensions = resourceManager.findResources(
                PROFILE_EXTENSIONS_PATH + "/" + baseProfile.getNamespace() + "/" + baseProfile.getPath(),
                identifier -> identifier.getPath().endsWith(PROFILE_EXTENSIONS_ENDING)
        );

        List<ProfileExtension> extensions = new ArrayList<>();
        for (var entry : profileExtensions.entrySet()) {
            try (var reader = entry.getValue().getReader()) {
                var profile = GSON.fromJson(reader, ProfileExtension.class);
                profile.validate();
                extensions.add(profile);
            } catch (IOException e) {
                throw new RuntimeException("Could not read profile extension", e);
            } catch (InvalidConfigurationException e) {
                LOGGER.error("Profile extension {} is invalid", entry.getKey().toString(), e);
            }
        }

        return extensions;
    }
}
