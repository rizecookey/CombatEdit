package net.rizecookey.clothconfig2.extension.gui.entries;

import com.google.common.collect.Iterators;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.ReferenceProvider;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.math.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class ObjectListListEntry<T> extends AbstractListListEntry<T, ObjectListListEntry.ObjectListCell<T>, ObjectListListEntry<T>> {
    private final List<ReferenceProvider<?>> referencableEntries = new ArrayList<>();

    public ObjectListListEntry(Text fieldName, List<T> value, boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier, Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, ObjectListListEntry<T>, ObjectListEntry<T>> createNewCell) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, (t, nestedListListEntry) -> new ObjectListCell<>(t, nestedListListEntry, createNewCell.apply(t, nestedListListEntry)));
        this.cells.forEach(cell -> this.referencableEntries.add(cell.inner));
        setReferenceProviderEntries(referencableEntries);
    }

    @Override
    public Iterator<String> getSearchTags() {
        return Iterators.concat(super.getSearchTags(), Iterators.concat(cells.stream().map(cell -> cell.inner.getSearchTags()).iterator()));
    }

    @Override
    public ObjectListListEntry<T> self() {
        return this;
    }

    public static class ObjectListCell<T> extends AbstractListCell<T, ObjectListCell<T>, ObjectListListEntry<T>> {
        private final ObjectListEntry<T> inner;

        public ObjectListCell(@Nullable T value, ObjectListListEntry<T> listListEntry, ObjectListEntry<T> inner) {
            super(value, listListEntry);
            this.inner = inner;
        }

        @Override
        public T getValue() {
            return inner.getValue();
        }

        @Override
        public Optional<Text> getError() {
            return inner.getError();
        }

        @Override
        public int getCellHeight() {
            return inner.getItemHeight();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            inner.setParent(((DynamicEntryListWidget<AbstractConfigEntry<T>>) (Object) listListEntry.getParent()));
            inner.setScreen(listListEntry.getConfigScreen());
            inner.render(drawContext, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
            if (isSelected) {
                drawContext.drawBorder(x - 16, y - 1, entryWidth + 17, entryHeight - 2, Color.ofRGB(127, 127, 127).getColor());
            }
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            inner.appendNarrations(builder);
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(inner);
        }

        @Override
        public SelectionType getType() {
            return inner.getType();
        }

        @Override
        public boolean isRequiresRestart() {
            return inner.isRequiresRestart();
        }

        @Override
        public void updateSelected(boolean isSelected) {
            inner.updateSelected(isSelected);
        }

        @Override
        public boolean isEdited() {
            return inner.isEdited();
        }

        @Override
        public void onAdd() {
            super.onAdd();
            listListEntry.referencableEntries.add(inner);
            listListEntry.requestReferenceRebuilding();
        }

        @Override
        public void onDelete() {
            super.onDelete();
            listListEntry.referencableEntries.remove(inner);
            listListEntry.requestReferenceRebuilding();
        }
    }
}
