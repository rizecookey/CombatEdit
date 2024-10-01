package net.rizecookey.combatedit.configuration.representation;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.rizecookey.combatedit.CombatEdit.GSON;

public class MutableConfiguration implements Configuration {
    public static final int CURRENT_VERSION = 1;

    private int configurationVersion;
    private List<ItemAttributes> itemAttributes;
    private List<EntityAttributes> entityAttributes;
    private Map<Identifier, Boolean> enabledSounds;
    private MiscOptions miscOptions;

    public MutableConfiguration(List<ItemAttributes> itemAttributes, List<EntityAttributes> entityAttributes, Map<Identifier, Boolean> enabledSounds, MiscOptions miscOptions) {
        this.configurationVersion = CURRENT_VERSION;
        this.itemAttributes = itemAttributes != null ? new ArrayList<>(itemAttributes) : new ArrayList<>();
        this.entityAttributes = entityAttributes != null ? new ArrayList<>(entityAttributes) : new ArrayList<>();
        this.enabledSounds = enabledSounds;
        this.miscOptions = miscOptions;
    }

    public MutableConfiguration() {}

    public int getConfigurationVersion() {
        if (configurationVersion == 0) {
            configurationVersion = 1;
        }

        return configurationVersion;
    }

    @Override
    public List<ItemAttributes> getItemAttributes() {
        if (itemAttributes == null) {
            itemAttributes = new ArrayList<>();
        }
        return itemAttributes;
    }

    @Override
    public List<EntityAttributes> getEntityAttributes() {
        if (entityAttributes == null) {
            entityAttributes = new ArrayList<>();
        }
        return entityAttributes;
    }

    @Override
    public Optional<Boolean> isSoundEnabled(Identifier soundIdentifier) {
        if (enabledSounds == null) {
            enabledSounds = new HashMap<>();
        }

        return Optional.ofNullable(enabledSounds.get(soundIdentifier));
    }

    @Override
    public MiscOptions getMiscOptions() {
        if (miscOptions == null) {
            miscOptions = new MiscOptions();
        }
        return miscOptions;
    }

    public void setSoundEnabled(Identifier soundIdentifier, @Nullable Boolean enabled) {
        if (enabled == null) {
            enabledSounds.remove(soundIdentifier);
            return;
        }

        enabledSounds.put(soundIdentifier, enabled);
    }

    public MutableConfiguration copy() {
        return new MutableConfiguration(
                itemAttributes.stream().map(ItemAttributes::copy).toList(),
                entityAttributes.stream().map(EntityAttributes::copy).toList(),
                Map.copyOf(enabledSounds),
                miscOptions.copy()
        );
    }

    public void validate() throws InvalidConfigurationException {
        if (configurationVersion > CURRENT_VERSION) {
            throw new InvalidConfigurationException("Configuration claims to be of a higher version than the current one");
        }

        for (var attr : getItemAttributes()) {
            attr.validate();
        }

        for (var attr : getEntityAttributes()) {
            attr.validate();
        }
    }

    public static JsonObject migrateToNewerVersion(JsonObject old, int version) {
        JsonObject copy = old.deepCopy();
        if (version < 1) {
            copy.addProperty("configuration_version", 1);
        }
        return copy;
    }

    private static JsonObject loadDefaultJson() throws IOException {
        try (InputStream in = MutableConfiguration.class.getResourceAsStream("/default_config.json")) {
            if (in == null) {
                throw new IOException("Resource was not bundled correctly");
            }
            return GSON.fromJson(new InputStreamReader(in), JsonObject.class);
        }
    }

    public static MutableConfiguration loadDefault() {
        try {
            return GSON.fromJson(loadDefaultJson(), MutableConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load default settings file", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutableConfiguration that)) return false;
        return Objects.equals(getItemAttributes(), that.getItemAttributes()) && Objects.equals(getEntityAttributes(), that.getEntityAttributes()) && Objects.equals(enabledSounds, that.enabledSounds) && Objects.equals(getMiscOptions(), that.getMiscOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemAttributes(), getEntityAttributes(), enabledSounds, getMiscOptions());
    }

    public static class MiscOptions implements Configuration.MiscOptions {
        @SerializedName("enable_1_8_knockback") private Boolean enable1_8Knockback;
        private Boolean disableSweepingWithoutEnchantment;

        public MiscOptions(Boolean enable1_8Knockback, Boolean disableSweepingWithoutEnchantment) {
            this.enable1_8Knockback = enable1_8Knockback;
            this.disableSweepingWithoutEnchantment = disableSweepingWithoutEnchantment;
        }

        protected MiscOptions() {}

        @Override
        public Optional<Boolean> is1_8KnockbackEnabled() {
            return Optional.ofNullable(enable1_8Knockback);
        }

        @Override
        public Optional<Boolean> isSweepingWithoutEnchantmentDisabled() {
            return Optional.ofNullable(disableSweepingWithoutEnchantment);
        }

        public void set1_8KnockbackEnabled(@Nullable Boolean enable1_8Knockback) {
            this.enable1_8Knockback = enable1_8Knockback;
        }

        public void setSweepingWithoutEnchantmentDisabled(@Nullable Boolean disableSweepingWithoutEnchantment) {
            this.disableSweepingWithoutEnchantment = disableSweepingWithoutEnchantment;
        }

        public MiscOptions copy() {
            return new MiscOptions(enable1_8Knockback, disableSweepingWithoutEnchantment);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MiscOptions that)) return false;
            return Objects.equals(enable1_8Knockback, that.enable1_8Knockback) && Objects.equals(disableSweepingWithoutEnchantment, that.disableSweepingWithoutEnchantment);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enable1_8Knockback, disableSweepingWithoutEnchantment);
        }
    }
}
