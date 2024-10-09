package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import net.rizecookey.combatedit.extension.ItemStackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Mutable
    @Shadow @Final
    MergedComponentMap components;

    @Shadow public abstract Item getItem();

    @Override
    public void combatEdit$useOriginalComponentMapAsBase() {
        ComponentMap baseComponents = this.getItem().getComponents();
        if (!(baseComponents instanceof DynamicComponentMap dynamicComponents)) {
            return;
        }

        this.components = MergedComponentMap.create(dynamicComponents.getOriginal(), this.components.getChanges());
    }
}
