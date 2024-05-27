package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.rizecookey.combatedit.CombatEdit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public abstract class ContainerSlotUpdateS2CPacketMixin {
    @Shadow @Final @Mutable private ItemStack stack;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStack(CallbackInfo ci) {
        this.stack = CombatEdit.getInstance().getAttributeHelper().getDisplayModified(this.stack);
    }
}
