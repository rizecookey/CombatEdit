package net.rizecookey.combatedit.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;
import net.rizecookey.combatedit.extension.EntityAttributeModifierExtension;

import java.util.Arrays;


public class AttributeHelper {
    public static final String ATTRIBUTE_TAG = "AttributeModifiers";
    public static final String ORIGINAL_ATTRIBUTE_TAG = "UnmodifiedAttributeModifiers";
    public static final String IS_PACKET_MODIFIED_TAG = "isPacketModified";

    public static Multimap<EntityAttribute, EntityAttributeModifier> getDisplayModifiers(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Multimap<EntityAttribute, EntityAttributeModifier> displayModifiers = MultimapBuilder.hashKeys().hashSetValues().build(itemStack.getAttributeModifiers(equipmentSlot));
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack.copy());
        if (sharpnessLevel > 0 && equipmentSlot.equals(EquipmentSlot.MAINHAND)) {
            float sharpnessDamage = 1 + (sharpnessLevel - 1) * 0.5f;
            EntityAttributeModifier[] damageModifierArray = displayModifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE).toArray(new EntityAttributeModifier[0]);
            int addSharpnessToIndex = 0;
            EntityAttributeModifier addSharpnessTo = null;
            for (int i = 0; i < damageModifierArray.length; i++) {
                if (damageModifierArray[i].getOperation().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                    addSharpnessToIndex = i;
                    addSharpnessTo = damageModifierArray[i];
                }
            }
            if (addSharpnessTo != null) {
                addSharpnessTo = new EntityAttributeModifier(((EntityAttributeModifierExtension) addSharpnessTo).getName(),
                        addSharpnessTo.getValue() + sharpnessDamage, addSharpnessTo.getOperation());
                damageModifierArray[addSharpnessToIndex] = addSharpnessTo;
                displayModifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE).clear();
                displayModifiers.putAll(EntityAttributes.GENERIC_ATTACK_DAMAGE, Arrays.asList(damageModifierArray));
            }
            else {
                displayModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier("Packet sharpness modification", 1 + (sharpnessLevel - 1) * 0.5, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
        return displayModifiers;
    }

    public static ItemStack changeDisplayModifiers(ItemStack itemStack) {
        ItemStack modifiedStack = itemStack.copy();
        if ((itemStack.getItem() instanceof SwordItem
                || itemStack.getItem() instanceof ToolItem
                || itemStack.getItem() instanceof TridentItem)
                && (!itemStack.hasNbt()
                || !itemStack.getNbt().contains(IS_PACKET_MODIFIED_TAG)
                || !itemStack.getNbt().getBoolean(IS_PACKET_MODIFIED_TAG))) {
            modifiedStack.getOrCreateNbt().putBoolean(IS_PACKET_MODIFIED_TAG, true);
            if (itemStack.hasNbt() && itemStack.getNbt().contains(ATTRIBUTE_TAG)) {
                modifiedStack.getNbt().put(ORIGINAL_ATTRIBUTE_TAG, itemStack.getNbt().get(ATTRIBUTE_TAG));
                modifiedStack.getNbt().remove(ATTRIBUTE_TAG);
            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<EntityAttribute, EntityAttributeModifier> modifierMap = getDisplayModifiers(itemStack, equipmentSlot);
                for (EntityAttribute entityAttribute : modifierMap.keys()) {
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
        if (itemStack.hasNbt() && itemStack.getNbt().contains(IS_PACKET_MODIFIED_TAG) && itemStack.getNbt().getBoolean(IS_PACKET_MODIFIED_TAG)) {
            if (itemStack.getNbt().contains(ORIGINAL_ATTRIBUTE_TAG)) {
                reversedStack.getNbt().put(ATTRIBUTE_TAG, reversedStack.getNbt().get(ORIGINAL_ATTRIBUTE_TAG));
                reversedStack.getNbt().remove(ORIGINAL_ATTRIBUTE_TAG);
            }
            else {
                reversedStack.getNbt().remove(ATTRIBUTE_TAG);
            }
            reversedStack.getNbt().remove(IS_PACKET_MODIFIED_TAG);
        }
        return reversedStack;
    }
}
