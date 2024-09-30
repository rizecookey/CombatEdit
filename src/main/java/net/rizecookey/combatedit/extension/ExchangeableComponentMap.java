package net.rizecookey.combatedit.extension;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ExchangeableComponentMap implements ComponentMap {
    private ComponentMap currentBase;

    public ExchangeableComponentMap(ComponentMap base) {
        this.currentBase = base;
    }

    public void exchangeBase(ComponentMap newBase) {
        this.currentBase = newBase;
    }

    @Override
    public @Nullable <T> T get(DataComponentType<? extends T> type) {
        return currentBase.get(type);
    }

    @Override
    public Set<DataComponentType<?>> getTypes() {
        return currentBase.getTypes();
    }
}
