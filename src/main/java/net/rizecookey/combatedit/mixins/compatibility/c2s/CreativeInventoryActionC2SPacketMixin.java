package net.rizecookey.combatedit.mixins.compatibility.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.CreativeInventoryActionC2SPacketExtension;
import net.rizecookey.combatedit.mixins.compatibility.ScreenHandlerAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryActionC2SPacket.class)
public abstract class CreativeInventoryActionC2SPacketMixin implements CreativeInventoryActionC2SPacketExtension {
    @Shadow @Final @Mutable private ItemStack stack;

    @Shadow @Final private short slot;
    @Unique
    private boolean hadPacketModification;

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("HEAD"))
    public void preApply(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo ci) {
        var unmodifiedStack = ConfigurationManager.getInstance().getAttributeHelper().reverseDisplayModifiers(this.stack);
        this.hadPacketModification = unmodifiedStack != this.stack;
        this.stack = unmodifiedStack;
    }

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("TAIL"))
    public void postApply(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (hadPacketModification || slot < 1 || slot > 45) {
            return;
        }

        ItemStack displayModified = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(this.stack);
        if (displayModified == stack || !(serverPlayPacketListener instanceof ServerPlayNetworkHandler networkHandler)) {
            return;
        }

        ScreenHandler screenHandler = networkHandler.player.playerScreenHandler;
        ScreenHandlerSyncHandler syncHandler = ((ScreenHandlerAccessor) screenHandler).getSyncHandler();
        if (syncHandler != null) {
            syncHandler.updateSlot(screenHandler, slot, displayModified);
        }
    }

    @Override
    public boolean combatEdit$hadPacketModification() {
        return hadPacketModification;
    }
}
