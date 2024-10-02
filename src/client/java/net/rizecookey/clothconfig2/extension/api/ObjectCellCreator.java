package net.rizecookey.clothconfig2.extension.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListListEntry;

import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface ObjectCellCreator<T> extends BiFunction<T, ObjectListListEntry<T>, ObjectListEntry<T>> {
}
