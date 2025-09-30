package net.rizecookey.combatedit.modification.item;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ItemModificationProvider {
    AttributeModifiersComponent getAttributeModifiers(Identifier id, Item item, AttributeModifiersComponent originalDefaults);
    boolean shouldModifyAttributes(Identifier id, Item item);

    ComponentMap getComponents(Identifier id, Item item, ComponentMap originalDefaults);
    boolean shouldModifyDefaultComponents(Identifier id, Item item);
}
