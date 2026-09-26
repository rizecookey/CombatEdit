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
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.yacl3.extension.api.DropdownIdentifierControllerFactory;
import net.rizecookey.yacl3.extension.gui.controllers.SubconfigButtonController;

public class EntityAttributeSubconfigButtonController extends SubconfigButtonController<EntityAttributes> {
    private static final EntityAttributes DEFAULT = EntityAttributes.getDefault();

    public EntityAttributeSubconfigButtonController(Option<EntityAttributes> option) {
        super(option, Component.translatable("option.combatedit.entity.entry"));
    }

    @Override
    public Component formatValue() {
        return Component.translatable(option().pendingValue().getEntityId().toLanguageKey("entity"));
    }

    @Override
    public Screen createScreen(Screen previousScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("option.combatedit.entity.entity_attributes"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("option.combatedit.entity.entity_attributes"))
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("option.combatedit.entity.entity_attributes.entity"))
                                .binding(Binding.generic(
                                        DEFAULT.getEntityId(),
                                        () -> option().pendingValue().getEntityId(),
                                        value -> option().requestSet(option().pendingValue().withEntityId(value))
                                ).xmap(Identifier::toString, Identifier::parse))
                                .controller(opt -> DropdownIdentifierControllerFactory.create(opt, BuiltInRegistries.ENTITY_TYPE))
                                .build())
                        .group(ListOption.<EntityAttributes.AttributeBaseValue>createBuilder()
                                .name(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry"))
                                .binding(
                                        DEFAULT.getBaseValues(),
                                        () -> option().pendingValue().getBaseValues(),
                                        newList -> option().requestSet(option().pendingValue().withBaseValues(newList))
                                )
                                .initial(EntityAttributes.AttributeBaseValue::getDefault)
                                .customController(AttributeBaseValueSubconfigButtonController::new)
                                .build())
                        .build())
                .build().generateScreen(previousScreen);
    }
}
