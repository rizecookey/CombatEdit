package net.rizecookey.combatedit.mixins;


import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    PlayerEntity instance = (PlayerEntity) (Object) this;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Redirect(method = "createPlayerAttributes", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;add(Lnet/minecraft/entity/attribute/EntityAttribute;)Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", ordinal = 0))
    private static DefaultAttributeContainer.Builder changeAttributes(DefaultAttributeContainer.Builder builder, EntityAttribute attribute) {
        return builder.add(EntityAttributes.GENERIC_ATTACK_SPEED, 20D);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void disableAttackSounds(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (sound != SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK && sound != SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE && sound != SoundEvents.ENTITY_PLAYER_ATTACK_STRONG && sound != SoundEvents.ENTITY_PLAYER_ATTACK_WEAK && sound != SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP) {
            world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @ModifyVariable(method = "attack", ordinal = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.BEFORE))
    public boolean checkIfSweepEnchant(boolean bl4) {
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.SWEEPING, instance) == 0) {
            bl4 = false;
        }
        return bl4;
    }

}
