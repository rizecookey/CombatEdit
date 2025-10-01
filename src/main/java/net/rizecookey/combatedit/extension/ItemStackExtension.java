package net.rizecookey.combatedit.extension;

public interface ItemStackExtension {
    default void combatEdit$useOriginalComponentMapAsBase() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
