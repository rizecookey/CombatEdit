package net.rizecookey.combatedit.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class DefaultEntityAttributeModifiers implements EntityAttributeModifierProvider {
    @Override
    public boolean shouldModifyEntity(Identifier id, EntityType<? extends LivingEntity> type) {
        return type.equals(EntityType.PLAYER);
    }

    @Override
    public DefaultAttributeContainer getModifiers(Identifier id, EntityType<? extends LivingEntity> type) {
        return PlayerEntity.createPlayerAttributes().add(EntityAttributes.GENERIC_ATTACK_SPEED, 20.0).build();
    }
}
