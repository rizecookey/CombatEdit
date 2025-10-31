package net.rizecookey.combatedit.mixins.compatibility.c2s;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.RemoteSlot;
import net.rizecookey.combatedit.extension.ServerCommonPacketListenerImplExtension;
import net.rizecookey.combatedit.mixins.compatibility.AbstractContainerMenuAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundContainerClickPacket.class)
public abstract class ServerboundContainerClickPacketMixin {
    @Shadow @Final private Int2ObjectMap<HashedStack> changedSlots;

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", at = @At("HEAD"))
    public void preApply(ServerGamePacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (!(serverPlayPacketListener instanceof ServerGamePacketListenerImpl networkHandler)) {
            return;
        }

        var handlerExt = (ServerCommonPacketListenerImplExtension) serverPlayPacketListener;
        if (!handlerExt.combatEdit$isAttributePatchingEnabled()) {
            return;
        }

        AbstractContainerMenuAccessor handlerAccessor = (AbstractContainerMenuAccessor) networkHandler.player.containerMenu;
        for (int key : changedSlots.keySet()) {
            RemoteSlot trackedSlot = handlerAccessor.getRemoteSlots().get(key);
            trackedSlot.combatEdit$setCompareWithDisplayModified(true);
        }
        handlerAccessor.getRemoteCarried().combatEdit$setCompareWithDisplayModified(true);
    }

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", at = @At("RETURN"))
    public void postApply(ServerGamePacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (!(serverPlayPacketListener instanceof ServerGamePacketListenerImpl networkHandler)) {
            return;
        }

        var handlerExt = (ServerCommonPacketListenerImplExtension) networkHandler;
        if (!handlerExt.combatEdit$isAttributePatchingEnabled()) {
            return;
        }

        AbstractContainerMenuAccessor handlerAccessor = (AbstractContainerMenuAccessor) networkHandler.player.containerMenu;
        for (int key : changedSlots.keySet()) {
            RemoteSlot trackedSlot = handlerAccessor.getRemoteSlots().get(key);
            trackedSlot.combatEdit$setCompareWithDisplayModified(false);
        }
        handlerAccessor.getRemoteCarried().combatEdit$setCompareWithDisplayModified(false);
    }
}
