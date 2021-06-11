package net.rizecookey.combatedit.mixins.knockback;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(FDD)V"))
    public void handleTakeKnockback(LivingEntity livingEntity, float speed, double xMovement, double zMovement) {
        speed = (float) ((double) speed * (1.0D - livingEntity.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).getValue()));
        Vec3d velocity = livingEntity.getVelocity().multiply(0.5D).add(new Vec3d(-xMovement, 0.0D, -zMovement).normalize().multiply(speed)).add(0.0D, speed, 0.0D);
        if (velocity.getY() > 0.4D) {
            velocity.multiply(1.0D, 0.0D, 1.0D);
            velocity.add(0.0D, 0.4D, 0.0D);
        }
        livingEntity.setVelocity(velocity);
    }
}
