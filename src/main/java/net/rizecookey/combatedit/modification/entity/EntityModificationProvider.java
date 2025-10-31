package net.rizecookey.combatedit.modification.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public interface EntityModificationProvider {
    boolean shouldModifyEntity(ResourceLocation id, EntityType<? extends LivingEntity> type);
    AttributeSupplier getModifiers(ResourceLocation id, EntityType<? extends LivingEntity> type, AttributeSupplier originalDefaults);
}
