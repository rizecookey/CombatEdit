package net.rizecookey.clothconfig2.extension.gui.entries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface ObjectAdapter<T> {
    T getValue();
    Optional<T> getDefaultValue();

    static <T> ObjectAdapter<T> create(Supplier<T> getValue, Supplier<Optional<T>> getDefaultValue) {
        return new ObjectAdapter<>() {
            @Override
            public T getValue() {
                return getValue.get();
            }

            @Override
            public Optional<T> getDefaultValue() {
                return getDefaultValue.get();
            }
        };
    }
}
