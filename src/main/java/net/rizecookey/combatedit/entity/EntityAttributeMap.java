package net.rizecookey.combatedit.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.EntityAttributes;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class EntityAttributeMap implements EntityAttributeModifierProvider {
    private final Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> attributeMap;

    public EntityAttributeMap(Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> entries) {
        this.attributeMap = Map.copyOf(entries);
    }

    @Override
    public boolean shouldModifyEntity(Identifier id, EntityType<? extends LivingEntity> type) {
        return attributeMap.containsKey(type);
    }

    @Override
    public DefaultAttributeContainer getModifiers(Identifier id, EntityType<? extends LivingEntity> type, DefaultAttributeContainer originalDefaults) {
        return attributeMap.get(type);
    }

    public static EntityAttributeMap fromConfiguration(List<EntityAttributes> entityAttributes, Function<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalDefaults) {
        Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> map = new HashMap<>();
        entityAttributes.stream()
                .map(modifier -> fromConfigurationEntry(modifier, originalDefaults))
                .filter(Objects::nonNull)
                .forEach(result -> map.put(result.getKey(), result.getValue()));

        return new EntityAttributeMap(map);
    }

    private static Map.Entry<EntityType<? extends LivingEntity>, DefaultAttributeContainer> fromConfigurationEntry(EntityAttributes modifier, Function<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalDefaults) {
        if (!Registries.ENTITY_TYPE.containsId(modifier.getEntityId())) {
            LOGGER.warn("No entity with id {} found, skipping entry", modifier.getEntityId());
            return null;
        }

        EntityType<? extends LivingEntity> type;
        try {
            //noinspection unchecked
            type = (EntityType<? extends LivingEntity>) Registries.ENTITY_TYPE.get(modifier.getEntityId());
        } catch (ClassCastException e) {
            LOGGER.warn("{} is not a living entity and thus does not have attributes, skipping entry", modifier.getEntityId());
            return null;
        }

        var builder = DefaultAttributeContainer.builder();
        if (!modifier.isOverrideDefault()) {
            DefaultAttributeContainer defaults = originalDefaults.apply(type);
            if (defaults == null) defaults = DefaultAttributeContainer.builder().build();
            ((DefaultAttributeContainerExtension) defaults).combatEdit$getInstances()
                    .forEach(instance -> builder.add(instance.getAttribute(), instance.getBaseValue()));
        }

        for (var attributeValue : modifier.getBaseValues()) {
            if (!Registries.ATTRIBUTE.containsId(attributeValue.attribute())) {
                LOGGER.warn("No attribute with id {} found, skipping entry for {}", attributeValue.attribute(), modifier.getEntityId());
                continue;
            }
            var attribute = Registries.ATTRIBUTE.getEntry(attributeValue.attribute()).orElseThrow();
            builder.add(attribute, attributeValue.baseValue());
        }

        return Map.entry(type, builder.build());
    }
}
