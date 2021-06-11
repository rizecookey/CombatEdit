package net.rizecookey.combatedit.mixins.packetmodifiers.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.rizecookey.combatedit.utils.AttributeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickWindowC2SPacketMixin {
    @Shadow
    private ItemStack stack;

    @Inject(method = "read", at = @At("TAIL"))
    public void modifyItemStack(PacketByteBuf buf, CallbackInfo ci) {
        this.stack = AttributeHelper.reverseDisplayModifiers(this.stack);
    }
}
