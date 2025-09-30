package net.rizecookey.combatedit.api.extension;

import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;

/**
 * Instances of this interface provide a profile extension dynamically on reload.
 */
@FunctionalInterface
public interface ProfileExtensionProvider {
    /**
     * Provide a profile extension on configuration load.
     *
     * @param profile The base profile that is being extended
     * @param defaultsSupplier an object providing vanilla and profile defaults for items and entities
     * @return the profile extension
     */
    ProfileExtension provideExtension(BaseProfile profile, DefaultsSupplier defaultsSupplier);
}
