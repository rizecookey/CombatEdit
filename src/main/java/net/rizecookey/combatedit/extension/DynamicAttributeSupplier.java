package net.rizecookey.combatedit.extension;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class DynamicAttributeSupplier extends AttributeSupplier implements AttributeSupplierInstancesProvider {
    private static boolean USE_EXCHANGEABLE = false;

    private final AttributeSupplier original;
    private AttributeSupplier exchangeable;

    public DynamicAttributeSupplier(AttributeSupplier original) {
        super(Map.copyOf(original.combatEdit$getInstances()));
        this.exchangeable = this.original = original;
    }

    public AttributeSupplier getOriginal() {
        return original;
    }

    public AttributeSupplier getExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(AttributeSupplier exchangeable) {
        this.exchangeable = exchangeable;
    }

    public static boolean shouldUseExchangeable() {
        return USE_EXCHANGEABLE;
    }

    public static void setUseExchangeable(boolean useExchangeable) {
        USE_EXCHANGEABLE = useExchangeable;
    }

    @Override
    public Map<Holder<Attribute>, AttributeInstance> combatEdit$getInstances() {
        return USE_EXCHANGEABLE ? exchangeable.combatEdit$getInstances() : original.combatEdit$getInstances();
    }
}
