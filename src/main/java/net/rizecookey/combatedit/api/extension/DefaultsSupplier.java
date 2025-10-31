package net.rizecookey.combatedit.api.extension;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface DefaultsSupplier {
    /**
     * Returns the item default supplier.
     *
     * @return the item defaults supplier
     */
    Items items();

    /**
     * Returns the entity default supplier.
     *
     * @return the entity defaults supplier
     */
    Entities entities();
    
    interface Items {
        /**
         * Provides the default components for an item.
         *
         * @param item the {@link Item} for which to get the components
         * @return a {@link DataComponentMap} of the default item components for the specified item
         */
        DataComponentMap getVanillaComponents(Item item);

        /**
         * Provides the default attribute modifiers for an item.
         *
         * @param item the {@link Item} for which to get the attribute modifiers
         * @return an {@link ItemAttributeModifiers} object containing the default attribute modifiers for the specified
         * item
         */
        ItemAttributeModifiers getVanillaAttributeModifiers(Item item);
    }

    interface Entities {
        /**
         * Provides the default entity attributes for a given entity type.
         *
         * @param entityType the {@link EntityType} for which to get the default entity attributes
         * @return an {@link AttributeSupplier} containing the default attributes for the specified entity type
         */
        AttributeSupplier getVanillaDefaultAttributes(EntityType<? extends LivingEntity> entityType);
    }
}
