package net.rizecookey.combatedit.extension;

public interface TranslatableContentsExtension {
    default void combatEdit$setFallback(String fallback) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
