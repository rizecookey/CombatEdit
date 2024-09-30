package net.rizecookey.combatedit.mixins.knockback;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique
    private ServerConfigurationManager configurationProvider;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCombatEditReference(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        configurationProvider = ServerConfigurationManager.getInstance();
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
    public void handleTakeKnockback(LivingEntity livingEntity, double speed, double xMovement, double zMovement) {
        if (!configurationProvider.getConfiguration().getMiscOptions().is1_8KnockbackEnabled().orElse(false)) {
            livingEntity.takeKnockback(speed, xMovement, zMovement);
            return;
        }

        speed = (float) (speed * (1.0D - Objects.requireNonNull(livingEntity.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)).getValue()));
        livingEntity.addVelocity(-(xMovement * speed), 0.1D, -(zMovement * speed));
    }
}
