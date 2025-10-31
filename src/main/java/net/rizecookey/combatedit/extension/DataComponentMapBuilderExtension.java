package net.rizecookey.combatedit.extension;

import net.minecraft.core.component.DataComponentMap;

public interface DataComponentMapBuilderExtension {
    default DataComponentMap.Builder combatEdit$preventDynamicWrap() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
