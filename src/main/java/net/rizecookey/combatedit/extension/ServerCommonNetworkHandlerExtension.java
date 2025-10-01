package net.rizecookey.combatedit.extension;

public interface ServerCommonNetworkHandlerExtension {
    default boolean combatEdit$isAttributePatchingEnabled() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }

    default void combatEdit$setAttributePatchingEnabled(boolean enabled) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
