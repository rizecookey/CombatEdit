package net.rizecookey.combatedit.mixins.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Avatar implements LivingEntityExtension {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;)V"))
    public void disableAttackSounds(Level world, Entity entity, double x, double y, double z, SoundEvent sound, SoundSource category) {
        if (level().isClientSide() || shouldPlayAttackSound(sound)) {
            world.playSound(entity, x, y, z, sound, category);
        }
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    public void disableAttackSounds(Level world, Entity entity, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        if (level().isClientSide() || shouldPlayAttackSound(sound)) {
            world.playSound(entity, x, y, z, sound, category, volume, pitch);
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
