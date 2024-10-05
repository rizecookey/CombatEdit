package net.rizecookey.combatedit.mixins.modification.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.rizecookey.combatedit.extension.DynamicDefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = DefaultAttributeRegistry.class, priority = Integer.MAX_VALUE)
public class DefaultAttributeRegistryMixin {
    @Unique
    private static Map<EntityType<? extends LivingEntity>, DynamicDefaultAttributeContainer> DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void initDynamicRegistry(CallbackInfo ci) {
        DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY = new HashMap<>();
    }

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private static DefaultAttributeContainer useDynamic(DefaultAttributeContainer defaultAttributeContainer, @Local(argsOnly = true) EntityType<? extends LivingEntity> type) {
        return DYNAMIC_DEFAULT_ATTRIBUTE_REGISTRY.computeIfAbsent(type, key -> new DynamicDefaultAttributeContainer(defaultAttributeContainer));
    }
}
