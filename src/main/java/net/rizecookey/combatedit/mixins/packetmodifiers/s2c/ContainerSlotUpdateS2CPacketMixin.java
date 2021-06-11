package net.rizecookey.combatedit.mixins.packetmodifiers.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.rizecookey.combatedit.utils.AttributeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public abstract class ContainerSlotUpdateS2CPacketMixin {
    @Shadow
    private ItemStack stack;

    @Inject(method = "<init>(IILnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    public void modifyItemStack(int syncId, int slot, ItemStack stack, CallbackInfo ci) {
        this.stack = AttributeHelper.changeDisplayModifiers(this.stack);
    }
}
