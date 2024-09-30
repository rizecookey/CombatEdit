package net.rizecookey.combatedit.mixins.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.rizecookey.combatedit.extension.ExchangeableComponentMap;
import net.rizecookey.combatedit.extension.ItemExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin implements ItemExtension {
    @Shadow @Final private ComponentMap components;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item$Settings;getValidatedComponents()Lnet/minecraft/component/ComponentMap;"))
    private ComponentMap replaceWithExchangeableComponentMap(ComponentMap original) {
        return new ExchangeableComponentMap(original);
    }

    @Unique
    public ExchangeableComponentMap exchangeableMap() {
        return (ExchangeableComponentMap) this.components;
    }

    @Override
    public void combatEdit$setComponents(ComponentMap map) {
        exchangeableMap().exchangeBase(map);
    }

    @Override
    public ComponentMap combatEdit$getComponents() {
        return components;
    }
}
