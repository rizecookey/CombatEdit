package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizecookey.combatedit.extension.CreativeInventoryActionC2SPacketExtension;
import net.rizecookey.combatedit.extension.ServerCommonNetworkHandlerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void decideOnPatching(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        ((ServerCommonNetworkHandlerExtension) this).combatEdit$setAttributePatchingEnabled(shouldPatchAttributes());
    }

    @Unique
    private boolean shouldPatchAttributes() {
        return server.isDedicated() || !server.isHost(getPlayer().getGameProfile());
    }

    @ModifyArg(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setPreviousTrackedSlot(ILnet/minecraft/item/ItemStack;)V"))
    private ItemStack forceUpdateIfUnmodifiedItem(ItemStack previous, @Local(argsOnly = true) CreativeInventoryActionC2SPacket packet) {
        if (((CreativeInventoryActionC2SPacketExtension) (Object) packet).combatEdit$hadPacketModification()) {
            return previous;
        }

        return ItemStack.EMPTY;
    }
}
