package net.rizecookey.combatedit.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;

import java.util.Arrays;


public class AttributeHelper {
    public static final String ATTRIBUTE_TAG = "AttributeModifiers";
    public static final String ORIGINAL_ATTRIBUTE_TAG = "UnmodifiedAttributeModifiers";
    public static final String IS_PACKET_MODIFIED_TAG = "isPacketModified";

    public static Multimap<String, EntityAttributeModifier> getDisplayModifiers(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Multimap<String, EntityAttributeModifier> displayModifiers = MultimapBuilder.hashKeys().hashSetValues().build(itemStack.getAttributeModifiers(equipmentSlot));
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack.copy());
        if (sharpnessLevel > 0 && equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
            float sharpnessDamage = 1 + (sharpnessLevel - 1) * 0.5f;
            EntityAttributeModifier[] damageModifierArray = displayModifiers.get(EntityAttributes.ATTACK_DAMAGE.getId()).toArray(new EntityAttributeModifier[0]);
            int addSharpnessToIndex = 0;
            EntityAttributeModifier addSharpnessTo = null;
            for (int i = 0; i < damageModifierArray.length; i++) {
                if (damageModifierArray[i].getOperation().equals(EntityAttributeModifier.Operation.ADDITION)) {
                    addSharpnessToIndex = i;
                    addSharpnessTo = damageModifierArray[i];
                }
            }
            if (addSharpnessTo != null) {
                addSharpnessTo = new EntityAttributeModifier(addSharpnessTo.getName(), addSharpnessTo.getAmount() + sharpnessDamage, addSharpnessTo.getOperation());
                damageModifierArray[addSharpnessToIndex] = addSharpnessTo;
                displayModifiers.get(EntityAttributes.ATTACK_DAMAGE.getId()).clear();
                displayModifiers.putAll(EntityAttributes.ATTACK_DAMAGE.getId(), Arrays.asList(damageModifierArray));
            }
            else {
                displayModifiers.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier("Packet sharpness modificiation", 1 + (sharpnessLevel - 1) * 0.5, EntityAttributeModifier.Operation.ADDITION));
            }
        }
        return displayModifiers;
    }

    public static ItemStack changeDisplayModifiers(ItemStack itemStack) {
        ItemStack modifiedStack = itemStack.copy();
        if ((itemStack.getItem() instanceof SwordItem
                || itemStack.getItem() instanceof ToolItem
                || itemStack.getItem() instanceof TridentItem)
                && (!itemStack.hasTag()
                || !itemStack.getTag().contains(IS_PACKET_MODIFIED_TAG)
                || !itemStack.getTag().getBoolean(IS_PACKET_MODIFIED_TAG))) {
            modifiedStack.getOrCreateTag().putBoolean(IS_PACKET_MODIFIED_TAG, true);
            if (itemStack.hasTag() && itemStack.getTag().contains(ATTRIBUTE_TAG)) {
                modifiedStack.getTag().put(ORIGINAL_ATTRIBUTE_TAG, itemStack.getTag().get(ATTRIBUTE_TAG));
                modifiedStack.getTag().remove(ATTRIBUTE_TAG);
            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<String, EntityAttributeModifier> modifierMap = getDisplayModifiers(itemStack, equipmentSlot);
                for (String entityAttribute : modifierMap.keys()) {
                    for (EntityAttributeModifier modifier : modifierMap.get(entityAttribute)) {
                        modifiedStack.addAttributeModifier(entityAttribute, modifier, equipmentSlot);
                    }
                }
            }
        }
        return modifiedStack;
    }

    public static ItemStack reverseDisplayModifiers(ItemStack itemStack) {
        ItemStack reversedStack = itemStack.copy();
        if (itemStack.hasTag() && itemStack.getTag().contains(IS_PACKET_MODIFIED_TAG) && itemStack.getTag().getBoolean(IS_PACKET_MODIFIED_TAG)) {
            if (itemStack.getTag().contains(ORIGINAL_ATTRIBUTE_TAG)) {
                reversedStack.getTag().put(ATTRIBUTE_TAG, reversedStack.getTag().get(ORIGINAL_ATTRIBUTE_TAG));
                reversedStack.getTag().remove(ORIGINAL_ATTRIBUTE_TAG);
            }
            else {
                reversedStack.getTag().remove(ATTRIBUTE_TAG);
            }
            reversedStack.getTag().remove(IS_PACKET_MODIFIED_TAG);
        }
        return reversedStack;
    }
}
