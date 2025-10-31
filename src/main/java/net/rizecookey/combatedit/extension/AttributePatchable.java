package net.rizecookey.combatedit.extension;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

public interface AttributePatchable {
    default void combatEdit$preSend(ServerGamePacketListenerImpl networkHandler) {}
}
