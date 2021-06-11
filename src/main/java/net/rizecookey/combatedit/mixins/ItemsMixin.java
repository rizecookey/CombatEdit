package net.rizecookey.combatedit.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.extension.DamageToolItem;
import net.rizecookey.combatedit.item.WeaponStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Items.class)
public abstract class ItemsMixin {
    @Inject(method = "register(Lnet/minecraft/util/Identifier;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;", at = @At("HEAD"))
    private static void editItems(Identifier id, Item item, CallbackInfoReturnable<Item> cir) {
        if (item instanceof DamageToolItem && item instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) item;
            if (WeaponStats.changeAttackDamage(toolItem)) {
                ((DamageToolItem) item).setAttackDamage(WeaponStats.getAttackDamage(toolItem));
            }
        }
    }
}
