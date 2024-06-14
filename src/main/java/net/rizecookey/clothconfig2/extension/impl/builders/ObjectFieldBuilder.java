package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ObjectFieldBuilder<T> extends AbstractFieldBuilder<T, ObjectListEntry<T>, ObjectFieldBuilder<T>> {
    private final List<AbstractConfigListEntry<?>> innerEntries;
    private final ObjectAdapter<T> adapter;

    public ObjectFieldBuilder(Text fieldNameKey, List<AbstractConfigListEntry<?>> innerEntries, ObjectAdapter<T> adapter, Text resetButtonKey) {
        super(resetButtonKey, fieldNameKey);
        this.innerEntries = List.copyOf(innerEntries != null ? innerEntries : List.of());
        if (adapter == null) {
            throw new IllegalArgumentException("No object adapter specified");
        }
        this.adapter = adapter;
    }

    @Override
    public @NotNull ObjectListEntry<T> build() {
        return new ObjectListEntry<>(getFieldNameKey(), innerEntries, adapter);
    }
}
