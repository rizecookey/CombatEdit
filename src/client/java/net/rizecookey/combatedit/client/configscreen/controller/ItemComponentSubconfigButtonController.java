package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

public class ItemComponentSubconfigButtonController extends SubconfigButtonController<ItemComponents> {
    private static final ItemComponents DEFAULT = ItemComponents.getDefault();

    public ItemComponentSubconfigButtonController(Option<ItemComponents> option) {
        super(option, Component.translatable("option.combatedit.item.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.translatable(option().pendingValue().getItemId().toLanguageKey("item"));
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_components"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_attributes.item"))
                                .binding(Binding.generic(
                                        DEFAULT.getItemId(),
                                        () -> option().pendingValue().getItemId(),
                                        value -> option().requestSet(option().pendingValue().withItemId(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.ITEM))
                                .build())
                        .group(ListOption.<ItemComponents.ComponentChangeEntry>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry"))
                                .binding(
                                        DEFAULT.getChanges(),
                                        () -> option().pendingValue().getChanges(),
                                        value -> option().requestSet(option().pendingValue().withChanges(value))
                                )
                                .initial(ItemComponents.ComponentChangeEntry::getDefault)
                                .customController(ComponentChangeEntrySubconfigButtonController::new)
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }
}
