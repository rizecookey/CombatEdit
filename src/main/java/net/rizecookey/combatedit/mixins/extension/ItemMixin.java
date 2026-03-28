package net.rizecookey.combatedit.mixins.extension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    // TODO need to update to 26.1
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;buildAndValidateComponents(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/Identifier;)Lnet/minecraft/core/component/DataComponentMap;"))
    private DataComponentMap replaceWithExchangeableComponentMap(DataComponentMap original) {
        return original instanceof DynamicDataComponentMap ? original : new DynamicDataComponentMap(original);
    }
}
