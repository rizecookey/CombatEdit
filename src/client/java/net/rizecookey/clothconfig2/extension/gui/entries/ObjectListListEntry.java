package net.rizecookey.clothconfig2.extension.gui.entries;

import com.google.common.collect.Iterators;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.ReferenceProvider;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.math.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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

    public ObjectListListEntry(Component fieldName, List<T> value, boolean defaultExpanded, Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, Component resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, ObjectListListEntry<T>, ObjectListEntry<T>> createNewCell) {
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
        public Optional<Component> getError() {
            return inner.getError();
        }

        @Override
        public int getCellHeight() {
            var morePossibleHeight = inner.getMorePossibleHeight();
            return inner.getItemHeight() + (morePossibleHeight >= 0 ? morePossibleHeight : 0);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void render(GuiGraphics drawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            inner.setParent(((DynamicEntryListWidget<AbstractConfigEntry<T>>) (Object) listListEntry.getParent()));
            inner.setScreen(listListEntry.getConfigScreen());
            inner.render(drawContext, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
            if (this.selected) {
                drawContext.renderOutline(x - 16, y - 1, entryWidth + 17, entryHeight - 2, Color.ofRGB(127, 127, 127).getColor());
            }

            inner.lateRender(drawContext, mouseX, mouseY, delta);
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return inner.getFocused();
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            inner.setFocused(focused);
        }

        @Override
        public @NotNull Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
            return inner.getChildAt(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            return inner.mouseClicked(event, doubleClick);
        }

        @Override
        public boolean mouseReleased(@NonNull MouseButtonEvent event) {
            return inner.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(@NonNull MouseButtonEvent event, double f, double g) {
            return inner.mouseDragged(event, f, g);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return inner.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyReleased(@NonNull KeyEvent event) {
            return inner.keyReleased(event);
        }

        @Override
        public boolean keyPressed(@NonNull KeyEvent event) {
            return inner.keyPressed(event);
        }

        @Override
        public boolean charTyped(@NonNull CharacterEvent event) {
            return inner.charTyped(event);
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
        public void updateNarration(@NonNull NarrationElementOutput builder) {
            inner.updateNarration(builder);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return Collections.singletonList(inner);
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return inner.narrationPriority();
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
        public ComponentPath getCurrentFocusPath() {
            return inner.getCurrentFocusPath();
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(@NonNull FocusNavigationEvent navigation) {
            return inner.nextFocusPath(navigation);
        }

        @Override
        public @NotNull ScreenRectangle getRectangle() {
            return inner.getRectangle();
        }

        @Override
        public int getTabOrderGroup() {
            return inner.getTabOrderGroup();
        }
    }
}
