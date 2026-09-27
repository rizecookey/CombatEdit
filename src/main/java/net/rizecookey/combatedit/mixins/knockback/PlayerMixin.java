package net.rizecookey.combatedit.mixins.knockback;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar {
    @Shadow
    public abstract float getSpeed();

    @Unique
    private ConfigurationManager configurationProvider;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCombatEditReference(CallbackInfo ci) {
        configurationProvider = ConfigurationManager.getInstance();
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V"))
    public void handleTakeKnockback(LivingEntity instance, double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        if (level().isClientSide() || !configurationProvider.getConfiguration().getMiscOptions().is1_8KnockbackEnabled().orElse(false)) {
            instance.knockback(power, xd, zd, source, damage, comesFromEffect);
            return;
        }

        power = (float) (power * (1.0D - Objects.requireNonNull(instance.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).getValue()));
        instance.push(-(xd * power), 0.1D, -(zd * power));
    }
}
