package net.rizecookey.combatedit.modification.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.utils.ReservedResourceLocations;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ItemModificationMap implements ItemModificationProvider {
    private final Map<Item, ItemAttributeModifiers> attributeMap;
    private final Map<Item, DataComponentMap> componentMap;

    public ItemModificationMap(Map<Item, ItemAttributeModifiers> attributeMap, Map<Item, DataComponentMap> componentMap) {
        this.attributeMap = Map.copyOf(attributeMap);
        this.componentMap = Map.copyOf(componentMap);
    }

    @Override
    public ItemAttributeModifiers getAttributeModifiers(ResourceLocation id, Item item, ItemAttributeModifiers originalDefaults) {
        return attributeMap.getOrDefault(item, originalDefaults);
    }

    @Override
    public boolean shouldModifyAttributes(ResourceLocation id, Item item) {
        return attributeMap.containsKey(item);
    }

    @Override
    public DataComponentMap getComponents(ResourceLocation id, Item item, DataComponentMap originalDefaults) {
        return componentMap.getOrDefault(item, originalDefaults);
    }

    @Override
    public boolean shouldModifyDefaultComponents(ResourceLocation id, Item item) {
        return componentMap.containsKey(item);
    }

    public static ItemModificationMap fromConfiguration(List<ItemAttributes> itemAttributes, List<ItemComponents> itemComponents, DefaultsSupplier.Items itemDefaultsSupplier) {
        Map<Item, ItemAttributeModifiers> attributeModifiers = new HashMap<>();
        Map<Item, DataComponentMap> componentMap = new HashMap<>();
        Function<Item, ItemAttributeModifiers> defaultProvider = item -> {
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

    private static @Nullable Map.Entry<Item, ItemAttributeModifiers> fromConfigurationEntry(ItemAttributes attributes, Function<Item, ItemAttributeModifiers> originalDefaults) {
        var builder = ItemAttributeModifiers.builder();
        if (!BuiltInRegistries.ITEM.containsKey(attributes.getItemId())) {
            LOGGER.warn("No item with id {} found, skipping all attribute specifications", attributes.getItemId());
            return null;
        }

        var item = BuiltInRegistries.ITEM.getValue(attributes.getItemId());
        if (!attributes.isOverrideDefault()) {
            originalDefaults.apply(item).modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot()));
        }

        for (var entry : attributes.getModifiers()) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(entry.attribute())) {
                LOGGER.warn("No attribute with id {} found, skipping modifier", entry.attribute());
                continue;
            }

            var attribute = BuiltInRegistries.ATTRIBUTE.get(entry.attribute()).orElseThrow();
            var modifier = new AttributeModifier(entry.modifierId() != null ? entry.modifierId() : generateBasedOnIndex(attributes.getModifiers().indexOf(entry)), entry.value(), entry.operation());

            builder.add(attribute, modifier, entry.slot());
        }

        return Map.entry(item, builder.build());
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Map.Entry<Item, DataComponentMap> fromConfigurationEntry(ItemComponents components, Function<Item, DataComponentMap> originalDefaults) {
        if (!BuiltInRegistries.ITEM.containsKey(components.getItemId())) {
            LOGGER.warn("No item with id {} found, skipping all component specifications", components.getItemId());
            return null;
        }

        var item = BuiltInRegistries.ITEM.getValue(components.getItemId());
        var builder = DataComponentMap.builder().combatEdit$preventDynamicWrap();
        originalDefaults.apply(item).forEach(component -> builder.set((DataComponentType<Object>) component.type(), component.value()));
        for (var entry : components.getChanges()) {
            if (!BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(entry.componentType())) {
                LOGGER.warn("No component with id {} found, skipping component", entry.componentType());
                return null;
            }

            var componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(entry.componentType());
            assert componentType != null;
            if (DataComponents.ATTRIBUTE_MODIFIERS.equals(componentType)) {
                LOGGER.warn("Changing attribute modifiers via components not supported, use the attribute modifiers feature instead. Skipping component entry for {}", components.getItemId());
                return null;
            }

            if (entry.changeType().equals(ItemComponents.ChangeType.REMOVE)) {
                builder.set(componentType, null);
                continue;
            }

            if (Objects.equals(componentType.codec(), Unit.CODEC)) {
                builder.set((DataComponentType<? super Unit>) componentType, Unit.INSTANCE);
                continue;
            }
            try {
                Tag nbtValue = TagParser.create(NbtOps.INSTANCE).parseFully(entry.value());
                builder.set((DataComponentType<Object>) componentType, componentType.codecOrThrow().parse(NbtOps.INSTANCE, nbtValue).getOrThrow());
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("Error parsing component for type %s".formatted(entry.componentType()), e);
            }
        }

        return Map.entry(item, builder.build());
    }

    public static ResourceLocation generateBasedOnIndex(int index) {
        return ResourceLocation.fromNamespaceAndPath(ReservedResourceLocations.RESERVED_NAMESPACE, "generated/" + (index + 1));
    }
}
