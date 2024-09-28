package net.rizecookey.combatedit.configuration;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.rizecookey.combatedit.CombatEdit.GSON;

public class Settings {
    private int settingsVersion = 1;
    private Identifier selectedBaseProfile;
    private MutableConfiguration configurationOverrides;

    public Settings(int settingsVersion, Identifier selectedBaseProfile, MutableConfiguration configurationOverrides) {
        this.settingsVersion = settingsVersion;
        this.selectedBaseProfile = selectedBaseProfile;
        this.configurationOverrides = configurationOverrides;
    }

    protected Settings() {}

    public int getSettingsVersion() {
        return settingsVersion;
    }

    public Identifier getSelectedBaseProfile() {
        return selectedBaseProfile;
    }

    public MutableConfiguration getConfigurationOverrides() {
        if (configurationOverrides == null) {
            configurationOverrides = new MutableConfiguration();
        }

        return configurationOverrides;
    }

    public void setSettingsVersion(int settingsVersion) {
        this.settingsVersion = settingsVersion;
    }

    public void setSelectedBaseProfile(Identifier selectedBaseProfile) {
        this.selectedBaseProfile = selectedBaseProfile;
    }

    public void setConfigurationOverrides(MutableConfiguration mutableConfigurationOverrides) {
        this.configurationOverrides = mutableConfigurationOverrides;
    }

    public void validate() throws InvalidConfigurationException {
        if (getSelectedBaseProfile() == null) {
            throw new InvalidConfigurationException("No selected profile specified");
        }

        getConfigurationOverrides().validate();
    }

    public void save(Path path) throws IOException {
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        try (JsonWriter writer = GSON.newJsonWriter(Files.newBufferedWriter(path))) {
            GSON.toJson(this, Settings.class, writer);
        }
    }

    public static Settings load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, Settings.class);
        }
    }

    private static JsonObject loadDefaultJson() throws IOException {
        try (InputStream in = MutableConfiguration.class.getResourceAsStream("/settings.json")) {
            if (in == null) {
                throw new IOException("Resource was not bundled correctly");
            }
            return GSON.fromJson(new InputStreamReader(in), JsonObject.class);
        }
    }

    public static Settings loadDefault() {
        try {
            return GSON.fromJson(loadDefaultJson(), Settings.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load default settings file", e);
        }
    }
}
