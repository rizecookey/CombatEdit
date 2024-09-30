package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(AttributeContainer.class)
public abstract class AttributeContainerMixin implements AttributeContainerExtension {
    @Shadow @Final private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom;
    @Shadow @Final private DefaultAttributeContainer fallback;

    @Shadow protected abstract void updateTrackedStatus(EntityAttributeInstance instance);

    @Unique
    private boolean combatEdit$sendAllAttributes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setSendAllAttributes(DefaultAttributeContainer defaultAttributes, CallbackInfo ci) {
        DefaultAttributeContainerExtension extendedContainer = (DefaultAttributeContainerExtension) defaultAttributes;
        combatEdit$sendAllAttributes = extendedContainer.combatEdit$sendAllAttributes();
    }

    @Inject(method = "getTracked", at = @At("HEAD"))
    private void potentiallyAddAllRemainingAttributes(CallbackInfoReturnable<Set<EntityAttributeInstance>> cir) {
        if (!combatEdit$sendAllAttributes) {
            return;
        }

        for (var instance : ((DefaultAttributeContainerExtension) fallback).combatEdit$getInstances()) {
            if (custom.containsKey(instance.getAttribute())) {
                continue;
            }

            updateTrackedStatus(instance);
        }

        combatEdit$sendAllAttributes = false;
    }

    @Inject(method = "getAttributesToSend", at = @At("HEAD"), cancellable = true)
    private void possiblySendAllAttributes(CallbackInfoReturnable<Collection<EntityAttributeInstance>> cir) {
        if (combatEdit$sendAllAttributes) {
            cir.setReturnValue(Stream
                    .concat(
                            ((DefaultAttributeContainerExtension) fallback).combatEdit$getInstances().stream()
                                    .filter(instance -> !custom.containsKey(instance.getAttribute())),
                            custom.values().stream())
                    .filter(instance -> instance.getAttribute().value().isTracked())
                    .toList());
            cir.cancel();
        }
    }

    @Unique
    private static ServerConfigurationManager configurationProvider() {
        return ServerConfigurationManager.getInstance();
    }

    @Override
    public void combatEdit$patchWithNewDefaults(EntityType<? extends LivingEntity> type, DefaultAttributeContainer previousDefaults) {
        custom.forEach((attribute, instance) -> {
            if (!this.fallback.has(attribute) || !previousDefaults.has(attribute)) {
                return;
            }

            double oldDefault = previousDefaults.getBaseValue(attribute);
            double newDefault = fallback.getBaseValue(attribute);

            if (instance.getBaseValue() == oldDefault) {
                combatEdit$sendAllAttributes = true;
                instance.setBaseValue(newDefault);
            }
        });
    }

    @Override
    public AttributeContainer combatEdit$getWithOriginalDefaults(EntityType<? extends LivingEntity> type) {
        DefaultAttributeContainer originalDefaults = configurationProvider().getModifier().getOriginalDefaults(type);
        AttributeContainer copy = new AttributeContainer(fallback);
        copy.setFrom(thisInstance());
        custom.forEach((attribute, instance) -> {
            if (!fallback.has(attribute) || !originalDefaults.has(attribute)) {
                return;
            }

            EntityAttributeInstance copyInstance = copy.getCustomInstance(attribute);
            if (copyInstance == null) {
                return;
            }

            double newDefault = fallback.getBaseValue(attribute);
            double oldDefault = originalDefaults.getBaseValue(attribute);
            if (instance.getBaseValue() == newDefault) {
                copyInstance.setBaseValue(oldDefault);
            }

        });

        return copy;
    }

    @Unique
    private AttributeContainer thisInstance() {
        return (AttributeContainer) (Object) this;
    }
}
