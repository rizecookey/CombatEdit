package net.rizecookey.combatedit.modification.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemModificationProvider {
    ItemAttributeModifiers getAttributeModifiers(Identifier id, Item item, ItemAttributeModifiers originalDefaults);
    boolean shouldModifyAttributes(Identifier id, Item item);

    DataComponentMap getComponents(Identifier id, Item item, DataComponentMap originalDefaults);
    boolean shouldModifyDefaultComponents(Identifier id, Item item);
}
