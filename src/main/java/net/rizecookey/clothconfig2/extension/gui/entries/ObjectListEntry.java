package net.rizecookey.clothconfig2.extension.gui.entries;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage"})
@Environment(EnvType.CLIENT)
public class ObjectListEntry<T> extends MultiElementListEntry<T> {
    private final ObjectAdapter<T> adapter;

    public ObjectListEntry(Text fieldName, List<AbstractConfigListEntry<?>> fieldEntries, ObjectAdapter<T> adapter) {
        super(fieldName, null, fieldEntries, true);
        this.adapter = adapter;
    }

    @Override
    public T getValue() {
        return adapter.getValue();
    }

    @Override
    public Optional<T> getDefaultValue() {
        return adapter.getDefaultValue();
    }
}
