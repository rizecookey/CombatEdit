package net.rizecookey.combatedit.client.configscreen.controller;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

import java.util.Objects;

public class ComponentChangeEntrySubconfigButtonController extends SubconfigButtonController<ItemComponents.ComponentChangeEntry> {
    private static final ItemComponents.ComponentChangeEntry DEFAULT = ItemComponents.ComponentChangeEntry.getDefault();

    public ComponentChangeEntrySubconfigButtonController(Option<ItemComponents.ComponentChangeEntry> option) {
        super(option, Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.literal(option().pendingValue().componentType().toString());
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.component"))
                                .binding(Binding.generic(
                                        DEFAULT.componentType(),
                                        () -> option().pendingValue().componentType(),
                                        value -> option().requestSet(option().pendingValue().withComponentType(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.DATA_COMPONENT_TYPE))
                                .build())
                        .option(Option.<ItemComponents.ChangeType>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type"))
                                .binding(
                                        DEFAULT.changeType(),
                                        () -> option().pendingValue().changeType(),
                                        value -> option().requestSet(option().pendingValue().withChangeType(value))
                                )
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(ItemComponents.ChangeType.class)
                                        .formatValue(ItemComponents.ChangeType::getText))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.item.item_components.component_change_entry.value"))
                                .binding(
                                        DEFAULT.value(),
                                        () -> Objects.requireNonNullElse(option().pendingValue().value(), ""),
                                        value -> {
                                            if (!validateComponentValue(value)) {
                                                return;
                                            }

                                            option().requestSet(option().pendingValue().withValue(value));
                                        }
                                )
                                .controller(StringControllerBuilder::create) // TODO formatting for invalid values
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }

    private boolean validateComponentValue(String value) {
        // TODO maybe more information about errors?
        if (option().pendingValue().changeType().equals(ItemComponents.ChangeType.REMOVE)) {
            return true;
        }

        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(option().pendingValue().componentType());
        if (type == null) return true;
        if (Unit.CODEC.equals(type.codec())) return true;

        var reader = TagParser.create(NbtOps.INSTANCE);
        Tag element;
        try {
            element = reader.parseFully(value);
        } catch (CommandSyntaxException e) {
            return false;
        }
        var result = type.codecOrThrow().parse(NbtOps.INSTANCE, element);
        return result.mapOrElse(
                _ -> true,
                e -> false);
    }
}
