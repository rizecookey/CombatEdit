package net.rizecookey.combatedit.configuration;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

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
        return modifiers;
    }

    public boolean isOverrideDefault() {
        return overrideDefault;
    }

    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    public record ModifierEntry(Identifier attribute, NbtCompound modifier, AttributeModifierSlot slot) {}
}
