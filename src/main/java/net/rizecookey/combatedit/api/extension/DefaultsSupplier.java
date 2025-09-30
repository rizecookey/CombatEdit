package net.rizecookey.combatedit.api.extension;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;

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
         * @return a {@link ComponentMap} of the default item components for the specified item
         */
        ComponentMap getVanillaComponents(Item item);

        /**
         * Provides the default attribute modifiers for an item.
         *
         * @param item the {@link Item} for which to get the attribute modifiers
         * @return an {@link AttributeModifiersComponent} object containing the default attribute modifiers for the specified
         * item
         */
        AttributeModifiersComponent getVanillaAttributeModifiers(Item item);
    }

    interface Entities {
        /**
         * Provides the default entity attributes for a given entity type.
         *
         * @param entityType the {@link EntityType} for which to get the default entity attributes
         * @return an {@link DefaultAttributeContainer} containing the default attributes for the specified entity type
         */
        DefaultAttributeContainer getVanillaDefaultAttributes(EntityType<? extends LivingEntity> entityType);
    }
}
