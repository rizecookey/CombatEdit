package net.rizecookey.clothconfig2.extension.impl.builders;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.ColorFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleListBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatListBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntListBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.KeyCodeBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongListBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.SelectorBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.rizecookey.clothconfig2.extension.api.ExtendedConfigEntryBuilder;
import net.rizecookey.clothconfig2.extension.api.ObjectCellCreator;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ExtendedConfigEntryBuilderImpl implements ExtendedConfigEntryBuilder {
    private final ConfigEntryBuilder fallback;

    public ExtendedConfigEntryBuilderImpl(ConfigEntryBuilder fallback) {
        this.fallback = fallback;
    }

    @Override
    public <T> ObjectListBuilder<T> startObjectList(Text fieldName, List<T> values, ObjectCellCreator<T> createNewCell) {
        return new ObjectListBuilder<>(fieldName, values, createNewCell, getResetButtonKey());
    }

    @Override
    public <T> ObjectFieldBuilder<T> startObjectField(Text fieldName, List<AbstractConfigListEntry<?>> innerEntries, ObjectAdapter<T> adapter) {
        return new ObjectFieldBuilder<>(fieldName, innerEntries, adapter, getResetButtonKey());
    }

    @Override
    public Text getResetButtonKey() {
        return fallback.getResetButtonKey();
    }

    @Override
    public ConfigEntryBuilder setResetButtonKey(Text text) {
        return fallback.setResetButtonKey(text);
    }

    @Override
    public IntListBuilder startIntList(Text text, List<Integer> list) {
        return fallback.startIntList(text, list);
    }

    @Override
    public LongListBuilder startLongList(Text text, List<Long> list) {
        return fallback.startLongList(text, list);
    }

    @Override
    public FloatListBuilder startFloatList(Text text, List<Float> list) {
        return fallback.startFloatList(text, list);
    }

    @Override
    public DoubleListBuilder startDoubleList(Text text, List<Double> list) {
        return fallback.startDoubleList(text, list);
    }

    @Override
    public StringListBuilder startStrList(Text text, List<String> list) {
        return fallback.startStrList(text, list);
    }

    @Override
    public SubCategoryBuilder startSubCategory(Text text) {
        return fallback.startSubCategory(text);
    }

    @Override
    public SubCategoryBuilder startSubCategory(Text text, List<AbstractConfigListEntry> list) {
        return fallback.startSubCategory(text, list);
    }

    @Override
    public BooleanToggleBuilder startBooleanToggle(Text text, boolean b) {
        return fallback.startBooleanToggle(text, b);
    }

    @Override
    public StringFieldBuilder startStrField(Text text, String s) {
        return fallback.startStrField(text, s);
    }

    @Override
    public ColorFieldBuilder startColorField(Text text, int i) {
        return fallback.startColorField(text, i);
    }

    @Override
    public TextFieldBuilder startTextField(Text text, String s) {
        return fallback.startTextField(text, s);
    }

    @Override
    public TextDescriptionBuilder startTextDescription(Text text) {
        return fallback.startTextDescription(text);
    }

    @Override
    public <T extends Enum<?>> EnumSelectorBuilder<T> startEnumSelector(Text text, Class<T> aClass, T t) {
        return fallback.startEnumSelector(text, aClass, t);
    }

    @Override
    public <T> SelectorBuilder<T> startSelector(Text text, T[] ts, T t) {
        return fallback.startSelector(text, ts, t);
    }

    @Override
    public IntFieldBuilder startIntField(Text text, int i) {
        return fallback.startIntField(text, i);
    }

    @Override
    public LongFieldBuilder startLongField(Text text, long l) {
        return fallback.startLongField(text, l);
    }

    @Override
    public FloatFieldBuilder startFloatField(Text text, float v) {
        return fallback.startFloatField(text, v);
    }

    @Override
    public DoubleFieldBuilder startDoubleField(Text text, double v) {
        return fallback.startDoubleField(text, v);
    }

    @Override
    public IntSliderBuilder startIntSlider(Text text, int i, int i1, int i2) {
        return fallback.startIntSlider(text, i, i1, i2);
    }

    @Override
    public LongSliderBuilder startLongSlider(Text text, long l, long l1, long l2) {
        return fallback.startLongSlider(text, l, l1, l2);
    }

    @Override
    public KeyCodeBuilder startModifierKeyCodeField(Text text, ModifierKeyCode modifierKeyCode) {
        return fallback.startModifierKeyCodeField(text, modifierKeyCode);
    }

    @Override
    public <T> DropdownMenuBuilder<T> startDropdownMenu(Text text, DropdownBoxEntry.SelectionTopCellElement<T> selectionTopCellElement, DropdownBoxEntry.SelectionCellCreator<T> selectionCellCreator) {
        return new DynamicDropdownMenuBuilder<>(getResetButtonKey(), text, selectionTopCellElement, selectionCellCreator);
    }
}
