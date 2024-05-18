package net.rizecookey.combatedit.mixins.item;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.rizecookey.combatedit.extension.ItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class ItemMixin implements ItemExtension {
    @Shadow private ComponentMap components;

    @Override
    public void combatEdit$setComponents(ComponentMap map) {
        this.components = map;
    }

    @Override
    public ComponentMap combatEdit$getComponents() {
        return components;
    }
}
