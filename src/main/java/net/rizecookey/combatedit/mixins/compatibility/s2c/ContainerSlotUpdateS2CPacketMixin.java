package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public abstract class ContainerSlotUpdateS2CPacketMixin implements AttributePatchable {
    @Shadow @Final @Mutable private ItemStack stack;

    @Override
    public void combatEdit$patchAttributes() {
        this.stack = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(this.stack);
    }
}
