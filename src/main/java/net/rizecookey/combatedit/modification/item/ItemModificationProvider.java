package net.rizecookey.combatedit.modification.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemModificationProvider {
    ItemAttributeModifiers getAttributeModifiers(ResourceLocation id, Item item, ItemAttributeModifiers originalDefaults);
    boolean shouldModifyAttributes(ResourceLocation id, Item item);

    DataComponentMap getComponents(ResourceLocation id, Item item, DataComponentMap originalDefaults);
    boolean shouldModifyDefaultComponents(ResourceLocation id, Item item);
}
