package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import net.rizecookey.combatedit.extension.ItemStackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Mutable
    @Shadow @Final
    MergedComponentMap components;

    @Shadow public abstract Item getItem();

    @SuppressWarnings("unchecked")
    @Override
    public void combatEdit$useOriginalComponentMapAsBase() {
        ComponentMap baseComponents = this.getItem().getComponents();
        if (!(baseComponents instanceof DynamicComponentMap dynamicComponents)) {
            return;
        }

        this.components = MergedComponentMap.create(dynamicComponents.getOriginal(), this.components.getChanges());

        for (ComponentType<?> componentType : Registries.DATA_COMPONENT_TYPE) {
            if (components.hasChanged(componentType)) {
                continue;
            }

            Object originalValue = dynamicComponents.getOriginal().get(componentType);
            Object exchangeableValue = dynamicComponents.getExchangeable().get(componentType);
            if (Objects.equals(originalValue, exchangeableValue)) {
                continue;
            }

            components.set((ComponentType<Object>) componentType, exchangeableValue);
        }
    }
}
