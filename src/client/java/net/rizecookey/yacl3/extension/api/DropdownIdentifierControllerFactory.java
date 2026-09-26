package net.rizecookey.yacl3.extension.api;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class DropdownIdentifierControllerFactory {
    private DropdownIdentifierControllerFactory() {}

    public static <T> DropdownStringControllerBuilder create(Option<String> option, Registry<T> registry) {
        return DropdownStringControllerBuilder.create(option)
                .values(registry.keySet().stream()
                        .map(Identifier::toString)
                        .toList());
    }
}
