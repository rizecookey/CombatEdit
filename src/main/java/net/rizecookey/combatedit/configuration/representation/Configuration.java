package net.rizecookey.combatedit.configuration.representation;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a configuration or part of a configuration for CombatEdit.
 * A configuration can choose to not set an option if the value should
 * instead be based on other configurations in the configuration hierarchy,
 * where the first configuration that sets the option will be used to provide
 * the value. Unset values are generally indicated by null or empty Optional
 * values.
 */
public interface Configuration {
    List<SoundEvent> CONFIGURABLE_SOUNDS = List.of(
            SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
            SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
            SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
    );

    /**
     * Returns a list of additional attribute modifiers and overrides for items.
     *
     * @return a list of all item attribute overrides of this configuration
     */
    @NotNull List<ItemAttributes> getItemAttributes();

    /**
     * Returns a list of all attribute overrides for entities.
     *
     * @return a list of all entity attribute overrides of this configuration
     */
    @NotNull List<EntityAttributes> getEntityAttributes();

    /**
     * Returns whether a specific attack sound should be played or not.
     * An empty optional indicates that this configuration does not set
     * a value for this option.
     *
     * @param soundIdentifier the identifier for the attack sound
     * @return whether an attack sound should be enabled, if this configuration
     * makes a decision about the specified sound, or an empty optional
     */
    Optional<Boolean> isSoundEnabled(Identifier soundIdentifier);

    /**
     * Returns a map that, for each sound identifier for which the option is set,
     * provides a boolean representing whether the sound should be enabled or not.
     *
     * @return the sound enablement map
     */
    @NotNull Map<Identifier, Boolean> getSoundMap();

    /**
     * Returns the miscellaneous options set by this configuration
     * @return the miscellaneous options for this configuration
     */
    @NotNull MiscOptions getMiscOptions();

    void validate() throws InvalidConfigurationException;

    /**
     * Represents miscellaneous options for the mod.
     */
    interface MiscOptions {
        /**
         * Returns whether 1.8-style knockback should be enabled, if a value is set.
         *
         * @return whether 1.8-style knockback should be enabled, or an empty Optional
         * if no value is set
         */
        Optional<Boolean> is1_8KnockbackEnabled();

        /**
         * Returns whether sweeping attacks should be enabled even if the attacker's item
         * does not have the sweeping enchantment.
         *
         * @return whether sweeping attacks without the sweeping enchantment should be enabled,
         * or an empty optional if no value is set
         */
        Optional<Boolean> isSweepingWithoutEnchantmentDisabled();
    }
}
