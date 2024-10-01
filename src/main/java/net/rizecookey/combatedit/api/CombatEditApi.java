package net.rizecookey.combatedit.api;

import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.api.extension.ProfileExtensionProvider;

public interface CombatEditApi {
    void registerProfileExtension(Identifier profileId, ProfileExtensionProvider extensionProvider);
}
