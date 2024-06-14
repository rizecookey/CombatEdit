package net.rizecookey.clothconfig2.extension.api;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.impl.builders.ExtendedConfigEntryBuilderImpl;
import net.rizecookey.clothconfig2.extension.impl.builders.ObjectFieldBuilder;
import net.rizecookey.clothconfig2.extension.impl.builders.ObjectListBuilder;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ExtendedConfigEntryBuilder extends ConfigEntryBuilder {
    static ExtendedConfigEntryBuilder create() {
        return new ExtendedConfigEntryBuilderImpl(ConfigEntryBuilder.create());
    }

    <T> ObjectListBuilder<T> startObjectList(Text fieldName, List<T> values, ObjectCellCreator<T> createNewCell);

    <T> ObjectFieldBuilder<T> startObjectField(Text fieldName, List<AbstractConfigListEntry<?>> innerEntries, ObjectAdapter<T> adapter);

}
