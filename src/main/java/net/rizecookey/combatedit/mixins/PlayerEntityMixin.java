package net.rizecookey.combatedit.mixins;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "initAttributes", at = @At("TAIL"))
    public void changeAttribute(CallbackInfo ci) {
        if (this.getAttributes().get(EntityAttributes.ATTACK_SPEED) != null) {
            this.getAttributes().get(EntityAttributes.ATTACK_SPEED).setBaseValue(20D);
        }
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void disableAttackSounds(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (sound != SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK && sound != SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE && sound != SoundEvents.ENTITY_PLAYER_ATTACK_STRONG && sound != SoundEvents.ENTITY_PLAYER_ATTACK_WEAK && sound != SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP) {
            world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @ModifyVariable(method = "attack", name = "bl4", ordinal = 0, at = @At("FIELD"))
    public boolean checkIfSweepEnchant(boolean bl4) {
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.SWEEPING, playerEntity) == 0) {
            bl4 = false;
        }
        return bl4;
    }

}
