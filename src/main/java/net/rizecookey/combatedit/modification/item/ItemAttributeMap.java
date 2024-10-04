package net.rizecookey.combatedit.modification.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.utils.ReservedIdentifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class ItemAttributeMap implements ItemAttributeModifierProvider {
    private final Map<Item, AttributeModifiersComponent> attributeMap;

    public ItemAttributeMap(Map<Item, AttributeModifiersComponent> attributeMap) {
        this.attributeMap = Map.copyOf(attributeMap);
    }

    @Override
    public AttributeModifiersComponent getModifiers(Identifier id, Item item, AttributeModifiersComponent originalDefaults) {
        return attributeMap.get(item);
    }

    @Override
    public boolean shouldModifyItem(Identifier id, Item item) {
        return attributeMap.containsKey(item);
    }

    public static ItemAttributeMap fromConfiguration(List<ItemAttributes> itemAttributes, Function<Item, AttributeModifiersComponent> originalDefaults) {
        Map<Item, AttributeModifiersComponent> map = new HashMap<>();
        Function<Item, AttributeModifiersComponent> defaultProvider = item -> {
            if (map.containsKey(item)) {
                return map.get(item);
            } else {
                return originalDefaults.apply(item);
            }
        };

        for (var attribute : itemAttributes) {
            var result = fromConfigurationEntry(attribute, defaultProvider);
            if (result != null) {
                map.put(result.getKey(), result.getValue());
            }
        }

        return new ItemAttributeMap(map);
    }

    private static Map.Entry<Item, AttributeModifiersComponent> fromConfigurationEntry(ItemAttributes attributes, Function<Item, AttributeModifiersComponent> originalDefaults) {
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

    public static Identifier generateBasedOnIndex(int index) {
        return Identifier.of(ReservedIdentifiers.RESERVED_NAMESPACE, "generated/" + (index + 1));
    }
}
