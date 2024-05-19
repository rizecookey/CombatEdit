package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
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
import java.util.stream.Stream;

@Mixin(AttributeContainer.class)
public abstract class AttributeContainerMixin {
    @Shadow @Final private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom;
    @Shadow @Final private DefaultAttributeContainer fallback;

    @Shadow protected abstract void updateTrackedStatus(EntityAttributeInstance instance);

    @Unique
    private boolean combatEdit$sendAllAttributes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setSendAllAttributes(DefaultAttributeContainer defaultAttributes, CallbackInfo ci) {
        DefaultAttributeContainerExtension extendedContainer = (DefaultAttributeContainerExtension) defaultAttributes;
        combatEdit$sendAllAttributes = extendedContainer.combatEdit$sendAllAttributes();
        if (combatEdit$sendAllAttributes) {
            for (var instance : extendedContainer.combatEdit$getInstances()) {
                updateTrackedStatus(instance);
            }
        }
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
}
