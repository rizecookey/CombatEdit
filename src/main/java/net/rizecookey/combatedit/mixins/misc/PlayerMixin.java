package net.rizecookey.combatedit.mixins.misc;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements LivingEntityExtension {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyVariable(method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), name = "sweepAttack")
    public boolean checkIfSweepEnchant(boolean sweepAttack) {
        if (level().isClientSide()) {
            return sweepAttack;
        }
        Configuration.MiscOptions miscOptions = combatEdit$configurationManager().getConfiguration().getMiscOptions();
        var thisPlayer = (Player) (Object) this;
        var sweepingRegistryEntry = level().registryAccess().lookupOrThrow(Enchantments.SWEEPING_EDGE.registryKey())
                .get(Enchantments.SWEEPING_EDGE.identifier()).orElseThrow();
        if (EnchantmentHelper.getEnchantmentLevel(sweepingRegistryEntry, thisPlayer) == 0
                && miscOptions.isSweepingWithoutEnchantmentDisabled().orElse(false)) {
            sweepAttack = false;
        }
        return sweepAttack;
    }

}
