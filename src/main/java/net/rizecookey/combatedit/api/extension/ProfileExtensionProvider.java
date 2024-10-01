package net.rizecookey.combatedit.api.extension;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;

import java.util.function.Function;

@FunctionalInterface
public interface ProfileExtensionProvider {
    ProfileExtension provideExtension(BaseProfile profile, Function<Item, AttributeModifiersComponent> originalItemDefaults, Function<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalEntityDefaults);
}
