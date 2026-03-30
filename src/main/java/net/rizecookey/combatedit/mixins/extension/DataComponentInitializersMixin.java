package net.rizecookey.combatedit.mixins.extension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponentInitializers.class)
public abstract class DataComponentInitializersMixin {
    @ModifyExpressionValue(method = "lambda$createInitializerForRegistry$0", at = @At(value = "INVOKE", target = "net/minecraft/core/component/DataComponentMap$Builder.build()Lnet/minecraft/core/component/DataComponentMap;"))
    private static <T> DataComponentMap replaceWithExchangeableComponentMap(DataComponentMap original, @Local(argsOnly = true, name = "elementKey") ResourceKey<? extends Registry<T>> elementKey) {
        if (!elementKey.registry().equals(Registries.ITEM.identifier())) {
            return original;
        }
        return original instanceof DynamicDataComponentMap ? original : new DynamicDataComponentMap(original);
    }

}
