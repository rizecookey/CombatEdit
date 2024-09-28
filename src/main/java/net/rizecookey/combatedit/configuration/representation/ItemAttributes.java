package net.rizecookey.combatedit.configuration.representation;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemAttributes {
    private Identifier itemId;
    private List<ModifierEntry> modifiers;
    private boolean overrideDefault;

    public ItemAttributes(Identifier itemId, List<ModifierEntry> modifiers, boolean overrideDefault) {
        this.itemId = itemId;
        this.modifiers = new ArrayList<>(modifiers);
        this.overrideDefault = overrideDefault;
    }

    protected ItemAttributes() {}

    public Identifier getItemId() {
        return itemId;
    }

    public void setItemId(Identifier itemId) {
        this.itemId = itemId;
    }

    public List<ModifierEntry> getModifiers() {
        if (modifiers == null) {
            modifiers = new ArrayList<>();
        }
        return modifiers;
    }

    public boolean isOverrideDefault() {
        return overrideDefault;
    }

    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    public void validate() throws InvalidConfigurationException {
        if (itemId == null || !Registries.ITEM.containsId(itemId)) {
            throw new InvalidConfigurationException("No item with id %s found".formatted(itemId));
        }

        for (var modifier : getModifiers()) {
            modifier.validate();
        }
    }

    public ItemAttributes copy() {
        return new ItemAttributes(itemId, List.copyOf(modifiers), overrideDefault);
    }

    public static ItemAttributes getDefault() {
        return new ItemAttributes(new Identifier("minecraft:wooden_sword"), List.of(), false);
    }

    public record ModifierEntry(Identifier attribute, UUID uuid, String name, double value, EntityAttributeModifier.Operation operation, AttributeModifierSlot slot) {
        public static ModifierEntry getDefault() {
            return new ModifierEntry(new Identifier("minecraft:generic.attack_damage"), Item.ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 1, EntityAttributeModifier.Operation.ADD_VALUE, AttributeModifierSlot.MAINHAND);
        }

        public void validate() throws InvalidConfigurationException {
            if (attribute == null || !Registries.ATTRIBUTE.containsId(attribute)) {
                throw new InvalidConfigurationException("No attribute with id %s found".formatted(attribute));
            }

            if (operation == null) {
                throw new InvalidConfigurationException("No operation has been specified for this modifier");
            }

            if (slot == null) {
                throw new InvalidConfigurationException("No slot has been specified for this modifier");
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, modifiers, overrideDefault);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemAttributes attr)) {
            return false;
        }

        return itemId.equals(attr.itemId) && modifiers.equals(attr.modifiers) && overrideDefault == attr.overrideDefault;
    }
}
