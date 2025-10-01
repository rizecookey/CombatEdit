package net.rizecookey.combatedit.extension;

import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;

public interface LivingEntityExtension {
    default ConfigurationManager combatEdit$configurationManager() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
