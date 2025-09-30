package net.rizecookey.combatedit.modification.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.utils.ReservedIdentifiers;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ItemModificationMap implements ItemModificationProvider {
    private final Map<Item, AttributeModifiersComponent> attributeMap;
    private final Map<Item, ComponentMap> componentMap;

    public ItemModificationMap(Map<Item, AttributeModifiersComponent> attributeMap, Map<Item, ComponentMap> componentMap) {
        this.attributeMap = Map.copyOf(attributeMap);
        this.componentMap = Map.copyOf(componentMap);
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers(Identifier id, Item item, AttributeModifiersComponent originalDefaults) {
        return attributeMap.getOrDefault(item, originalDefaults);
    }

    @Override
    public boolean shouldModifyAttributes(Identifier id, Item item) {
        return attributeMap.containsKey(item);
    }

    @Override
    public ComponentMap getComponents(Identifier id, Item item, ComponentMap originalDefaults) {
        return componentMap.getOrDefault(item, originalDefaults);
    }

    @Override
    public boolean shouldModifyDefaultComponents(Identifier id, Item item) {
        return componentMap.containsKey(item);
    }

    public static ItemModificationMap fromConfiguration(List<ItemAttributes> itemAttributes, List<ItemComponents> itemComponents, DefaultsSupplier.Items itemDefaultsSupplier) {
        Map<Item, AttributeModifiersComponent> attributeModifiers = new HashMap<>();
        Map<Item, ComponentMap> componentMap = new HashMap<>();
        Function<Item, AttributeModifiersComponent> defaultProvider = item -> {
            if (attributeModifiers.containsKey(item)) {
                return attributeModifiers.get(item);
            } else {
                return itemDefaultsSupplier.getVanillaAttributeModifiers(item);
            }
        };

        for (var attribute : itemAttributes) {
            var result = fromConfigurationEntry(attribute, defaultProvider);
            if (result != null) {
                attributeModifiers.put(result.getKey(), result.getValue());
            }
        }

        for (var components : itemComponents) {
            var result = fromConfigurationEntry(components, itemDefaultsSupplier::getVanillaComponents);
            if (result != null) {
                componentMap.put(result.getKey(), result.getValue());
            }
        }

        return new ItemModificationMap(attributeModifiers, componentMap);
    }

    private static @Nullable Map.Entry<Item, AttributeModifiersComponent> fromConfigurationEntry(ItemAttributes attributes, Function<Item, AttributeModifiersComponent> originalDefaults) {
        var builder = AttributeModifiersComponent.builder();
        if (!Registries.ITEM.containsId(attributes.getItemId())) {
            LOGGER.warn("No item with id {} found, skipping all attribute specifications", attributes.getItemId());
            return null;
        }

        var item = Registries.ITEM.get(attributes.getItemId());
        if (!attributes.isOverrideDefault()) {
            originalDefaults.apply(item).modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot()));
        }

        for (var entry : attributes.getModifiers()) {
            if (!Registries.ATTRIBUTE.containsId(entry.attribute())) {
                LOGGER.warn("No attribute with id {} found, skipping modifier", entry.attribute());
                continue;
            }

            var attribute = Registries.ATTRIBUTE.getEntry(entry.attribute()).orElseThrow();
            var modifier = new EntityAttributeModifier(entry.modifierId() != null ? entry.modifierId() : generateBasedOnIndex(attributes.getModifiers().indexOf(entry)), entry.value(), entry.operation());

            builder.add(attribute, modifier, entry.slot());
        }

        return Map.entry(item, builder.build());
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Map.Entry<Item, ComponentMap> fromConfigurationEntry(ItemComponents components, Function<Item, ComponentMap> originalDefaults) {
        if (!Registries.ITEM.containsId(components.getItemId())) {
            LOGGER.warn("No item with id {} found, skipping all component specifications", components.getItemId());
            return null;
        }

        var item = Registries.ITEM.get(components.getItemId());
        var builder = ComponentMap.builder();
        originalDefaults.apply(item).forEach(component -> builder.add((ComponentType<Object>) component.type(), component.value()));
        for (var entry : components.getChanges()) {
            if (!Registries.DATA_COMPONENT_TYPE.containsId(entry.componentType())) {
                LOGGER.warn("No component with id {} found, skipping component", entry.componentType());
                return null;
            }

            var componentType = Registries.DATA_COMPONENT_TYPE.get(entry.componentType());
            assert componentType != null;
            if (DataComponentTypes.ATTRIBUTE_MODIFIERS.equals(componentType)) {
                LOGGER.warn("Changing attribute modifiers via components not supported, use the attribute modifiers feature instead. Skipping component entry for {}", components.getItemId());
                return null;
            }

            if (entry.changeType().equals(ItemComponents.ChangeType.REMOVE)) {
                builder.add(componentType, null);
                continue;
            }

            if (Objects.equals(componentType.getCodec(), Unit.CODEC)) {
                builder.add((ComponentType<? super Unit>) componentType, Unit.INSTANCE);
                continue;
            }
            try {
                NbtElement nbtValue = StringNbtReader.fromOps(NbtOps.INSTANCE).read(entry.value());
                builder.add((ComponentType<Object>) componentType, componentType.getCodecOrThrow().parse(NbtOps.INSTANCE, nbtValue).getOrThrow());
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("Error parsing component for type %s".formatted(entry.componentType()), e);
            }
        }

        return Map.entry(item, builder.build());
    }

    public static Identifier generateBasedOnIndex(int index) {
        return Identifier.of(ReservedIdentifiers.RESERVED_NAMESPACE, "generated/" + (index + 1));
    }
}
