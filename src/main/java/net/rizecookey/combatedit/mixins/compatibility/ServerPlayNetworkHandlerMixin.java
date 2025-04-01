package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizecookey.combatedit.extension.ServerCommonNetworkHandlerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
