package net.rizecookey.combatedit.extension;

import net.minecraft.component.ComponentMap;

public interface ComponentMapBuilderExtension {
    default ComponentMap.Builder combatEdit$preventDynamicWrap() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
