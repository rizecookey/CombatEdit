package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.text.Text;
import net.rizecookey.clothconfig2.extension.gui.entries.DynamicDropdownBoxEntry;
import org.jetbrains.annotations.NotNull;

public class DynamicDropdownMenuBuilder<T> extends DropdownMenuBuilder<T> {
    public DynamicDropdownMenuBuilder(Text resetButtonKey, Text fieldNameKey, DropdownBoxEntry.SelectionTopCellElement<T> topCellElement, DropdownBoxEntry.SelectionCellCreator<T> cellCreator) {
        super(resetButtonKey, fieldNameKey, topCellElement, cellCreator);
    }

    @Override
    public @NotNull DropdownBoxEntry<T> build() {
        DropdownBoxEntry<T> entry = new DynamicDropdownBoxEntry<>(getFieldNameKey(), getResetButtonKey(), null, isRequireRestart(), defaultValue, saveConsumer, selections, topCellElement, cellCreator);
        entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        entry.setSuggestionMode(suggestionMode);
        return finishBuilding(entry);
    }
}
