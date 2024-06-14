package net.rizecookey.combatedit.configuration;

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

    public record AttributeBaseValue(Identifier attribute, double baseValue) {}

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
