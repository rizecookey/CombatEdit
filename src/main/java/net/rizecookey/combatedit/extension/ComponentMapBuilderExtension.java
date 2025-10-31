package net.rizecookey.combatedit.extension;

import net.minecraft.core.component.DataComponentMap;

public interface ComponentMapBuilderExtension {
    default DataComponentMap.Builder combatEdit$preventDynamicWrap() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
