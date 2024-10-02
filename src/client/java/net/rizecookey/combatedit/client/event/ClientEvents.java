package net.rizecookey.combatedit.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public final class ClientEvents {
    private ClientEvents() {}

    public static final Event<ClientFinishedLoading> CLIENT_FINISHED_LOADING = EventFactory.createArrayBacked(ClientFinishedLoading.class, callbacks -> client -> {
        for (var callback : callbacks) {
            callback.onClientFinishedLoading(client);
        }
    });

    @FunctionalInterface
    public interface ClientFinishedLoading {
        void onClientFinishedLoading(MinecraftClient client);
    }
}
