package net.rizecookey.combatedit.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    ServerPlayerEntity instance = (ServerPlayerEntity) (Object) this;
    @Inject(method = "<init>", at = @At("TAIL"))
    public void sendAttributeUpdate(MinecraftServer server, ServerWorld world, GameProfile profile, PlayerPublicKey publicKey, CallbackInfo ci) {
        EntityAttributeInstance entityAttributeInstance = instance.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (entityAttributeInstance != null) entityAttributeInstance.setBaseValue(entityAttributeInstance.getBaseValue());
    }
}
