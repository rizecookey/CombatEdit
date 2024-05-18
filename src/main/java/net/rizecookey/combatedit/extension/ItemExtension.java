package net.rizecookey.combatedit.extension;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;

public interface ItemExtension {
    void combatEdit$setComponents(ComponentMap map);
    ComponentMap combatEdit$getComponents();

    default void combatEdit$setAttributeModifiers(AttributeModifiersComponent component) {
        combatEdit$setComponents(ComponentMap.of(
                combatEdit$getComponents(),
                ComponentMap.builder()
                        .add(DataComponentTypes.ATTRIBUTE_MODIFIERS, component)
                        .build()
        ));
    }

    default AttributeModifiersComponent combatEdit$getAttributeModifiers() {
        return combatEdit$getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
    }
}
