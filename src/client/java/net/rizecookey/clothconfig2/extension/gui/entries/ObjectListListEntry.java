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
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
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
        private boolean selected = false;

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
            var morePossibleHeight = inner.getMorePossibleHeight();
            return inner.getItemHeight() + (morePossibleHeight >= 0 ? morePossibleHeight : 0);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            inner.setParent(((DynamicEntryListWidget<AbstractConfigEntry<T>>) (Object) listListEntry.getParent()));
            inner.setScreen(listListEntry.getConfigScreen());
            inner.render(drawContext, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
            if (this.selected) {
                drawContext.drawBorder(x - 16, y - 1, entryWidth + 17, entryHeight - 2, Color.ofRGB(127, 127, 127).getColor());
            }

            inner.lateRender(drawContext, mouseX, mouseY, delta);
        }

        @Nullable
        @Override
        public Element getFocused() {
            return inner.getFocused();
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            inner.setFocused(focused);
        }

        @Override
        public Optional<Element> hoveredElement(double mouseX, double mouseY) {
            return inner.hoveredElement(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return inner.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return inner.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return inner.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return inner.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return inner.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return inner.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            return inner.charTyped(chr, modifiers);
        }

        @Override
        public void setFocused(boolean focused) {
            inner.setFocused(focused);
        }

        @Override
        public boolean isFocused() {
            return inner.isFocused();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            inner.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return inner.isMouseOver(mouseX, mouseY);
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
            this.selected = isSelected;
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

        @Nullable
        @Override
        public GuiNavigationPath getFocusedPath() {
            return inner.getFocusedPath();
        }

        @Nullable
        @Override
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
            return inner.getNavigationPath(navigation);
        }

        @Override
        public ScreenRect getNavigationFocus() {
            return inner.getNavigationFocus();
        }

        @Override
        public boolean isNarratable() {
            return inner.isNarratable();
        }

        @Override
        public int getNavigationOrder() {
            return inner.getNavigationOrder();
        }
    }
}
