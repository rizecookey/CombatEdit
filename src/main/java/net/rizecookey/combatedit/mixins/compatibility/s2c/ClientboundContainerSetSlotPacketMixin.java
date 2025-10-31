package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundContainerSetSlotPacket.class)
public abstract class ClientboundContainerSetSlotPacketMixin implements AttributePatchable {
    @Shadow @Final @Mutable private ItemStack itemStack;

    @Override
    public void combatEdit$preSend(ServerGamePacketListenerImpl networkHandler) {
        this.itemStack = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(this.itemStack);
    }
}
