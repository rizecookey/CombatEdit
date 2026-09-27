package net.rizecookey.yacl3.extension.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

public record ValidatedController<T>(IStringController<T> controller, Predicate<String> validator) implements IStringController<T> {
    @Override
    public String getString() {
        return controller().getString();
    }

    @Override
    public void setFromString(String value) {
        controller().setFromString(value);
    }

    @Override
    public Option<T> option() {
        return controller().option();
    }

    @Override
    public Component formatValue() {
        return validator().test(getString())
                ? controller().formatValue()
                : controller().formatValue().copy().withStyle(ChatFormatting.RED);
    }
}
