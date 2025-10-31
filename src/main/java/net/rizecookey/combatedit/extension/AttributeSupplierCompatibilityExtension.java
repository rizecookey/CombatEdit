package net.rizecookey.combatedit.extension;

public interface AttributeSupplierCompatibilityExtension {
    default boolean combatEdit$sendAllAttributes() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
    default void combatEdit$setSendAllAttributes(boolean sendAll) {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
