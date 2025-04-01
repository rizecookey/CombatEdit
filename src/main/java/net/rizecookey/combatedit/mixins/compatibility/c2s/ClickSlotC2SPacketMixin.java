package net.rizecookey.combatedit.mixins.compatibility.c2s;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.rizecookey.combatedit.extension.ServerCommonNetworkHandlerExtension;
import net.rizecookey.combatedit.extension.TrackedSlotExtension;
import net.rizecookey.combatedit.mixins.compatibility.ScreenHandlerAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickSlotC2SPacketMixin {
    @Shadow @Final private Int2ObjectMap<ItemStackHash> modifiedStacks;

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("HEAD"))
    public void preApply(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (!(serverPlayPacketListener instanceof ServerPlayNetworkHandler networkHandler)) {
            return;
        }

        var handlerExt = (ServerCommonNetworkHandlerExtension) serverPlayPacketListener;
        if (!handlerExt.combatEdit$isAttributePatchingEnabled()) {
            return;
        }

        ScreenHandlerAccessor handlerAccessor = (ScreenHandlerAccessor) networkHandler.player.currentScreenHandler;
        for (int key : modifiedStacks.keySet()) {
            TrackedSlot trackedSlot = handlerAccessor.getTrackedSlots().get(key);
            ((TrackedSlotExtension) trackedSlot).combatEdit$setCompareWithDisplayModified(true);
        }
        ((TrackedSlotExtension) handlerAccessor.getTrackedCursorSlot()).combatEdit$setCompareWithDisplayModified(true);
    }

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("RETURN"))
    public void postApply(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (!(serverPlayPacketListener instanceof ServerPlayNetworkHandler networkHandler)) {
            return;
        }

        var handlerExt = (ServerCommonNetworkHandlerExtension) networkHandler;
        if (!handlerExt.combatEdit$isAttributePatchingEnabled()) {
            return;
        }

        ScreenHandlerAccessor handlerAccessor = (ScreenHandlerAccessor) networkHandler.player.currentScreenHandler;
        for (int key : modifiedStacks.keySet()) {
            TrackedSlot trackedSlot = handlerAccessor.getTrackedSlots().get(key);
            ((TrackedSlotExtension) trackedSlot).combatEdit$setCompareWithDisplayModified(false);
        }
        ((TrackedSlotExtension) handlerAccessor.getTrackedCursorSlot()).combatEdit$setCompareWithDisplayModified(false);
    }
}
