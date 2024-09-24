package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.rizecookey.combatedit.extension.AttributePatchReversible;
import net.rizecookey.combatedit.extension.ServerCommonNetworkHandlerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void potentiallyReversePatches(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (!(listener instanceof ServerCommonNetworkHandlerExtension extension) || !extension.combatEdit$isAttributePatchingEnabled()) {
            return;
        }

        if (packet instanceof AttributePatchReversible reversible) {
            reversible.combatEdit$reverseAttributePatches();
        }
    }
}
