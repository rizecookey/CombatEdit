package net.rizecookey.combatedit.extension;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DynamicComponentMap implements ComponentMap {
    private static boolean USE_EXCHANGEABLE = false;

    private final ComponentMap original;
    private ComponentMap exchangeable;

    public DynamicComponentMap(ComponentMap original) {
        this.original = original;
        this.exchangeable = original;
    }

    public ComponentMap getOriginal() {
        return original;
    }

    public ComponentMap getExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(ComponentMap exchangeable) {
        this.exchangeable = exchangeable;
    }

    private ComponentMap getCurrent() {
        return USE_EXCHANGEABLE ? exchangeable : original;
    }

    @Override
    public @Nullable <T> T get(ComponentType<? extends T> type) {
        return getCurrent().get(type);
    }

    @Override
    public Set<ComponentType<?>> getTypes() {
        return getCurrent().getTypes();
    }

    public static boolean shouldUseExchangeable() {
        return USE_EXCHANGEABLE;
    }

    public static void setUseExchangeable(boolean useExchangeable) {
        USE_EXCHANGEABLE = useExchangeable;
    }
}
