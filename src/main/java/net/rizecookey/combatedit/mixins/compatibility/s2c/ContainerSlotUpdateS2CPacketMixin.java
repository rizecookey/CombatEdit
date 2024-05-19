package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.rizecookey.combatedit.CombatEdit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public abstract class ContainerSlotUpdateS2CPacketMixin {
    @Shadow @Final @Mutable private ItemStack stack;

    @Unique
    private static CombatEdit COMBAT_EDIT;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStack(CallbackInfo ci) {
        if (COMBAT_EDIT == null) {
            COMBAT_EDIT = CombatEdit.getInstance();
        }

        this.stack = COMBAT_EDIT.getAttributeHelper().getDisplayModified(this.stack);
    }
}
