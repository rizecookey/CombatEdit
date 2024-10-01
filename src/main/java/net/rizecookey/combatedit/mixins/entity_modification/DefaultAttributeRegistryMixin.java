package net.rizecookey.combatedit.mixins.entity_modification;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.rizecookey.combatedit.extension.MutableDefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.stream.Collectors;

@Mixin(DefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {
    @Shadow public static Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> DEFAULT_ATTRIBUTE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void replaceWithMutable(CallbackInfo ci) {
        DEFAULT_ATTRIBUTE_REGISTRY = DEFAULT_ATTRIBUTE_REGISTRY.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), MutableDefaultAttributeContainer.copyOf(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
