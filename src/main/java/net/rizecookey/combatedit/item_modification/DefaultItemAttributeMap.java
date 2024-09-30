package net.rizecookey.combatedit.item_modification;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

public class DefaultItemAttributeMap extends ItemAttributeMap {
    private static final Map<Class<? extends ToolItem>, Double> BASE_DAMAGE_TABLE = Map.of(
            SwordItem.class, 4.0,
            AxeItem.class, 3.0,
            PickaxeItem.class, 2.0,
            ShovelItem.class, 1.0,
            HoeItem.class, 2.0
    );

    private DefaultItemAttributeMap(Map<Item, AttributeModifiersComponent> attributeMap) {
        super(attributeMap);
    }

    public static DefaultItemAttributeMap create() {
        Map<Item, AttributeModifiersComponent> map = new HashMap<>();
        for (Item item : Registries.ITEM) {
            var clazzOverride = BASE_DAMAGE_TABLE.keySet().stream()
                    .filter(clazz -> clazz.isAssignableFrom(item.getClass())).findFirst();
            AttributeModifiersComponent modifiersComponent;
            if (clazzOverride.isEmpty() && !AttributeModifiersComponent.DEFAULT.equals((modifiersComponent = item.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT)))) {
                map.put(item, stripAttackSpeedModifiers(modifiersComponent));
                continue;
            }

            map.put(item, withDamageOnly(BASE_DAMAGE_TABLE.get(clazzOverride.orElseThrow())));
        }

        return new DefaultItemAttributeMap(map);
    }

    private static AttributeModifiersComponent withDamageOnly(double damage) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(
                        Item.ATTACK_DAMAGE_MODIFIER_ID,
                        "Tool modifier",
                        damage,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ), AttributeModifierSlot.MAINHAND)
                .build();
    }

    private static AttributeModifiersComponent stripAttackSpeedModifiers(AttributeModifiersComponent original) {
        if (original.modifiers().stream().noneMatch(modifier -> modifier.attribute().equals(EntityAttributes.GENERIC_ATTACK_SPEED))) {
            return original;
        }

        AttributeModifiersComponent.Builder newModifiers = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : original.modifiers()) {
            if (entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                continue;
            }

            newModifiers.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        return newModifiers.build();
    }
}
