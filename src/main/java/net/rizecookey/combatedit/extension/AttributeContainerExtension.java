package net.rizecookey.combatedit.extension;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public interface AttributeContainerExtension {
    ThreadLocal<Deque<Boolean>> IS_SAVE_CALL = ThreadLocal.withInitial(() -> new ArrayDeque<>(List.of(false)));

    void combatEdit$patchWithNewDefaults(EntityType<? extends LivingEntity> type, DefaultAttributeContainer previousDefaults);
    AttributeContainer combatEdit$getWithOriginalDefaults(EntityType<? extends LivingEntity> type);
}
