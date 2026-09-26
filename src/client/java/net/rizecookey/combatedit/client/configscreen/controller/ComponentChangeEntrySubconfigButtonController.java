package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

import java.util.List;
import java.util.Objects;

public class ComponentChangeEntrySubconfigButtonController extends SubconfigButtonController<ItemComponents.ComponentChangeEntry> {
    private static final ItemComponents.ComponentChangeEntry DEFAULT = ItemComponents.ComponentChangeEntry.getDefault();

    private ItemComponents.ComponentChangeEntry intermittentValue;
    private final Binding<ItemComponents.ComponentChangeEntry> binding = Binding.generic(
            DEFAULT,
            () -> intermittentValue,
            value -> intermittentValue = value
    );

    public ComponentChangeEntrySubconfigButtonController(Option<ItemComponents.ComponentChangeEntry> option) {
        super(option, Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"));

        intermittentValue = option().pendingValue();
    }

    @Override
    public Component formatValue() {
        return Component.literal(option().pendingValue().componentType().toString());
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        var binding = new IntermittentBinding(option().pendingValue(), DEFAULT);

        var opts = List.of(
                Option.<String>createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.component"))
                        .binding(binding.xmap(
                                entry -> entry.componentType().toString(),
                                componentType -> binding.getValue().withComponentType(Identifier.parse(componentType))))
                        .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.DATA_COMPONENT_TYPE))
                        .build(),
                Option.<ItemComponents.ChangeType>createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type"))
                        .binding(binding.xmap(
                                ItemComponents.ComponentChangeEntry::changeType,
                                changeType -> binding.getValue().withChangeType(changeType)))
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(ItemComponents.ChangeType.class)
                                .formatValue(ItemComponents.ChangeType::getText))
                        .build(),
                Option.<String>createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.value"))
                        .binding(binding.xmap(
                                entry -> Objects.requireNonNullElse(entry.value(), ""),
                                value -> binding.getValue().withValue(value)))
                        .controller(StringControllerBuilder::create) // TODO formatting for invalid values
                        .build()
        );

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                        .options(opts)
                        .build())
                .save(() -> {
                    // need to check whether value matches the format for the component type
                    try {
                        binding.getValue().validateValue();
                    } catch (InvalidConfigurationException e) {
                        binding.setValue(option().pendingValue());
                        opts.forEach(Option::forgetPendingValue);
                        return;
                    }

                    option().requestSet(binding.getValue());
                })
                .build().generateScreen(previousScreen);
    }

    private static class IntermittentBinding implements Binding<ItemComponents.ComponentChangeEntry> {
        private ItemComponents.ComponentChangeEntry value;
        private final ItemComponents.ComponentChangeEntry defaultValue;

        public IntermittentBinding(ItemComponents.ComponentChangeEntry value, ItemComponents.ComponentChangeEntry defaultValue) {
            this.value = value;
            this.defaultValue = defaultValue;
        }

        @Override
        public void setValue(ItemComponents.ComponentChangeEntry value) {
            this.value = value;
        }

        @Override
        public ItemComponents.ComponentChangeEntry getValue() {
            return value;
        }

        @Override
        public ItemComponents.ComponentChangeEntry defaultValue() {
            return defaultValue;
        }
    }
}
