package net.rizecookey.combatedit.mixins.knockback;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(FDD)V"))
    public void handleTakeKnockback(LivingEntity livingEntity, float speed, double xMovement, double zMovement) {
        speed = (float) ((double) speed * (1.0D - livingEntity.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).getValue()));
        livingEntity.addVelocity(-(xMovement * speed), 0.1D, -(zMovement * speed));
    }
}
