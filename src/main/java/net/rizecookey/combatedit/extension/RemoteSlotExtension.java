package net.rizecookey.combatedit.extension;

public interface RemoteSlotExtension {
    default void combatEdit$setCompareWithDisplayModified(boolean value) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
