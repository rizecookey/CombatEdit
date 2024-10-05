package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.ComponentMap;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentMap.Builder.class)
public abstract class ComponentMap$BuilderMixin {
    @ModifyReturnValue(method = "build(Ljava/util/Map;)Lnet/minecraft/component/ComponentMap;", at = @At("RETURN"))
    private static ComponentMap replaceWithDynamic(ComponentMap original) {
        return new DynamicComponentMap(original);
    }
}
