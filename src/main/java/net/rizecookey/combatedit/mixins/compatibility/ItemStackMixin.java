package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
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
    PatchedDataComponentMap components;

    @Shadow public abstract Item getItem();

    @SuppressWarnings("unchecked")
    @Override
    public void combatEdit$useOriginalComponentMapAsBase() {
        DataComponentMap baseComponents = this.getItem().components();
        if (!(baseComponents instanceof DynamicDataComponentMap dynamicComponents)) {
            return;
        }

        this.components = PatchedDataComponentMap.fromPatch(dynamicComponents.getOriginal(), this.components.asPatch());

        for (DataComponentType<?> componentType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
            if (components.hasNonDefault(componentType)) {
                continue;
            }

            Object originalValue = dynamicComponents.getOriginal().get(componentType);
            Object exchangeableValue = dynamicComponents.getModified().get(componentType);
            if (Objects.equals(originalValue, exchangeableValue)) {
                continue;
            }

            components.set((DataComponentType<Object>) componentType, exchangeableValue);
        }
    }
}
