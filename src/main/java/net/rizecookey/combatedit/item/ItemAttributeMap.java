package net.rizecookey.combatedit.item;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.ItemAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

        itemAttributes.stream()
                .map(attribute -> fromConfigurationEntry(attribute, originalDefaults))
                .filter(Objects::nonNull)
                .forEach(result -> map.put(result.getKey(), result.getValue()));

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
            var modifier = EntityAttributeModifier.fromNbt(entry.modifier());
            if (modifier == null) {
                LOGGER.warn("Modifier {} failed to parse, skipping", entry.modifier().asString());
                continue;
            }
            modifier = patchUUIDReference(modifier);

            builder.add(attribute, modifier, entry.slot());
        }

        return Map.entry(item, builder.build());
    }

    /*
    To make the tooltip appear green, the UUID's reference has to equal the corresponding constant in the Item class with ==.
    As the parsed UUID will never match that reference, we will patch this manually here.
     */
    private static EntityAttributeModifier patchUUIDReference(EntityAttributeModifier modifier) {
        UUID newUUID = null;
        if (modifier.uuid().equals(Item.ATTACK_DAMAGE_MODIFIER_ID)) {
            newUUID = Item.ATTACK_DAMAGE_MODIFIER_ID;
        } else if (modifier.uuid().equals(Item.ATTACK_SPEED_MODIFIER_ID)) {
            newUUID = Item.ATTACK_SPEED_MODIFIER_ID;
        }

        if (newUUID == null) {
            return modifier;
        }

        return new EntityAttributeModifier(newUUID, modifier.name(), modifier.value(), modifier.operation());
    }
}