package net.rizecookey.combatedit.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Identifier;

import java.util.Map;

public class DefaultItemAttributeModifiers implements ItemAttributeModifierProvider {
    private static final Map<Class<? extends ToolItem>, Double> BASE_DAMAGE_TABLE = Map.of(
            SwordItem.class, 4.0,
            AxeItem.class, 3.0,
            PickaxeItem.class, 2.0,
            ShovelItem.class, 1.0,
            HoeItem.class, 2.0
    );

    @Override
    public AttributeModifiersComponent getModifiers(Identifier id, Item item) {
        if (!(item instanceof SwordItem) && !(item instanceof MiningToolItem)) {
            return stripAttackSpeedModifiers(item);
        }

        ToolItem toolItem = (ToolItem) item;
        return withDamageOnly(toolItem.getMaterial().getAttackDamage() + BASE_DAMAGE_TABLE.getOrDefault(item.getClass(), 0.0));
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

    private static AttributeModifiersComponent stripAttackSpeedModifiers(Item item) {
        AttributeModifiersComponent original = item.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
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

    @Override
    public boolean shouldModifyItem(Identifier id, Item item) {
        return item instanceof MiningToolItem || item instanceof SwordItem || item instanceof TridentItem
                || item.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()
                .stream().anyMatch(modifier -> modifier.attribute().equals(EntityAttributes.GENERIC_ATTACK_SPEED));
    }
}
