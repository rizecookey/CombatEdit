package net.rizecookey.combatedit.configuration;

import com.google.gson.annotations.SerializedName;
import net.minecraft.text.Text;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ConfigurationView;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.util.Objects;

public class BaseProfile {
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
}
