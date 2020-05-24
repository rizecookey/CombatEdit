package net.rizecookey.combatedit;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer {
    String prefix = "[CombatEdit]";

    @Override
    public void onInitialize() {
        Logger logger = LogManager.getLogger();
        logger.info("Successfully initialized CombatEdit.");
    }

}
