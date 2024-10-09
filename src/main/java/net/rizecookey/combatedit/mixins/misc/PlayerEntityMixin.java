package net.rizecookey.combatedit.mixins.misc;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements LivingEntityExtension {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyVariable(method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), ordinal = 3)
    public boolean checkIfSweepEnchant(boolean bl4) {
        if (getWorld().isClient()) {
            return bl4;
        }
        Configuration.MiscOptions miscOptions = combatEdit$configurationManager().getConfiguration().getMiscOptions();
        var thisPlayer = (PlayerEntity) (Object) this;
        var sweepingRegistryEntry = getWorld().getRegistryManager().getOrThrow(Enchantments.SWEEPING_EDGE.getRegistryRef())
                .getEntry(Enchantments.SWEEPING_EDGE.getValue()).orElseThrow();
        if (EnchantmentHelper.getEquipmentLevel(sweepingRegistryEntry, thisPlayer) == 0
                && miscOptions.isSweepingWithoutEnchantmentDisabled().orElse(false)) {
            bl4 = false;
        }
        return bl4;
    }

}
