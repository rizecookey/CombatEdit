package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.impl.builders.AbstractListBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.rizecookey.clothconfig2.extension.api.ObjectCellCreator;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ObjectListBuilder<T> extends AbstractListBuilder<T, ObjectListListEntry<T>, ObjectListBuilder<T>> {
    private final ObjectCellCreator<T> createNewCell;

    public ObjectListBuilder(Text fieldNameKey, List<T> values, ObjectCellCreator<T> createNewCell, Text resetButtonKey) {
        super(resetButtonKey, fieldNameKey);
        if (createNewCell == null) {
            throw new IllegalStateException("createNewCell not provided");
        }
        this.value = List.copyOf(values);
        this.createNewCell = createNewCell;
    }

    @Override
    public @NotNull ObjectListListEntry<T> build() {
        return new ObjectListListEntry<>(
                getFieldNameKey(),
                value,
                isExpanded(),
                () -> getTooltipSupplier().apply(value),
                getSaveConsumer(),
                getDefaultValue(),
                getResetButtonKey(),
                isRequireRestart(),
                isDeleteButtonEnabled(),
                isInsertInFront(),
                createNewCell
        );
    }
}
