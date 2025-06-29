package net.rizecookey.combatedit.mixins.extension;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.rizecookey.combatedit.extension.AttributePatchable;
import net.rizecookey.combatedit.extension.ServerCommonNetworkHandlerExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements ServerCommonNetworkHandlerExtension {
    @Unique
    private boolean shouldPatchAttributes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void disableAttributePatchingByDefault(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData, CallbackInfo ci) {
        shouldPatchAttributes = false;
    }

    @Inject(method = "send", at = @At("HEAD"))
    private void potentiallyPatchPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        if (!((ServerCommonNetworkHandler) (Object) this instanceof ServerPlayNetworkHandler handler)) {
            return;
        }
        if (combatEdit$isAttributePatchingEnabled() && packet instanceof AttributePatchable patchable) {
            patchable.combatEdit$preSend(handler);
        }
    }

    @Override
    public boolean combatEdit$isAttributePatchingEnabled() {
        return shouldPatchAttributes;
    }

    @Override
    public void combatEdit$setAttributePatchingEnabled(boolean enabled) {
        this.shouldPatchAttributes = enabled;
    }
}
