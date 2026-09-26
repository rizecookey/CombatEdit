package net.rizecookey.yacl3.extension.gui.controllers;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ActionController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class SubconfigButtonController<T> implements Controller<T> {
    private final Option<T> option;
    private final ActionController actionController;

    public SubconfigButtonController(Option<T> option) {
        this(option, option.name(), option.description());
    }

    public SubconfigButtonController(Option<T> option, Component buttonName) {
        this(option, buttonName, option.description());
    }

    public SubconfigButtonController(Option<T> option, Component buttonName, OptionDescription buttonDescription) {
        this.option = option;
        this.actionController = new ActionController(ButtonOption.createBuilder()
                .name(buttonName)
                .description(buttonDescription)
                .action((yaclScreen, _) -> Minecraft.getInstance().setScreenAndShow(createScreen(yaclScreen))).build()) {
            @Override
            public Component formatValue() {
                return SubconfigButtonController.this.formatValue();
            }
        };
    }

    @Override
    public Option<T> option() {
        return option;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return actionController.provideWidget(screen, widgetDimension);
    }

    public abstract Component formatValue();

    public abstract Screen createScreen(Screen previousScreen);
}
