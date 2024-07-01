package net.rizecookey.combatedit.configuration;

import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.rizecookey.combatedit.CombatEdit.GSON;

public class Configuration {
    private List<ItemAttributes> itemAttributes;
    private List<EntityAttributes> entityAttributes;

    public Configuration(List<ItemAttributes> itemAttributes, List<EntityAttributes> entityAttributes) {
        this.itemAttributes = new ArrayList<>(itemAttributes);
        this.entityAttributes = new ArrayList<>(entityAttributes);
    }

    protected Configuration() {}

    public List<ItemAttributes> getItemAttributes() {
        return itemAttributes;
    }

    public List<EntityAttributes> getEntityAttributes() {
        return entityAttributes;
    }

    public void validate() throws InvalidConfigurationException {
        for (var itemAttributes : itemAttributes) {
            itemAttributes.validate();
        }

        for (var entityAttributes : entityAttributes) {
            entityAttributes.validate();
        }
    }

    public void save(Path path) throws IOException {
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        try (JsonWriter writer = GSON.newJsonWriter(Files.newBufferedWriter(path))) {
            GSON.toJson(this, Configuration.class, writer);
        }
    }

    public static Configuration load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, Configuration.class);
        }
    }

    public static Configuration loadDefault() {
        try (InputStream in = Configuration.class.getResourceAsStream("/config.json")) {
            if (in == null) {
                throw new IOException("Resource was not bundled correctly");
            }
            return GSON.fromJson(new InputStreamReader(in), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load default config", e);
        }
    }
}
