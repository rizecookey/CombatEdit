package net.rizecookey.combatedit.mixins;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class PacketModiferMixin {
    @Environment(EnvType.SERVER)
    @Mixin(ClientConnection.class)
    public static class ClientConnectionMixin {
        @ModifyVariable(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), name = "packet", ordinal = 0)
        public Packet<?> sendModifyVariable(Packet<?> packet) {
            Packet<?> newPacket = packet;
            if (packet instanceof InventoryS2CPacket) {
                InventoryS2CPacket inventoryS2CPacket = (InventoryS2CPacket) packet;
                List<ItemStack> slotStackList = (List<ItemStack>) getFieldValue("field_12147", inventoryS2CPacket);
                List<ItemStack> modifiedStacks = DefaultedList.ofSize(slotStackList.size(), ItemStack.EMPTY);
                for (ItemStack itemStack : slotStackList) {
                    modifiedStacks.set(slotStackList.indexOf(itemStack), fixItemStackData(itemStack));
                }
                setFieldValue("slotStackList", inventoryS2CPacket, modifiedStacks);
                newPacket = inventoryS2CPacket;
            } else if (packet instanceof ContainerSlotUpdateS2CPacket) {
                ContainerSlotUpdateS2CPacket containerSlotUpdateS2CPacket = (ContainerSlotUpdateS2CPacket) packet;
                ItemStack itemStack = (ItemStack) getFieldValue("field_12153", containerSlotUpdateS2CPacket);
                setFieldValue("stack", containerSlotUpdateS2CPacket, fixItemStackData(itemStack));
                newPacket = containerSlotUpdateS2CPacket;
            }
            return newPacket;
        }

        @ModifyVariable(method = "handlePacket", at = @At("HEAD"), name = "packet", ordinal = 0)
        private static Packet<?> handlePacketModifyVariable(Packet<?> packet) {
            return packet;
        }

        private static Multimap<String, EntityAttributeModifier> getModifierData(ItemStack itemStack, EquipmentSlot equipmentSlot) {
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

        private static ItemStack fixItemStackData(ItemStack itemStack) {
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

        private static Object getFieldValue(String fieldName, Object object) {
            Object returned = null;
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                returned = field.get(object);
                field.setAccessible(accessible);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return returned;
        }

        private static void setFieldValue(String fieldName, Object object, Object fieldValue) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                field.set(object, fieldValue);
                field.setAccessible(accessible);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
