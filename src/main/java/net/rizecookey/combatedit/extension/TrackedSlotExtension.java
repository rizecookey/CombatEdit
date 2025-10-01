package net.rizecookey.combatedit.extension;

public interface TrackedSlotExtension {
    default void combatEdit$setCompareWithDisplayModified(boolean value) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
