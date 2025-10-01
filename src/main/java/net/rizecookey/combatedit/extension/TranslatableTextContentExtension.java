package net.rizecookey.combatedit.extension;

public interface TranslatableTextContentExtension {
    default void combatEdit$setFallback(String fallback) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
