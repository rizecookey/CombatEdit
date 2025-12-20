package net.rizecookey.combatedit.configuration.representation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list of overridden entity attribute base values for a given entity.
 */
public class EntityAttributes {
    private Identifier entityId;
    private List<AttributeBaseValue> baseValues;
    private boolean overrideDefault;

    public EntityAttributes(Identifier entityId, List<AttributeBaseValue> baseValues, boolean overrideDefault) {
        this.entityId = entityId;
        this.baseValues = new ArrayList<>(baseValues);
        this.overrideDefault = overrideDefault;
    }

    protected EntityAttributes() {}

    /**
     * Returns the ID of the entity that this object overrides attribute base values for.
     * @return the id of the entity for which values are overridden
     */
    public Identifier getEntityId() {
        return entityId;
    }

    public void setEntityId(Identifier entityId) {
        this.entityId = entityId;
    }

    /**
     * Returns a list of pairs of attributes and base values that should be overridden for this entity.
     * @return the list of attributes alongside base values overrides for this entity
     */
    public List<AttributeBaseValue> getBaseValues() {
        if (baseValues == null) {
            baseValues = new ArrayList<>();
        }

        return baseValues;
    }

    /**
     * Returns whether all attributes which are not overridden here should be removed from
     * the entity. Should not be used because it generally causes crashes as some entities
     * might not have required attributes set as a result.
     */
    public boolean isOverrideDefault() {
        return overrideDefault;
    }

    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    public void validate() throws InvalidConfigurationException {
        if (entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            throw new InvalidConfigurationException("No entity with id %s found".formatted(entityId));
        }

        for (var baseValue : getBaseValues()) {
            baseValue.validate();
        }
    }

    public EntityAttributes copy() {
        return new EntityAttributes(entityId, List.copyOf(baseValues), overrideDefault);
    }

    public static EntityAttributes getDefault() {
        return new EntityAttributes(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CREEPER), List.of(), false);
    }

    public record AttributeBaseValue(Identifier attribute, double baseValue) {
        public static AttributeBaseValue getDefault() {
            var attackDamageAttribute = net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;
            return new AttributeBaseValue(BuiltInRegistries.ATTRIBUTE.getKey(attackDamageAttribute.value()), 1);
        }

        public void validate() throws InvalidConfigurationException {
            if (attribute() == null || !BuiltInRegistries.ATTRIBUTE.containsKey(attribute())) {
                throw new InvalidConfigurationException("No attribute with id %s found".formatted(attribute()));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntityAttributes attr)) {
            return false;
        }

        return getEntityId().equals(attr.getEntityId()) && isOverrideDefault() == attr.isOverrideDefault() && getBaseValues().equals(attr.getBaseValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntityId(), getBaseValues(), isOverrideDefault());
    }
}
