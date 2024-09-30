package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.rizecookey.combatedit.extension.MutableDefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;build()Lnet/minecraft/entity/attribute/DefaultAttributeContainer;"))
    private static DefaultAttributeContainer replaceWithMutable(DefaultAttributeContainer value) {
        return MutableDefaultAttributeContainer.copyOf(value);
    }
}
