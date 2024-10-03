package net.rizecookey.combatedit.api;

import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;

/**
 * The main interface for communicating with the CombatEdit mod.
 */
public interface CombatEditApi {
    /**
     * Registers a profile extension for a given base profile.
     * @param profileId         The identifier of the base profile being extended
     * @param extensionProvider An extension provider that provides the extension during resource reloads
     */
    void registerProfileExtension(Identifier profileId, ProfileExtensionProvider extensionProvider);
}
