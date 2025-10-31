package net.rizecookey.combatedit.mixins.compatibility.c2s;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
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

@Mixin(ServerboundSetCreativeModeSlotPacket.class)
public abstract class CreativeInventoryActionC2SPacketMixin implements CreativeInventoryActionC2SPacketExtension {
    @Shadow @Final @Mutable private ItemStack itemStack;

    @Shadow @Final private short slotNum;
    @Unique
    private boolean hadPacketModification;

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", at = @At("HEAD"))
    public void preApply(ServerGamePacketListener serverPlayPacketListener, CallbackInfo ci) {
        var unmodifiedStack = ConfigurationManager.getInstance().getAttributeHelper().reverseDisplayModifiers(this.itemStack);
        this.hadPacketModification = unmodifiedStack != this.itemStack;
        this.itemStack = unmodifiedStack;
    }

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", at = @At("TAIL"))
    public void postApply(ServerGamePacketListener serverPlayPacketListener, CallbackInfo ci) {
        if (hadPacketModification || slotNum < 1 || slotNum > 45) {
            return;
        }

        ItemStack displayModified = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(this.itemStack);
        if (displayModified == itemStack || !(serverPlayPacketListener instanceof ServerGamePacketListenerImpl networkHandler)) {
            return;
        }

        AbstractContainerMenu screenHandler = networkHandler.player.inventoryMenu;
        ContainerSynchronizer syncHandler = ((ScreenHandlerAccessor) screenHandler).getSynchronizer();
        if (syncHandler != null) {
            syncHandler.sendSlotChange(screenHandler, slotNum, displayModified);
        }
    }

    @Override
    public boolean combatEdit$hadPacketModification() {
        return hadPacketModification;
    }
}
