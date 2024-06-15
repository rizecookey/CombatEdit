package net.rizecookey.combatedit.configuration;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public Identifier getEntityId() {
        return entityId;
    }

    public void setEntityId(Identifier entityId) {
        this.entityId = entityId;
    }

    public List<AttributeBaseValue> getBaseValues() {
        return baseValues;
    }

    public boolean isOverrideDefault() {
        return overrideDefault;
    }

    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    public void validate() throws InvalidConfigurationException {
        if (entityId == null || !Registries.ENTITY_TYPE.containsId(entityId)) {
            throw new InvalidConfigurationException("No entity with id %s found".formatted(entityId));
        }

        if (baseValues == null) {
            baseValues = new ArrayList<>();
        }

        for (var baseValue : baseValues) {
            baseValue.validate();
        }
    }

    public static EntityAttributes getDefault() {
        return new EntityAttributes(new Identifier("minecraft:creeper"), List.of(), false);
    }

    public record AttributeBaseValue(Identifier attribute, double baseValue) {
        public static AttributeBaseValue getDefault() {
            return new AttributeBaseValue(new Identifier("minecraft:generic.attack_damage"), 1);
        }

        public void validate() throws InvalidConfigurationException {
            if (attribute() == null || !Registries.ATTRIBUTE.containsId(attribute())) {
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
