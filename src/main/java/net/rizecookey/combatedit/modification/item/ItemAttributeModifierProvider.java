package net.rizecookey.combatedit.modification.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ItemAttributeModifierProvider {
    AttributeModifiersComponent getModifiers(Identifier id, Item item, AttributeModifiersComponent originalDefaults);
    boolean shouldModifyItem(Identifier id, Item item);
}
