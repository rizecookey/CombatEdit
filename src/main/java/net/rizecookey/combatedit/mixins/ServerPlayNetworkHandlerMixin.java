package net.rizecookey.combatedit.mixins;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    @Inject(method = "onDisconnected", at = @At("HEAD"))
    public void handleDisconnected(Text reason, CallbackInfo ci) {
        if (player.getAttributes().get(EntityAttributes.ATTACK_SPEED) != null && player.getAttributes().get(EntityAttributes.ATTACK_SPEED).getBaseValue() == 20D) {
            Objects.requireNonNull(player.getAttributes().get(EntityAttributes.ATTACK_SPEED)).setBaseValue(4D);
        }
    }
}
