package net.rizecookey.combatedit.mixins.knockback;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyArg(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), index = 1)
    public double changeKnockbackY(double y, @Local(ordinal = 0, argsOnly = true) double strength, @Local(ordinal = 0) Vec3d vec3d) {
        y = vec3d.y / 2.0 + strength;
        return vec3d.y > 0.4D ? 0.4D : y;
    }
}
