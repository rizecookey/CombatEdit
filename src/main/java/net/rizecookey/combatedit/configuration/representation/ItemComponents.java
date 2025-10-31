package net.rizecookey.combatedit.configuration.representation;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemComponents {
    private ResourceLocation itemId;
    private List<ComponentChangeEntry> changes;

    public ItemComponents(ResourceLocation itemId, List<ComponentChangeEntry> changes) {
        this.itemId = itemId;
        this.changes = new ArrayList<>(changes);
    }

    protected ItemComponents() {}

    /**
     * Returns the identifier of the item to be modified.
     * @return the identifier of the item to be modified
     */
    public ResourceLocation getItemId() {
        return itemId;
    }

    public void setItemId(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    /**
     * Returns a list of component changes to be made to the specified item.
     * @return a list of component changes to be made to the specified item
     */
    public List<ComponentChangeEntry> getChanges() {
        if (changes == null) {
            changes = new ArrayList<>();
        }

        return changes;
    }

    public void validate() throws InvalidConfigurationException {
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            throw new InvalidConfigurationException("No item with id %s found".formatted(itemId));
        }

        for (var component : getChanges()) {
            component.validate();
        }
    }

    /**
     * Enum for declaring the change type of a component change entry.
     */
    public enum ChangeType {
        /** Sets a new value for a component. */
        @SerializedName("set")
        SET(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type.set")),
        /** Removes the default component value for the given type from the item. */
        @SerializedName("remove")
        REMOVE(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type.remove")),
        ;

        private final Component text;

        ChangeType(Component text) {
            this.text = text;
        }

        public Component getText() {
            return text;
        }
    }

    /**
     * A component change entry for an item.
     *
     * @param componentType the identifier of the component to be changed
     * @param changeType the change type for this component entry
     * @param value the value to use for this component, or an empty string if the component type has no values or the component is to be removed
     */
    public record ComponentChangeEntry(ResourceLocation componentType, ChangeType changeType, String value) {
        public ComponentChangeEntry(ResourceLocation componentType, @Nullable ChangeType changeType, @Nullable String value) {
            this.componentType = componentType;
            this.changeType = changeType != null ? changeType : ChangeType.SET;
            this.value = value != null ? value : "";
        }
        public void validate() throws InvalidConfigurationException {
            if (componentType == null || !BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(componentType)) {
                throw new InvalidConfigurationException("Unknown component id");
            }

            if (ChangeType.REMOVE.equals(changeType)) {
                return;
            }

            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentType);
            assert type != null;
            if (Unit.CODEC.equals(type.codec())) {
                return; // value irrelevant
            }

            var reader = TagParser.create(NbtOps.INSTANCE);
            Tag element;
            try {
                element = reader.parseFully(value);
            } catch (CommandSyntaxException e) {
                throw new InvalidConfigurationException("Could not read value for component %s".formatted(componentType), e);
            }
            type.codecOrThrow().parse(NbtOps.INSTANCE, element)
                    .getOrThrow(error -> new InvalidConfigurationException("Error parsing component: " + error));
        }

        public static ComponentChangeEntry getDefault() {
            return new ComponentChangeEntry(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.DAMAGE), ChangeType.SET, "0");
        }
    }

    public ItemComponents copy() {
        return new ItemComponents(itemId, List.copyOf(changes));
    }

    public static ItemComponents getDefault() {
        return new ItemComponents(BuiltInRegistries.ITEM.getKey(Items.WOODEN_SWORD), List.of());
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, changes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemComponents comps)) {
            return false;
        }

        return itemId.equals(comps.itemId) && changes.equals(comps.changes);
    }
}
