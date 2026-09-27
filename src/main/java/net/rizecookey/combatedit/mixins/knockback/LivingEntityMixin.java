package net.rizecookey.combatedit.mixins.knockback;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private ConfigurationManager configurationProvider;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCombatEditReference(EntityType<? extends LivingEntity> type, Level level, CallbackInfo ci) {
        configurationProvider = ConfigurationManager.getInstance();
    }

    @ModifyArg(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"), index = 1)
    public double changeKnockbackY(double y, @Local(argsOnly = true, name = "power") double power, @Local(name = "deltaMovement") Vec3 deltaMovement) {
        if (level().isClientSide() || !configurationProvider.getConfiguration().getMiscOptions().is1_8KnockbackEnabled().orElse(false)) {
            return y;
        }

        y = deltaMovement.y / 2.0 + power;
        return deltaMovement.y > 0.4D ? 0.4D : y;
    }
}
