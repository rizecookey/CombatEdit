package net.rizecookey.combatedit.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface ItemAttributeModifierProvider {
    AttributeModifiersComponent getModifiers(Identifier id, Item item);
    boolean shouldModifyItem(Identifier id, Item item);
}
