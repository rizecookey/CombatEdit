package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.function.Function;

public class ExtendedDropdownMenus {
    public static int getMaxNeededWidth(Registry<?> registry) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        return registry.getIds().stream()
                .mapToInt(id -> textRenderer.getWidth(id.toString()))
                .max()
                .orElse(146);
    }

    public static <T extends Enum<T> & StringIdentifiable> int getMaxNeededWidth(Class<T> enumClass) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        return Arrays.stream(enumClass.getEnumConstants())
                .mapToInt(entry -> textRenderer.getWidth(entry.asString()))
                .max()
                .orElse(146);
    }

    public static class TopCellElementBuilder {
        private static <T> Function<String, Identifier> registryCheckedStringToIdentifier(Registry<T> registry) {
            return string -> {
                Identifier identifier;
                try {
                    identifier = Identifier.of(string);
                } catch (InvalidIdentifierException e) {
                    identifier = null;
                }
                return registry.containsId(identifier) ? identifier : null;
            };
        }

        public static <T> DropdownBoxEntry.SelectionTopCellElement<Identifier> ofRegistryIdentifier(Registry<T> registry, T initialValue) {
            var id = registry.getId(initialValue);
            if (id == null) {
                throw new IllegalArgumentException("Not a member of the specified registry");
            }
            return new DropdownBoxEntry.DefaultSelectionTopCellElement<>(
                    id,
                    registryCheckedStringToIdentifier(registry),
                    identifier -> Text.literal(identifier.toString())
            );
        }

        public static <T extends Enum<T> & StringIdentifiable> DropdownBoxEntry.SelectionTopCellElement<T> ofStringIdentifiable(Class<T> enumClass, T initialValue) {
            return new DropdownBoxEntry.DefaultSelectionTopCellElement<>(
                    initialValue,
                    string -> Arrays.stream(enumClass.getEnumConstants())
                            .filter(entry -> entry.asString().equals(string))
                            .findAny().orElse(null),
                    entry -> Text.literal(entry.asString())
            );
        }
    }

    public static class CellCreatorBuilder {
        public static <T extends Enum<T> & StringIdentifiable> DropdownBoxEntry.SelectionCellCreator<T> ofStringIdentifiableAutoWidth(Class<T> enumClass) {
            var neededWidth = getMaxNeededWidth(enumClass) + 12;
            return DropdownMenuBuilder.CellCreatorBuilder.ofWidth(neededWidth, entry -> Text.literal(entry.asString()));
        }

        public static <T> DropdownBoxEntry.SelectionCellCreator<Identifier> ofRegistryIdentifier(Registry<T> registry) {
            var neededWidth = getMaxNeededWidth(registry) + 12;
            return DropdownMenuBuilder.CellCreatorBuilder.ofWidth(neededWidth, identifier -> Text.literal(identifier.toString()));
        }
    }
}
