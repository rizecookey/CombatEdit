package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import java.util.Arrays;
import java.util.function.Function;

public class ExtendedDropdownMenus {
    public static int getMaxNeededWidth(Registry<?> registry) {
        var textRenderer = Minecraft.getInstance().font;
        return registry.keySet().stream()
                .mapToInt(id -> textRenderer.width(id.toString()))
                .max()
                .orElse(146);
    }

    public static <T extends Enum<T> & StringRepresentable> int getMaxNeededWidth(Class<T> enumClass) {
        var textRenderer = Minecraft.getInstance().font;
        return Arrays.stream(enumClass.getEnumConstants())
                .mapToInt(entry -> textRenderer.width(entry.getSerializedName()))
                .max()
                .orElse(146);
    }

    public static class TopCellElementBuilder {
        private static <T> Function<String, Identifier> registryCheckedStringToIdentifier(Registry<T> registry) {
            return string -> {
                Identifier identifier;
                try {
                    identifier = Identifier.parse(string);
                } catch (IdentifierException e) {
                    identifier = null;
                }
                return identifier != null && registry.containsKey(identifier) ? identifier : null;
            };
        }

        public static <T> DropdownBoxEntry.SelectionTopCellElement<Identifier> ofRegistryIdentifier(Registry<T> registry, T initialValue) {
            var id = registry.getKey(initialValue);
            if (id == null) {
                throw new IllegalArgumentException("Not a member of the specified registry");
            }
            return new DropdownBoxEntry.DefaultSelectionTopCellElement<>(
                    id,
                    registryCheckedStringToIdentifier(registry),
                    identifier -> Component.literal(identifier.toString())
            );
        }

        public static <T extends Enum<T> & StringRepresentable> DropdownBoxEntry.SelectionTopCellElement<T> ofStringIdentifiable(Class<T> enumClass, T initialValue) {
            return new DropdownBoxEntry.DefaultSelectionTopCellElement<>(
                    initialValue,
                    string -> Arrays.stream(enumClass.getEnumConstants())
                            .filter(entry -> entry.getSerializedName().equals(string))
                            .findAny().orElse(null),
                    entry -> Component.literal(entry.getSerializedName())
            );
        }
    }

    public static class CellCreatorBuilder {
        public static <T extends Enum<T> & StringRepresentable> DropdownBoxEntry.SelectionCellCreator<T> ofStringIdentifiableAutoWidth(Class<T> enumClass) {
            var neededWidth = getMaxNeededWidth(enumClass) + 12;
            return DropdownMenuBuilder.CellCreatorBuilder.ofWidth(neededWidth, entry -> Component.literal(entry.getSerializedName()));
        }

        public static <T> DropdownBoxEntry.SelectionCellCreator<Identifier> ofRegistryIdentifier(Registry<T> registry) {
            var neededWidth = getMaxNeededWidth(registry) + 12;
            return DropdownMenuBuilder.CellCreatorBuilder.ofWidth(neededWidth, identifier -> Component.literal(identifier.toString()));
        }
    }
}
