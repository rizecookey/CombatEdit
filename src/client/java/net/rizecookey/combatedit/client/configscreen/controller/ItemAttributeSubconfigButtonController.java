package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

public class ItemAttributeSubconfigButtonController extends SubconfigButtonController<ItemAttributes> {
    private static final ItemAttributes DEFAULT = ItemAttributes.getDefault();

    public ItemAttributeSubconfigButtonController(Option<ItemAttributes> option) {
        super(option, Component.translatable("option.combatedit.item.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.translatable(option().pendingValue().getItemId().toLanguageKey("item"));
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_attributes"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_attributes"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.item"))
                                .binding(Binding.generic(
                                        DEFAULT.getItemId(),
                                        () -> option().pendingValue().getItemId(),
                                        value -> option().requestSet(option().pendingValue().withItemId(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.ITEM))
                                .build())
                        .group(ListOption.<ItemAttributes.ModifierEntry>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.modifier_entry"))
                                .binding(
                                        DEFAULT.getModifiers(),
                                        () -> option().pendingValue().getModifiers(),
                                        value -> option().requestSet(option().pendingValue().withModifiers(value))
                                )
                                .initial(ItemAttributes.ModifierEntry::getDefault)
                                .customController(ModifierEntrySubconfigButtonController::new)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.override_defaults"))
                                .binding(
                                        DEFAULT.isOverrideDefault(),
                                        () -> option().pendingValue().isOverrideDefault(),
                                        value -> option().requestSet(option().pendingValue().withOverrideDefault(value))
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }
}
