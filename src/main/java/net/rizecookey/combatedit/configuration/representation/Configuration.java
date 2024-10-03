package net.rizecookey.combatedit.configuration.representation;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Configuration {
    List<SoundEvent> CONFIGURABLE_SOUNDS = List.of(
            SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
            SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
            SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
    );

    List<ItemAttributes> getItemAttributes();

    List<EntityAttributes> getEntityAttributes();

    Optional<Boolean> isSoundEnabled(Identifier soundIdentifier);

    Map<Identifier, Boolean> getSoundMap();

    MiscOptions getMiscOptions();

    void validate() throws InvalidConfigurationException;

    interface MiscOptions {
        Optional<Boolean> is1_8KnockbackEnabled();

        Optional<Boolean> isSweepingWithoutEnchantmentDisabled();
    }
}
