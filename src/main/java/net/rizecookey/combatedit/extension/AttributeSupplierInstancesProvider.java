package net.rizecookey.combatedit.extension;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public interface AttributeSupplierInstancesProvider {
    default Map<Holder<Attribute>, AttributeInstance> combatEdit$getInstances() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
