package net.rizecookey.combatedit.mixins.modification.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.rizecookey.combatedit.extension.DynamicDefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = DefaultAttributes.class, priority = Integer.MAX_VALUE)
public class DefaultAttributeRegistryMixin {
    @Unique
    private static Map<EntityType<? extends LivingEntity>, DynamicDefaultAttributeContainer> DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void initDynamicRegistry(CallbackInfo ci) {
        DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY = new HashMap<>();
    }

    @ModifyReturnValue(method = "getSupplier", at = @At("RETURN"))
    private static AttributeSupplier useDynamic(AttributeSupplier defaultAttributeContainer, @Local(argsOnly = true) EntityType<? extends LivingEntity> type) {
        return DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY.computeIfAbsent(type, key -> new DynamicDefaultAttributeContainer(defaultAttributeContainer));
    }
}
