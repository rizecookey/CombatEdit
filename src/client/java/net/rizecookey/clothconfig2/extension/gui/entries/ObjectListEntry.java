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

    public ObjectListEntry(Text fieldName, List<AbstractConfigListEntry<?>> fieldEntries, ObjectAdapter<T> adapter, boolean defaultExpanded) {
        super(fieldName, null, fieldEntries, true);
        this.setExpanded(defaultExpanded);
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

    @SuppressWarnings("deprecation")
    @Override
    public int getMorePossibleHeight() {
        if (getReferenceProviderEntries() == null) {
            return -1;
        }

        return getReferenceProviderEntries().stream()
                .mapToInt(entry -> entry.provideReferenceEntry().getMorePossibleHeight())
                .filter(value -> value >= 0)
                .sum();
    }
}
