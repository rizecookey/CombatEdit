package net.rizecookey.combatedit.extension;

import net.minecraft.server.network.ServerPlayNetworkHandler;

public interface AttributePatchable {
    default void combatEdit$preSend(ServerPlayNetworkHandler networkHandler) {}
}
