package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.IdentifierController;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;
import net.rizecookey.yacl3.extension.gui.controllers.ValidatedController;
import org.jspecify.annotations.Nullable;

public class ModifierEntrySubconfigButtonController extends SubconfigButtonController<ItemAttributes.ModifierEntry> {
    private static final ItemAttributes.ModifierEntry DEFAULT = ItemAttributes.ModifierEntry.getDefault();

    public ModifierEntrySubconfigButtonController(Option<ItemAttributes.ModifierEntry> option) {
        super(option, Component.translatable("option.combatedit.item.item_attributes.modifier_entry.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.translatable("attribute.name." + option().pendingValue().attribute().toShortLanguageKey());
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.entry"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.entry"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.attribute"))
                                .binding(Binding.generic(
                                        DEFAULT.attribute(),
                                        () -> option().pendingValue().attribute(),
                                        value -> option().requestSet(option().pendingValue().withAttribute(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.ATTRIBUTE))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.modifier_id"))
                                .description(OptionDescription.of(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.modifier_id.tooltip")))
                                .binding(
                                        nullableIdentifierToString(DEFAULT.modifierId()),
                                        () -> nullableIdentifierToString(option().pendingValue().modifierId()),
                                        value -> {
                                            Identifier id = stringToNullableIdentifier(value);
                                            if (!validateModifierId(id)) {
                                                return;
                                            }

                                            option().requestSet(option().pendingValue().withModifierId(id));
                                        }
                                )
                                .customController(opt -> new ValidatedController<>(new IdentifierController(opt),
                                        value -> validateModifierId(stringToNullableIdentifier(value))))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.value"))
                                .binding(
                                        DEFAULT.value(),
                                        () -> option().pendingValue().value(),
                                        value -> option().requestSet(option().pendingValue().withValue(value))
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<AttributeModifier.Operation>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.operation"))
                                .binding(
                                        DEFAULT.operation(),
                                        () -> option().pendingValue().operation(),
                                        value -> option().requestSet(option().pendingValue().withOperation(value))
                                )
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(AttributeModifier.Operation.class)
                                        .formatValue(value -> Component.literal(value.getSerializedName())))
                                .build())
                        .option(Option.<EquipmentSlotGroup>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.slot"))
                                .binding(
                                        DEFAULT.slot(),
                                        () -> option().pendingValue().slot(),
                                        value -> option().requestSet(option().pendingValue().withSlot(value))
                                )
                                .controller(opt -> EnumDropdownControllerBuilder.create(opt)
                                        .formatValue(value -> Component.literal(value.getSerializedName())))
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }

    private boolean validateModifierId(Identifier modifierId) {
        ItemAttributes.ModifierEntry newValue = option().pendingValue().withModifierId(modifierId);
        try {
            newValue.validateModifierId();
        } catch (InvalidConfigurationException e) {
            return false;
        }

        return true;
    }

    private static @Nullable Identifier stringToNullableIdentifier(String value) {
        return value.isEmpty() ? null : Identifier.parse(value);
    }

    private static String nullableIdentifierToString(@Nullable Identifier identifier) {
        if (identifier == null) {
            return "";
        }

        return identifier.toString();
    }
}
