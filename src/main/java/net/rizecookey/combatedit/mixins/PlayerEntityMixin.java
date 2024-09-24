package net.rizecookey.combatedit.mixins;


import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.SoundConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Unique
    private PlayerEntity instance = (PlayerEntity) (Object) this;

    @Unique
    private CombatEdit combatEdit;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void loadCombatEditReference(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        this.combatEdit = CombatEdit.getInstance();
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void disableAttackSounds(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (shouldPlayAttackSound(sound)) {
            world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Unique
    private boolean shouldPlayAttackSound(SoundEvent sound) {
        if (!SoundConfiguration.CONFIGURABLE_SOUNDS.contains(sound)) {
            return true;
        }

        SoundConfiguration soundConfiguration = combatEdit.getConfig().getSoundConfiguration();
        return soundConfiguration.getEnabledSounds().getOrDefault(sound.getId(), false);
    }

    @ModifyVariable(method = "attack", ordinal = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.BEFORE))
    public boolean checkIfSweepEnchant(boolean bl4) {
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.SWEEPING_EDGE, instance) == 0 && combatEdit.getConfig().getMiscConfiguration().isSweepingWithoutEnchantmentDisabled()) {
            bl4 = false;
        }
        return bl4;
    }

}
