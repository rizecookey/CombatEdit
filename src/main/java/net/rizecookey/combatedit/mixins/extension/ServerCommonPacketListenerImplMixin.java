package net.rizecookey.combatedit.mixins.extension;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.rizecookey.combatedit.extension.AttributePatchable;
import net.rizecookey.combatedit.extension.ServerCommonPacketListenerImplExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements ServerCommonPacketListenerImplExtension {
    @Unique
    private boolean shouldPatchAttributes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void disableAttributePatchingByDefault(MinecraftServer server, Connection connection, CommonListenerCookie clientData, CallbackInfo ci) {
        shouldPatchAttributes = false;
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"))
    private void potentiallyPatchPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        if (!((ServerCommonPacketListenerImpl) (Object) this instanceof ServerGamePacketListenerImpl handler)) {
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
