package net.rizecookey.combatedit.extension;

public interface ServerboundSetCreativeModeSlotPacketExtension {
    default boolean combatEdit$hadPacketModification() {
        throw new UnsupportedOperationException("Extension not applied correctly");
    }
}
