package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;
import net.rizecookey.yacl3.extension.gui.controllers.ValidatedController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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

        var componentTypeOpt = Option.<String>createBuilder()
                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.component"))
                .binding(binding.xmap(
                        entry -> entry.componentType().toString(),
                        componentType -> binding.getValue().withComponentType(Identifier.parse(componentType))))
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .values(BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet().stream()
                                .filter(entry -> !entry.getValue().equals(DataComponents.ATTRIBUTE_MODIFIERS))
                                .map(Map.Entry::getKey)
                                .map(ResourceKey::identifier)
                                .map(Identifier::toString)
                                .toList()))
                .build();

        var changeTypeOpt = Option.<ItemComponents.ChangeType>createBuilder()
                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type"))
                .binding(binding.xmap(
                        ItemComponents.ComponentChangeEntry::changeType,
                        changeType -> binding.getValue().withChangeType(changeType)))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(ItemComponents.ChangeType.class)
                        .formatValue(ItemComponents.ChangeType::getText))
                .build();

        var valueOpt = Option.<String>createBuilder()
                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.value"))
                .binding(binding.xmap(
                        entry -> Objects.requireNonNullElse(entry.value(), ""),
                        value -> binding.getValue().withValue(value)))
                .customController(opt -> new ValidatedController<>(
                        new StringController(opt),
                        value -> validate(new ItemComponents.ComponentChangeEntry(
                                Identifier.parse(componentTypeOpt.pendingValue()),
                                changeTypeOpt.pendingValue(),
                                value))))
                .available(enableValueField(componentTypeOpt, changeTypeOpt))
                .build();

        Stream.of(componentTypeOpt, changeTypeOpt).forEach(opt ->
                opt.addEventListener((_, event) -> {
                    if (event != OptionEventListener.Event.STATE_CHANGE) {
                        return;
                    }
                    valueOpt.setAvailable(enableValueField(componentTypeOpt, changeTypeOpt));
                }));

        var opts = List.of(
                componentTypeOpt,
                changeTypeOpt,
                valueOpt
        );

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                        .options(opts)
                        .build())
                .save(() -> {
                    // need to check whether value matches the format for the component type
                    if (!validate(binding.getValue())) {
                        binding.setValue(option().pendingValue());
                        opts.forEach(Option::forgetPendingValue);
                        return;
                    }

                    option().requestSet(binding.getValue());
                })
                .build().generateScreen(previousScreen);
    }

    private static boolean enableValueField(Option<String> componentTypeOpt, Option<ItemComponents.ChangeType> changeTypeOpt) {
        if (!changeTypeOpt.pendingValue().equals(ItemComponents.ChangeType.SET)) {
            return false;
        }

        Identifier identifier = Identifier.parse(componentTypeOpt.pendingValue());
        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(identifier);
        return componentType == null || !Unit.CODEC.equals(componentType.codec());
    }

    private boolean validate(ItemComponents.ComponentChangeEntry entry) {
        try {
            entry.validateValue();
        } catch (InvalidConfigurationException e) {
            return false;
        }

        return true;
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
