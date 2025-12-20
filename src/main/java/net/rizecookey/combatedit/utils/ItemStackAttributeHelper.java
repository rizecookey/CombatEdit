package net.rizecookey.combatedit.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;

public class ItemStackAttributeHelper {
    private static final String ORIGINAL_ATTRIBUTE_TAG = "combatedit:original_attribute_modifiers";
    private static final String IS_PACKET_MODIFIED_TAG = "combatedit:is_packet_modified";

    private static final Identifier SHARPNESS_MODIFIER_ID = Identifier.fromNamespaceAndPath(ReservedIdentifiers.RESERVED_NAMESPACE, "sharpness_modifier");

    private final ConfigurationManager configurationProvider;

    public ItemStackAttributeHelper(ConfigurationManager configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public ItemAttributeModifiers getDisplayModifiers(ItemStack itemStack) {
        var itemModifiers = configurationProvider.getModifier().items().modificationProvider();
        Item item = itemStack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (!itemModifiers.shouldModifyAttributes(id, item)) {
            return null;
        }

        ItemAttributeModifiers modifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return null;
        }

        var sharpnessRegistryEntry = configurationProvider.getCurrentServer().overworld().registryAccess()
                .lookupOrThrow(Enchantments.SHARPNESS.registryKey()).get(Enchantments.SHARPNESS.identifier()).orElseThrow();
        int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(sharpnessRegistryEntry, itemStack);
        double sharpnessDamage = 1 + (sharpnessLevel - 1) * 0.5;
        boolean shouldAddSharpnessModifier = sharpnessLevel > 0;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Identifier modifierId = getSafeIdentifier(entry.modifier().id());
            if (shouldAddSharpnessModifier && entry.attribute().equals(Attributes.ATTACK_DAMAGE) && entry.slot().equals(EquipmentSlotGroup.MAINHAND) && entry.modifier().operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                // add sharpness damage display modifier onto this modifier (vanilla doesn't add sharpness to the display on its own)
                shouldAddSharpnessModifier = false;
                builder.add(entry.attribute(), new AttributeModifier(modifierId, entry.modifier().amount() + sharpnessDamage, entry.modifier().operation()), entry.slot());
                continue;
            }
            builder.add(entry.attribute(), new AttributeModifier(modifierId, entry.modifier().amount(), entry.modifier().operation()), entry.slot());
        }

        if (shouldAddSharpnessModifier) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(SHARPNESS_MODIFIER_ID, sharpnessDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }

        return builder.build();
    }

    public ItemStack getDisplayModified(ItemStack itemStack) {
        var itemModifiers = configurationProvider.getModifier().items().modificationProvider();
        Item item = itemStack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        boolean shouldModifyAttributes = itemModifiers.shouldModifyAttributes(id, item);
        boolean shouldModifyComponents = itemModifiers.shouldModifyDefaultComponents(id, item);
        if (!shouldModifyAttributes && !shouldModifyComponents) {
            return itemStack;
        }

        CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.getBoolean(IS_PACKET_MODIFIED_TAG).orElse(false)) {
            return itemStack;
        }

        ItemStack modified = itemStack.copy();
        modified.combatEdit$useOriginalComponentMapAsBase();
        nbt.putBoolean(IS_PACKET_MODIFIED_TAG, true);

        if (shouldModifyAttributes) {
            ItemAttributeModifiers component = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            nbt.store(ORIGINAL_ATTRIBUTE_TAG, ItemAttributeModifiers.CODEC, component);
            modified.set(DataComponents.ATTRIBUTE_MODIFIERS, getDisplayModifiers(itemStack));
        }
        modified.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        return modified;
    }

    public ItemStack reverseDisplayModifiers(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.getBoolean(IS_PACKET_MODIFIED_TAG).orElse(false)) {
            return itemStack;
        }

        ItemStack reversed = itemStack.copy();
        if (nbt.contains(ORIGINAL_ATTRIBUTE_TAG)) {
            reversed.set(DataComponents.ATTRIBUTE_MODIFIERS, nbt.read(ORIGINAL_ATTRIBUTE_TAG, ItemAttributeModifiers.CODEC).orElseThrow());
            nbt.remove(ORIGINAL_ATTRIBUTE_TAG);
        }
        nbt.remove(IS_PACKET_MODIFIED_TAG);

        if (!nbt.isEmpty()) {
            reversed.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        } else {
            reversed.remove(DataComponents.CUSTOM_DATA);
        }
        return reversed;
    }

    private static Identifier getSafeIdentifier(Identifier identifier) {
        if (identifier.equals(Item.BASE_ATTACK_DAMAGE_ID)) {
            return ReservedIdentifiers.ATTACK_DAMAGE_MODIFIER_ID_ALT;
        }
        if (identifier.equals(Item.BASE_ATTACK_SPEED_ID)) {
            return ReservedIdentifiers.ATTACK_SPEED_MODIFIER_ID_ALT;
        }

        return identifier;
    }
}
