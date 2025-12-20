package net.rizecookey.combatedit.mixins.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements LivingEntityExtension {
    @Shadow
    protected abstract void playServerSideSound(SoundEvent par1);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playServerSideSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    public void disableAttackSounds(Player instance, SoundEvent soundEvent) {
        if (level().isClientSide() || shouldPlayAttackSound(soundEvent)) {
            playServerSideSound(soundEvent);
        }
    }

    @Redirect(method = "doSweepAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playServerSideSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    public void disableSweepAttackSounds(Player instance, SoundEvent soundEvent) {
        if (level().isClientSide() || shouldPlayAttackSound(soundEvent)) {
            playServerSideSound(soundEvent);
        }
    }

    @Redirect(method = "attackVisualEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playServerSideSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    public void disableSpecialAttackSounds(Player instance, SoundEvent soundEvent) {
        if (level().isClientSide() || shouldPlayAttackSound(soundEvent)) {
            playServerSideSound(soundEvent);
        }
    }

    @Unique
    private boolean shouldPlayAttackSound(SoundEvent sound) {
        if (!Configuration.CONFIGURABLE_SOUNDS.contains(sound)) {
            return true;
        }

        Configuration configuration = combatEdit$configurationManager().getConfiguration();
        return configuration.isSoundEnabled(sound.location()).orElse(false);
    }
}
