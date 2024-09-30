package net.rizecookey.combatedit.mixins.misc;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements LivingEntityExtension {
    @ModifyVariable(method = "attack", ordinal = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.BEFORE))
    public boolean checkIfSweepEnchant(boolean bl4) {
        Configuration.MiscOptions miscOptions = combatEdit$configurationManager().getConfiguration().getMiscOptions();
        var thisPlayer = (PlayerEntity) (Object) this;
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.SWEEPING_EDGE, thisPlayer) == 0 && miscOptions.isSweepingWithoutEnchantmentDisabled().orElse(false)) {
            bl4 = false;
        }
        return bl4;
    }

}
