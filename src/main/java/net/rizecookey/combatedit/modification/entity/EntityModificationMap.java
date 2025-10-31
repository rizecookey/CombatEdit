package net.rizecookey.combatedit.modification.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class EntityModificationMap implements EntityModificationProvider {
    private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributeMap;

    public EntityModificationMap(Map<EntityType<? extends LivingEntity>, AttributeSupplier> entries) {
        this.attributeMap = Map.copyOf(entries);
    }

    @Override
    public boolean shouldModifyEntity(ResourceLocation id, EntityType<? extends LivingEntity> type) {
        return attributeMap.containsKey(type);
    }

    @Override
    public AttributeSupplier getModifiers(ResourceLocation id, EntityType<? extends LivingEntity> type, AttributeSupplier originalDefaults) {
        return attributeMap.get(type);
    }

    public static EntityModificationMap fromConfiguration(List<EntityAttributes> entityAttributes, DefaultsSupplier.Entities entityDefaultsSupplier) {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new HashMap<>();
        Function<EntityType<? extends LivingEntity>, AttributeSupplier> defaultProvider = type -> {
            if (map.containsKey(type)) {
                return map.get(type);
            } else {
                return entityDefaultsSupplier.getVanillaDefaultAttributes(type);
            }
        };

        for (EntityAttributes entityAttribute : entityAttributes) {
            var result = fromConfigurationEntry(entityAttribute, defaultProvider);
            if (result != null) {
                map.put(result.getKey(), result.getValue());
            }
        }

        return new EntityModificationMap(map);
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<EntityType<? extends LivingEntity>, AttributeSupplier> fromConfigurationEntry(EntityAttributes modifier, Function<EntityType<? extends LivingEntity>, AttributeSupplier> originalDefaults) {
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(modifier.getEntityId())) {
            LOGGER.warn("No entity with id {} found, skipping entry", modifier.getEntityId());
            return null;
        }

        EntityType<? extends LivingEntity> type;
        try {
            type = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(modifier.getEntityId());
        } catch (ClassCastException e) {
            LOGGER.warn("{} is not a living entity and thus does not have attributes, skipping entry", modifier.getEntityId());
            return null;
        }

        var builder = AttributeSupplier.builder();
        if (!modifier.isOverrideDefault()) {
            AttributeSupplier defaults = originalDefaults.apply(type);
            if (defaults == null) defaults = AttributeSupplier.builder().build();
            defaults.combatEdit$getInstances().values()
                    .forEach(instance -> builder.add(instance.getAttribute(), instance.getBaseValue()));
        }

        for (var attributeValue : modifier.getBaseValues()) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(attributeValue.attribute())) {
                LOGGER.warn("No attribute with id {} found, skipping entry for {}", attributeValue.attribute(), modifier.getEntityId());
                continue;
            }
            var attribute = BuiltInRegistries.ATTRIBUTE.get(attributeValue.attribute()).orElseThrow();
            builder.add(attribute, attributeValue.baseValue());
        }

        return Map.entry(type, builder.build());
    }
}
