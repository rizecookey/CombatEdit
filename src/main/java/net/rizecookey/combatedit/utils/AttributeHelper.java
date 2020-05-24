package net.rizecookey.combatedit.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;

import java.util.Arrays;

public class AttributeHelper {
    public static Multimap<String, EntityAttributeModifier> getModifierData(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Multimap<String, EntityAttributeModifier> chosenMap = itemStack.getAttributeModifiers(equipmentSlot).isEmpty() ? itemStack.getItem().getModifiers(equipmentSlot) : itemStack.getAttributeModifiers(equipmentSlot);
        Multimap<String, EntityAttributeModifier> modifierMap = HashMultimap.create(chosenMap);
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
        if (sharpnessLevel > 0 && equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
            double sharpnessDamage = 1.0;
            sharpnessDamage += 0.5 * (sharpnessLevel - 1);
            EntityAttributeModifier[] modifiers = modifierMap.get(EntityAttributes.ATTACK_DAMAGE.getId()).toArray(new EntityAttributeModifier[0]);
            if (modifiers[0].getOperation().equals(EntityAttributeModifier.Operation.ADDITION)) {
                modifierMap.get(EntityAttributes.ATTACK_DAMAGE.getId()).clear();
                modifiers[0] = new EntityAttributeModifier(modifiers[0].getName(), modifiers[0].getAmount() + sharpnessDamage, EntityAttributeModifier.Operation.ADDITION);
                modifierMap.putAll(EntityAttributes.ATTACK_DAMAGE.getId(), Arrays.asList(modifiers));
            }
            else {
                modifierMap.get(EntityAttributes.ATTACK_DAMAGE.getId()).add(new EntityAttributeModifier("Sharpness damage", sharpnessDamage, EntityAttributeModifier.Operation.ADDITION));
            }
        }
        return modifierMap;
    }

    public static ItemStack fixAttributeData(ItemStack itemStack) {
        ItemStack modifiedStack = itemStack.copy();
        if (itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof MiningToolItem || itemStack.getItem() instanceof TridentItem) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                modifiedStack.getAttributeModifiers(equipmentSlot).clear();
                Multimap<String, EntityAttributeModifier> modifierMap = getModifierData(itemStack, equipmentSlot);
                for (String entityAttribute : modifierMap.keys()) {
                    for (EntityAttributeModifier modifier : modifierMap.get(entityAttribute)) {
                        modifiedStack.addAttributeModifier(entityAttribute, modifier, equipmentSlot);
                    }
                }
            }
        }
        return modifiedStack;
    }
}
