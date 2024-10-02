package net.rizecookey.combatedit;

import net.fabricmc.api.DedicatedServerModInitializer;

public class CombatEditServerInitializer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        new CombatEdit();
    }
}
