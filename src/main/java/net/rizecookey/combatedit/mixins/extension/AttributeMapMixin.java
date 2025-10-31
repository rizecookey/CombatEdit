package net.rizecookey.combatedit.mixins.extension;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributeMapExtension;
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

@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin implements AttributeMapExtension {
    @Shadow @Final private Map<Holder<Attribute>, AttributeInstance> attributes;

    @Shadow protected abstract void onAttributeModified(AttributeInstance instance);

    @Shadow @Final private AttributeSupplier supplier;
    @Unique
    private boolean combatEdit$sendAllAttributes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setSendAllAttributes(AttributeSupplier defaultAttributes, CallbackInfo ci) {
        combatEdit$sendAllAttributes = defaultAttributes.combatEdit$sendAllAttributes();
    }

    @Inject(method = "getAttributesToSync", at = @At("HEAD"))
    private void potentiallyAddAllRemainingAttributes(CallbackInfoReturnable<Set<AttributeInstance>> cir) {
        if (!combatEdit$sendAllAttributes) {
            return;
        }

        for (var instance : supplier.combatEdit$getInstances().values()) {
            if (attributes.containsKey(instance.getAttribute())) {
                continue;
            }

            onAttributeModified(instance);
        }

        combatEdit$sendAllAttributes = false;
    }

    @Inject(method = "getSyncableAttributes", at = @At("HEAD"), cancellable = true)
    private void possiblySendAllAttributes(CallbackInfoReturnable<Collection<AttributeInstance>> cir) {
        if (combatEdit$sendAllAttributes) {
            cir.setReturnValue(Stream
                    .concat(
                            supplier.combatEdit$getInstances().values().stream()
                                    .filter(instance -> !attributes.containsKey(instance.getAttribute())),
                            attributes.values().stream())
                    .filter(instance -> instance.getAttribute().value().isClientSyncable())
                    .toList());
            cir.cancel();
        }
    }

    @Unique
    private static ConfigurationManager configurationProvider() {
        return ConfigurationManager.getInstance();
    }

    @Override
    public void combatEdit$patchWithNewDefaults(EntityType<? extends LivingEntity> type, AttributeSupplier previousDefaults) {
        attributes.forEach((attribute, instance) -> {
            if (!this.supplier.hasAttribute(attribute) || !previousDefaults.hasAttribute(attribute)) {
                return;
            }

            double oldDefault = previousDefaults.getBaseValue(attribute);
            double newDefault = supplier.getBaseValue(attribute);

            if (instance.getBaseValue() == oldDefault) {
                combatEdit$sendAllAttributes = true;
                instance.setBaseValue(newDefault);
            }
        });
    }

    @Override
    public AttributeMap combatEdit$getWithOriginalDefaults(EntityType<? extends LivingEntity> type) {
        AttributeSupplier originalDefaults = configurationProvider().getModifier().entities()
                .getVanillaDefaultAttributes(type);
        AttributeMap copy = new AttributeMap(supplier);
        copy.assignAllValues(thisInstance());
        attributes.forEach((attribute, instance) -> {
            if (!supplier.hasAttribute(attribute) || !originalDefaults.hasAttribute(attribute)) {
                return;
            }

            AttributeInstance copyInstance = copy.getInstance(attribute);
            if (copyInstance == null) {
                return;
            }

            double newDefault = supplier.getBaseValue(attribute);
            double oldDefault = originalDefaults.getBaseValue(attribute);
            if (instance.getBaseValue() == newDefault) {
                copyInstance.setBaseValue(oldDefault);
            }

        });

        return copy;
    }

    @Unique
    private AttributeMap thisInstance() {
        return (AttributeMap) (Object) this;
    }
}
