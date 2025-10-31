package net.rizecookey.combatedit.extension;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public interface AttributeMapExtension {
    ThreadLocal<Deque<Boolean>> IS_SAVE_CALL = ThreadLocal.withInitial(() -> new ArrayDeque<>(List.of(false)));

    default void combatEdit$patchWithNewDefaults(EntityType<? extends LivingEntity> type, AttributeSupplier previousDefaults) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }

    default AttributeMap combatEdit$getWithOriginalDefaults(EntityType<? extends LivingEntity> type) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
