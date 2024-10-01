package net.rizecookey.combatedit.mixins.knockback;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private ServerConfigurationManager configurationProvider;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCombatEditReference(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        configurationProvider = ServerConfigurationManager.getInstance();
    }

    @ModifyArg(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), index = 1)
    public double changeKnockbackY(double y, @Local(ordinal = 0, argsOnly = true) double strength, @Local(ordinal = 0) Vec3d vec3d) {
        if (getWorld().isClient() || !configurationProvider.getConfiguration().getMiscOptions().is1_8KnockbackEnabled().orElse(false)) {
            return y;
        }

        y = vec3d.y / 2.0 + strength;
        return vec3d.y > 0.4D ? 0.4D : y;
    }
}
