package net.rizecookey.yacl3.extension.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

public record IdentifierController(Option<String> option) implements IStringController<String> {

    @Override
    public String getString() {
        return option().pendingValue();
    }

    @Override
    public void setFromString(String value) {
        option().requestSet(value);
    }

    @Override
    public boolean isInputValid(String input) {
        try {
            Identifier.parse(input);
            return true;
        } catch (IdentifierException e) {
            return false;
        }
    }
}
