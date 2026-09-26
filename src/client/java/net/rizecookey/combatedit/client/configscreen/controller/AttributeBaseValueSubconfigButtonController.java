package net.rizecookey.combatedit.client.configscreen.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

public class AttributeBaseValueSubconfigButtonController extends SubconfigButtonController<EntityAttributes.AttributeBaseValue> {
    private static final EntityAttributes.AttributeBaseValue DEFAULT = EntityAttributes.AttributeBaseValue.getDefault();

    public AttributeBaseValueSubconfigButtonController(Option<EntityAttributes.AttributeBaseValue> option) {
        super(option, Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.translatable("attribute.name." + option().pendingValue().attribute().toShortLanguageKey());
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.attribute"))
                                .binding(Binding.generic(
                                        DEFAULT.attribute(),
                                        () -> option().pendingValue().attribute(),
                                        value -> option().requestSet(option().pendingValue().withAttribute(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.ATTRIBUTE))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.base_value"))
                                .binding(
                                        DEFAULT.baseValue(),
                                        () -> option().pendingValue().baseValue(),
                                        value -> option().requestSet(option().pendingValue().withBaseValue(value))
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }
}
