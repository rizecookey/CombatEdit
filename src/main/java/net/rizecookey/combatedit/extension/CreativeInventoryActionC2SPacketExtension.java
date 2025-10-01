package net.rizecookey.combatedit.extension;

public interface CreativeInventoryActionC2SPacketExtension {
    default boolean combatEdit$hadPacketModification() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
