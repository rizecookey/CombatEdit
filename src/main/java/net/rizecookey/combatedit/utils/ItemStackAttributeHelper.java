package net.rizecookey.combatedit.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;

public class ItemStackAttributeHelper {
    private static final String ORIGINAL_ATTRIBUTE_TAG = "combatedit:original_attribute_modifiers";
    private static final String IS_PACKET_MODIFIED_TAG = "combatedit:is_packet_modified";

    private static final Identifier SHARPNESS_MODIFIER_ID = Identifier.of(ReservedIdentifiers.RESERVED_NAMESPACE, "sharpness_modifier");

    private final ConfigurationManager configurationProvider;

    public ItemStackAttributeHelper(ConfigurationManager configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public AttributeModifiersComponent getDisplayModifiers(ItemStack itemStack) {
        var itemModifiers = configurationProvider.getModifier().items().modificationProvider();
        Item item = itemStack.getItem();
        Identifier id = Registries.ITEM.getId(item);
        if (!itemModifiers.shouldModifyAttributes(id, item)) {
            return null;
        }

        AttributeModifiersComponent modifiers = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, null);
        if (modifiers == null) {
            return null;
        }

        var sharpnessRegistryEntry = configurationProvider.getCurrentServer().getOverworld().getRegistryManager()
                .getOrThrow(Enchantments.SHARPNESS.getRegistryRef()).getEntry(Enchantments.SHARPNESS.getValue()).orElseThrow();
        int sharpnessLevel = EnchantmentHelper.getLevel(sharpnessRegistryEntry, itemStack);
        double sharpnessDamage = 1 + (sharpnessLevel - 1) * 0.5;
        boolean shouldAddSharpnessModifier = sharpnessLevel > 0;

        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
            Identifier modifierId = getSafeIdentifier(entry.modifier().id());
            if (shouldAddSharpnessModifier && entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE) && entry.slot().equals(AttributeModifierSlot.MAINHAND) && entry.modifier().operation().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                // add sharpness damage display modifier onto this modifier (vanilla doesn't add sharpness to the display on its own)
                shouldAddSharpnessModifier = false;
                builder.add(entry.attribute(), new EntityAttributeModifier(modifierId, entry.modifier().value() + sharpnessDamage, entry.modifier().operation()), entry.slot());
                continue;
            }
            builder.add(entry.attribute(), new EntityAttributeModifier(modifierId, entry.modifier().value(), entry.modifier().operation()), entry.slot());
        }

        if (shouldAddSharpnessModifier) {
            builder.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(SHARPNESS_MODIFIER_ID, sharpnessDamage, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND);
        }

        return builder.build();
    }

    public ItemStack getDisplayModified(ItemStack itemStack) {
        var itemModifiers = configurationProvider.getModifier().items().modificationProvider();
        Item item = itemStack.getItem();
        Identifier id = Registries.ITEM.getId(item);
        boolean shouldModifyAttributes = itemModifiers.shouldModifyAttributes(id, item);
        boolean shouldModifyComponents = itemModifiers.shouldModifyDefaultComponents(id, item);
        if (!shouldModifyAttributes && !shouldModifyComponents) {
            return itemStack;
        }

        NbtCompound nbt = itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbt.getBoolean(IS_PACKET_MODIFIED_TAG).orElse(false)) {
            return itemStack;
        }

        ItemStack modified = itemStack.copy();
        modified.combatEdit$useOriginalComponentMapAsBase();
        nbt.putBoolean(IS_PACKET_MODIFIED_TAG, true);

        if (shouldModifyAttributes) {
            AttributeModifiersComponent component = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            nbt.put(ORIGINAL_ATTRIBUTE_TAG, AttributeModifiersComponent.CODEC, component);
            modified.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDisplayModifiers(itemStack));
        }
        modified.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return modified;
    }

    public ItemStack reverseDisplayModifiers(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (!nbt.getBoolean(IS_PACKET_MODIFIED_TAG).orElse(false)) {
            return itemStack;
        }

        ItemStack reversed = itemStack.copy();
        if (nbt.contains(ORIGINAL_ATTRIBUTE_TAG)) {
            reversed.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, nbt.get(ORIGINAL_ATTRIBUTE_TAG, AttributeModifiersComponent.CODEC).orElseThrow());
            nbt.remove(ORIGINAL_ATTRIBUTE_TAG);
        }
        nbt.remove(IS_PACKET_MODIFIED_TAG);

        if (!nbt.isEmpty()) {
            reversed.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        } else {
            reversed.remove(DataComponentTypes.CUSTOM_DATA);
        }
        return reversed;
    }

    private static Identifier getSafeIdentifier(Identifier identifier) {
        if (identifier.equals(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
            return ReservedIdentifiers.ATTACK_DAMAGE_MODIFIER_ID_ALT;
        }
        if (identifier.equals(Item.BASE_ATTACK_SPEED_MODIFIER_ID)) {
            return ReservedIdentifiers.ATTACK_SPEED_MODIFIER_ID_ALT;
        }

        return identifier;
    }
}
