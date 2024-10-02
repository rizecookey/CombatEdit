package net.rizecookey.combatedit.client;

import net.fabricmc.api.ClientModInitializer;

public class CombatEditClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new CombatEditClient();
    }
}
