package net.rizecookey.combatedit.mixins.extension;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityExtension {
    @Unique
    private ConfigurationManager configurationManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initConfigurationManagerReference(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        configurationManager = ConfigurationManager.getInstance();
    }

    @Override
    public ConfigurationManager combatEdit$configurationManager() {
        return configurationManager;
    }
}
