package net.rizecookey.combatedit.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.rizecookey.combatedit.utils.AttributeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class PacketModifierMixin {
    //Server to Client packet modification
    @Mixin(InventoryS2CPacket.class)
    public static class InventoryS2CMixin {
        @Shadow private List<ItemStack> contents;

        @Inject(method = "<init>(ILnet/minecraft/util/collection/DefaultedList;)V", at = @At("TAIL"))
        public void modifyItemStacks(int guiId, DefaultedList<ItemStack> slotStackList, CallbackInfo ci) {
            List<ItemStack> modifiedSlotStackList = DefaultedList.ofSize(this.contents.size(), ItemStack.EMPTY);
            for (ItemStack itemStack : this.contents) {
                modifiedSlotStackList.set(this.contents.indexOf(itemStack), AttributeHelper.changeDisplayModifiers(itemStack));
            }
            this.contents = modifiedSlotStackList;
        }
    }
    @Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
    public static class ContainerSlotUpdateS2CPacketMixin {
        @Shadow private ItemStack stack;

        @Inject(method = "<init>(IILnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
        public void modifyItemStack(int syncId, int slot, ItemStack stack, CallbackInfo ci) {
            this.stack = AttributeHelper.changeDisplayModifiers(this.stack);
        }
    }
    //Client to Server packet modification
    @Mixin(ClickWindowC2SPacket.class)
    public static class ClickWindowC2SPacketMixin {
        @Shadow private ItemStack stack;

        @Inject(method = "read", at = @At("TAIL"))
        public void modifyItemStack(PacketByteBuf buf, CallbackInfo ci) {
            this.stack = AttributeHelper.reverseDisplayModifiers(this.stack);
        }
    }
    @Mixin(CreativeInventoryActionC2SPacket.class)
    public static class CreativeInventoryActionC2SPacketMixin {
        @Shadow private ItemStack stack;

        @Inject(method = "read", at = @At("TAIL"))
        public void modifyItemStack(PacketByteBuf buf, CallbackInfo ci) {
            this.stack = AttributeHelper.reverseDisplayModifiers(this.stack);
        }
    }
}
