package net.rizecookey.combatedit;

import net.fabricmc.api.ModInitializer;
import net.rizecookey.combatedit.configuration.Configuration;
import net.rizecookey.combatedit.configuration.RegistriesModifier;
import net.rizecookey.combatedit.item.DefaultEntityAttributeModifiers;
import net.rizecookey.combatedit.item.DefaultItemAttributeModifiers;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CombatEdit implements ModInitializer {
    private static CombatEdit INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);

    private RegistriesModifier modifier;
    private Configuration config;
    private ItemStackAttributeHelper attributeHelper;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        config = new Configuration(new DefaultItemAttributeModifiers(), new DefaultEntityAttributeModifiers());
        modifier = new RegistriesModifier();
        attributeHelper = new ItemStackAttributeHelper(this);

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    private void setConfig(Configuration config) {
        this.config = config;
        onConfigReload();
    }

    private void onConfigReload() {
        LOGGER.info("Redoing modifications to the registries...");
        modifier.revertModifications();
        modifier.makeModifications(config.itemModifierConfiguration(), config.entityModifierConfiguration());
        LOGGER.info("Modification done.");
    }

    public Configuration getConfig() {
        return config;
    }

    public ItemStackAttributeHelper getAttributeHelper() {
        return attributeHelper;
    }

    public RegistriesModifier getModifier() {
        return modifier;
    }

    public static CombatEdit getInstance() {
        return INSTANCE;
    }
}
