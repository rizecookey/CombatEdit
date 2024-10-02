package net.rizecookey.clothconfig2.extension.gui.entries;

import com.google.common.collect.ImmutableList;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.math.Rectangle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicDropdownBoxEntry<T> extends DropdownBoxEntry<T> {
    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    public DynamicDropdownBoxEntry(Text fieldName, @NotNull Text resetButtonKey, @Nullable Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart, @Nullable Supplier<T> defaultValue, @Nullable Consumer<T> saveConsumer, @Nullable Iterable<T> selections, @NotNull DropdownBoxEntry.SelectionTopCellElement<T> topRenderer, @NotNull DropdownBoxEntry.SelectionCellCreator<T> cellCreator) {
        super(fieldName, resetButtonKey, tooltipSupplier, requiresRestart, defaultValue, saveConsumer, selections, topRenderer, cellCreator);
        this.selectionElement = new SelectionElement<>(this, new Rectangle(0, 0, 150, 20), new DynamicDropdownMenuElement<>(selections == null ? ImmutableList.of() : ImmutableList.copyOf(selections)), topRenderer, cellCreator);
    }

    public static class DynamicDropdownMenuElement<U> extends DefaultDropdownMenuElement<U> {
        public DynamicDropdownMenuElement(@NotNull ImmutableList<U> selections) {
            super(selections);
        }

        @Override
        public void lateRender(DrawContext graphics, int mouseX, int mouseY, float delta) {
            if (getCellCreator().getCellWidth() > lastRectangle.getWidth()) {
                lastRectangle.x -= getCellCreator().getCellWidth() - lastRectangle.getWidth();
            }
            super.lateRender(graphics, mouseX, mouseY, delta);
        }
    }
}
