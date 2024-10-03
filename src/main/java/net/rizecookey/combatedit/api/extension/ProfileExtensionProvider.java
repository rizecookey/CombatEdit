package net.rizecookey.combatedit.api.extension;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;

import java.util.function.Function;

/**
 * Instances of this interface provide a profile extension dynamically on reload.
 */
@FunctionalInterface
public interface ProfileExtensionProvider {
    /**
     * Provide a profile extension on configuration load.
     *
     * @param profile                The base profile that is being extended
     * @param originalItemDefaults   A function that, for a given Item, can provide its vanilla attribute component
     * @param originalEntityDefaults A function that, given an Entity Type, can provide its default, vanilla attributes
     * @return the profile extension
     */
    ProfileExtension provideExtension(BaseProfile profile, Function<Item, AttributeModifiersComponent> originalItemDefaults, Function<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalEntityDefaults);
}
