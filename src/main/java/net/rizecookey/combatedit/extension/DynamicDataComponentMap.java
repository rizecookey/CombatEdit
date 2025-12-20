package net.rizecookey.combatedit.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.NonNull;

public class DynamicDataComponentMap implements DataComponentMap {
    private static boolean USE_EXCHANGEABLE = false;

    private final DataComponentMap original;
    private DataComponentMap exchangeable;

    public DynamicDataComponentMap(DataComponentMap original) {
        this.original = this.exchangeable = original;
    }

    public DataComponentMap getOriginal() {
        return original;
    }

    public DataComponentMap getExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(DataComponentMap exchangeable) {
        this.exchangeable = exchangeable;
    }

    private DataComponentMap getCurrent() {
        return USE_EXCHANGEABLE ? exchangeable : original;
    }

    @Override
    public @Nullable <T> T get(@NonNull DataComponentType<? extends T> type) {
        return getCurrent().get(type);
    }

    @Override
    public @NotNull Set<DataComponentType<?>> keySet() {
        return getCurrent().keySet();
    }

    public static boolean shouldUseExchangeable() {
        return USE_EXCHANGEABLE;
    }

    public static void setUseExchangeable(boolean useExchangeable) {
        USE_EXCHANGEABLE = useExchangeable;
    }
}
