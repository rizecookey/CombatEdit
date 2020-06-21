package net.rizecookey.combatedit.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.item.AxeItem;
import net.rizecookey.combatedit.item.PickaxeItem;
import net.rizecookey.combatedit.item.SwordItem;
import net.rizecookey.combatedit.item.ShovelItem;
import net.rizecookey.combatedit.item.HoeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Items.class)
public abstract class ItemsMixin {
    @Shadow
    private static Item register(Identifier id, Item item) {
        return null;
    }

    @Inject(method = "register(Ljava/lang/String;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;", at = @At(value = "HEAD"), cancellable = true)
    private static void handleRegisterItem(String id, Item item, CallbackInfoReturnable<Item> cir) {
        if (item instanceof ToolItem) {
            Item newItem = item;
            if (item instanceof net.minecraft.item.AxeItem) {
                newItem = new AxeItem(((net.minecraft.item.AxeItem) item).getMaterial(), 3.0F, 0f, (new Item.Settings()).group(ItemGroup.TOOLS));
            } else if (item instanceof net.minecraft.item.SwordItem) {
                newItem = new SwordItem(((net.minecraft.item.SwordItem) item).getMaterial(), 4, 0.0f, (new Item.Settings()).group(ItemGroup.COMBAT));
            } else if (item instanceof net.minecraft.item.PickaxeItem) {
                newItem = new PickaxeItem(((net.minecraft.item.PickaxeItem) item).getMaterial(), 2, 0f, (new Item.Settings()).group(ItemGroup.TOOLS));
            } else if (item instanceof net.minecraft.item.ShovelItem) {
                newItem = new ShovelItem(((net.minecraft.item.ShovelItem) item).getMaterial(), 1.0f, 0f, (new Item.Settings()).group(ItemGroup.TOOLS));
            } else if (item instanceof net.minecraft.item.HoeItem) {
                newItem = new HoeItem(((net.minecraft.item.HoeItem) item).getMaterial(), 0f, (new Item.Settings()).group(ItemGroup.TOOLS));
            }
            Item registeredItem = register(new Identifier(id), newItem);
            cir.cancel();
            cir.setReturnValue(registeredItem);
        }
    }
}
