package net.rizecookey.combatedit.entity_modification;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;

public interface EntityAttributeModifierProvider {
    boolean shouldModifyEntity(Identifier id, EntityType<? extends LivingEntity> type);
    DefaultAttributeContainer getModifiers(Identifier id, EntityType<? extends LivingEntity> type, DefaultAttributeContainer originalDefaults);
}
