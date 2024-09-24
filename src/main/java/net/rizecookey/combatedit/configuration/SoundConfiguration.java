package net.rizecookey.combatedit.configuration;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SoundConfiguration {
    public static final List<SoundEvent> CONFIGURABLE_SOUNDS = List.of(
            SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
            SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
            SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
    );

    private Map<Identifier, Boolean> enabledSounds;

    public SoundConfiguration(Map<Identifier, Boolean> enabledSounds) {
        this.enabledSounds = new HashMap<>(enabledSounds);
    }

    protected SoundConfiguration() {}

    public Map<Identifier, Boolean> getEnabledSounds() {
        return enabledSounds;
    }

    public void validate(SoundConfiguration defaults) {
        if (enabledSounds == null) {
            enabledSounds = new HashMap<>();
        }

        for (var defaultEntry : defaults.getEnabledSounds().entrySet()) {
            if (!enabledSounds.containsKey(defaultEntry.getKey())) {
                enabledSounds.put(defaultEntry.getKey(), defaultEntry.getValue());
            }
        }

        for (SoundEvent event : CONFIGURABLE_SOUNDS) {
            if (!enabledSounds.containsKey(event.getId())) {
                enabledSounds.put(event.getId(), true);
            }
        }
    }

    @Override
    public int hashCode() {
        return enabledSounds.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SoundConfiguration other)) {
            return false;
        }

        return Objects.equals(enabledSounds, other.enabledSounds);
    }
}
