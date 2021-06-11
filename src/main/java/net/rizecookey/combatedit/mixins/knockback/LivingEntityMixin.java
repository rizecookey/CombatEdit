package net.rizecookey.combatedit.mixins.knockback;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique private Vec3d cache_vec3d;
    @Unique private double cache_f;
    @Inject(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void loadVec3d(double f, double d, double e, CallbackInfo ci, Vec3d vec3d, Vec3d vec3d2) {
        this.cache_f = f;
        this.cache_vec3d = vec3d;
    }

    @ModifyArg(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), index = 1)
    public double changeKnockbackY(double y) {
        y = cache_vec3d.y / 2.0 + cache_f;
        return cache_vec3d.y > 0.4D ? 0.4D : y;
    }
}
