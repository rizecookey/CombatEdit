package net.rizecookey.combatedit.mixins.compatibility.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.rizecookey.combatedit.CombatEdit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryActionC2SPacket.class)
public abstract class CreativeInventoryActionC2SPacketMixin {
    @Shadow @Final @Mutable private ItemStack stack;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStack(CallbackInfo ci) {
        this.stack = CombatEdit.getInstance().getAttributeHelper().reverseDisplayModifiers(this.stack);
    }
}
