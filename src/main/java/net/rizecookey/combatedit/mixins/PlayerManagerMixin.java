package net.rizecookey.combatedit.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void setupAttribute(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        EntityAttributeInstance attackSpeedInstance = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (attackSpeedInstance != null) {
            attackSpeedInstance.setBaseValue(20D);
        }
        else {
            player.getAttributes().register(EntityAttributes.ATTACK_SPEED).setBaseValue(20D);
        }
    }
}
