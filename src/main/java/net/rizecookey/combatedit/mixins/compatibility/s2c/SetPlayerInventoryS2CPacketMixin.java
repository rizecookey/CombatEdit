package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SetPlayerInventoryS2CPacket.class)
public abstract class SetPlayerInventoryS2CPacketMixin implements AttributePatchable {
    @Mutable
    @Shadow @Final private ItemStack contents;

    @Override
    public void combatEdit$preSend(ServerPlayNetworkHandler networkHandler) {
        this.contents = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(this.contents);
    }
}
