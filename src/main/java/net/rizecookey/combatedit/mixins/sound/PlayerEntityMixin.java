package net.rizecookey.combatedit.mixins.sound;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements LivingEntityExtension {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void disableAttackSounds(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (shouldPlayAttackSound(sound)) {
            world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Unique
    private boolean shouldPlayAttackSound(SoundEvent sound) {
        if (!Configuration.CONFIGURABLE_SOUNDS.contains(sound)) {
            return true;
        }

        Configuration configuration = combatEdit$configurationManager().getConfiguration();
        return configuration.isSoundEnabled(sound.getId()).orElse(false);
    }
}
