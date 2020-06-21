package net.rizecookey.combatedit.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public class KnockbackMixin {
    @Mixin(LivingEntity.class)
    public static class LivingEntityKnockback {
        @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(Lnet/minecraft/entity/Entity;FDD)V"))
        public void handleTakeKnockback(LivingEntity livingEntity, Entity attacker, float speed, double xMovement, double zMovement) {
            speed = (float)((double)speed * (1.0D - livingEntity.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).getValue()));
            Vec3d velocity = livingEntity.getVelocity().multiply(0.5D).add(new Vec3d(-xMovement, 0.0D, -zMovement).normalize().multiply(speed)).add(0.0D, speed, 0.0D);
            if (velocity.getY() > 0.4D) {
                velocity.multiply(1.0D, 0.0D, 1.0D);
                velocity.add(0.0D, 0.4D, 0.0D);
            }
            livingEntity.setVelocity(velocity);
        }
    }
    @Mixin(PlayerEntity.class)
    public static class PlayerEntityKnockback {
        @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(Lnet/minecraft/entity/Entity;FDD)V"))
        public void handleTakeKnockback(LivingEntity livingEntity, Entity attacker, float speed, double xMovement, double zMovement) {
            livingEntity.addVelocity(- (xMovement * speed), 0.1D, - (zMovement * speed));
        }
    }
}
