package net.rizecookey.combatedit.mixins;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    @Inject(method = "onDisconnected", at = @At("HEAD"))
    public void handleDisconnected(Text reason, CallbackInfo ci) {
        if (player.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_SPEED) && player.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED) == 20D) {
            Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)).setBaseValue(4D);
        }
    }
}
