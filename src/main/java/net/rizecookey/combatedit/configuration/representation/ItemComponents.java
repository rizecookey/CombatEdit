package net.rizecookey.combatedit.configuration.representation;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemComponents {
    private Identifier itemId;
    private List<ComponentChangeEntry> changes;

    public ItemComponents(Identifier itemId, List<ComponentChangeEntry> changes) {
        this.itemId = itemId;
        this.changes = new ArrayList<>(changes);
    }

    protected ItemComponents() {}

    /**
     * Returns the identifier of the item to be modified.
     * @return the identifier of the item to be modified
     */
    public Identifier getItemId() {
        return itemId;
    }

    public void setItemId(Identifier itemId) {
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
        if (itemId == null || !Registries.ITEM.containsId(itemId)) {
            throw new InvalidConfigurationException("No item with id %s found".formatted(itemId));
        }

        for (var component : getChanges()) {
            component.validate();
        }
    }

    /**
     * A component change entry for an item.
     *
     * @param componentType the identifier of the component to be changed
     * @param value the value to use for this component, or {@code null} if the component type should be removed from
     *              the default components of the item
     */
    public record ComponentChangeEntry(Identifier componentType, @Nullable String value) {
        public void validate() throws InvalidConfigurationException {
            if (!Registries.DATA_COMPONENT_TYPE.containsId(componentType)) {
                throw new InvalidConfigurationException("Unknown component id");
            }

            if (value == null) {
                return;
            }

            ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(componentType);
            assert type != null;
            var reader = StringNbtReader.fromOps(NbtOps.INSTANCE);
            NbtElement element;
            try {
                element = reader.read(value);
            } catch (CommandSyntaxException e) {
                throw new InvalidConfigurationException(e);
            }
            type.getCodecOrThrow().parse(NbtOps.INSTANCE, element)
                    .getOrThrow(error -> new InvalidConfigurationException("Error parsing component: " + error));
        }

        public static ComponentChangeEntry getDefault() {
            return new ComponentChangeEntry(Registries.DATA_COMPONENT_TYPE.getId(DataComponentTypes.DAMAGE), "0");
        }
    }

    public ItemComponents copy() {
        return new ItemComponents(itemId, List.copyOf(changes));
    }

    public static ItemComponents getDefault() {
        return new ItemComponents(Registries.ITEM.getId(Items.WOODEN_SWORD), List.of());
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
