package net.rizecookey.combatedit.mixins.extension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item$Settings;getValidatedComponents(Lnet/minecraft/text/Text;Lnet/minecraft/util/Identifier;)Lnet/minecraft/component/ComponentMap;"))
    private ComponentMap replaceWithExchangeableComponentMap(ComponentMap original) {
        return new DynamicComponentMap(original);
    }
}
