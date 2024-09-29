package net.rizecookey.combatedit.configuration;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.exception.ResourceLoadFailureException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.rizecookey.combatedit.CombatEdit.GSON;
import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class BaseProfile {
    public static final String BASE_PROFILE_PATH = "combatedit/base_profiles";
    public static final String BASE_PROFILE_ENDING = ".json";

    private Text name;
    private Text description;
    @SerializedName("configuration") private MutableConfiguration parsedConfiguration;
    private transient Configuration configuration;

    public BaseProfile(Text name, Text description, Configuration configuration) {
        this.name = name;
        this.description = description;
        this.configuration = configuration;
    }

    protected BaseProfile() {}

    public Text getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            var config = parsedConfiguration != null ? parsedConfiguration : new MutableConfiguration();
            configuration = new ConfigurationView(config, MutableConfiguration.loadDefault());
        }

        return configuration;
    }

    public void validate() throws InvalidConfigurationException {
        if (name == null) {
            throw new InvalidConfigurationException("name is required");
        }
        if (description == null) {
            throw new InvalidConfigurationException("description is required");
        }

        getConfiguration().validate();
    }

    public static Map<Identifier, BaseProfile> find(ResourceManager manager) {
        var baseProfiles = new HashMap<Identifier, BaseProfile>();
        for (var entry : manager.findResources(BASE_PROFILE_PATH, id -> id.getPath().endsWith(BASE_PROFILE_ENDING)).entrySet()) {
            var shortId = getShortId(entry.getKey());
            try (var reader = new InputStreamReader(entry.getValue().getInputStream())) {
                var baseProfile = GSON.fromJson(reader, BaseProfile.class);
                baseProfile.validate();
                baseProfiles.put(shortId, baseProfile);
            } catch (IOException e) {
                throw new ResourceLoadFailureException(e);
            } catch (InvalidConfigurationException e) {
                LOGGER.error("Could not load base profile {}", shortId.toString(), e);
            }
        }

        return baseProfiles;
    }

    private static Identifier getShortId(Identifier longId) {
        var path = longId.getPath();
        if (!path.startsWith(BASE_PROFILE_PATH) || !path.endsWith(BASE_PROFILE_ENDING)) {
            throw new IllegalArgumentException("Not a valid base profile identifier");
        }

        return new Identifier(longId.getNamespace(), path.substring(BASE_PROFILE_PATH.length() + 1, path.length() - BASE_PROFILE_ENDING.length()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, getConfiguration());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseProfile profile)) {
            return false;
        }

        return Objects.equals(name, profile.name) && Objects.equals(description, profile.description)
                && Objects.equals(getConfiguration(), profile.getConfiguration());
    }

    public record Info(Identifier id, Text name, Text description) {}

    public enum IntegratedProfiles {
        VANILLA(new Info(new Identifier("combatedit", "vanilla"), Text.translatable("profile.combatedit.vanilla.name"), Text.translatable("profile.combatedit.vanilla.description"))),
        OLD_1_8_COMBAT(new Info(new Identifier("combatedit", "1_8_combat"), Text.translatable("profile.combatedit.1_8_combat.name"), Text.translatable("profile.combatedit.1_8_combat.description")));

        private final Info info;

        IntegratedProfiles(Info info) {
            this.info = info;
        }

        public Info getInfo() {
            return info;
        }
    }
}
