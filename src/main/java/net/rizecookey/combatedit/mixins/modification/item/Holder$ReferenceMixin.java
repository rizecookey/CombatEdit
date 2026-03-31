package net.rizecookey.combatedit.mixins.modification.item;

import com.llamalad7.mixinextras.expression.Definition;import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;import net.minecraft.core.component.DataComponentMap;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.core.Holder$Reference")
public abstract class Holder$ReferenceMixin {
    @Shadow
    private @Nullable DataComponentMap components;

    @Definition(id = "components", local = @Local(type = DataComponentMap.class, name = "components", argsOnly = true))
    @Expression("components")
    @ModifyExpressionValue(method = "bindComponents", at = @At("MIXINEXTRAS:EXPRESSION"), order = 1100)
    public DataComponentMap reuseSameMap(DataComponentMap original) {
        if (!(components instanceof DynamicDataComponentMap dynamicComponents)) {
            return original;
        }

        dynamicComponents.setOriginal(original);
        return dynamicComponents;
    }
}
