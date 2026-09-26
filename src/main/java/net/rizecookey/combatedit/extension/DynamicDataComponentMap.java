package net.rizecookey.combatedit.extension;

import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DynamicDataComponentMap implements DataComponentMap {
    private static boolean USE_EXCHANGEABLE = false;

    private DataComponentMap original;
    private DataComponentMap modified;

    public DynamicDataComponentMap(DataComponentMap original) {
        this.original = this.modified = original;
    }

    public DataComponentMap getOriginal() {
        return original;
    }

    public void setOriginal(DataComponentMap original) {
        this.original = original;
    }

    public DataComponentMap getModified() {
        return modified;
    }

    public void setModified(DataComponentMap modified) {
        this.modified = modified;
    }

    private DataComponentMap getCurrent() {
        return USE_EXCHANGEABLE ? modified : original;
    }

    @Override
    public @Nullable <T> T get(@NonNull DataComponentType<? extends T> type) {
        return getCurrent().get(type);
    }

    @Override
    public @NonNull Set<DataComponentType<?>> keySet() {
        return getCurrent().keySet();
    }

    public static boolean shouldUseExchangeable() {
        return USE_EXCHANGEABLE;
    }

    public static void setUseExchangeable(boolean useExchangeable) {
        USE_EXCHANGEABLE = useExchangeable;
    }
}
