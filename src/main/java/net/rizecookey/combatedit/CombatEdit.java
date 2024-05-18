package net.rizecookey.combatedit;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.Configuration;
import net.rizecookey.combatedit.extension.ItemExtension;
import net.rizecookey.combatedit.item.DefaultEntityAttributeModifiers;
import net.rizecookey.combatedit.item.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.item.ItemAttributeModifierProvider;
import net.rizecookey.combatedit.item.DefaultItemAttributeModifiers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CombatEdit implements ModInitializer {
    private static CombatEdit INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);

    private Configuration config;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        setConfig(new Configuration(new DefaultItemAttributeModifiers(), new DefaultEntityAttributeModifiers()));

        LOGGER.info("Successfully initialized CombatEdit.");
    }

    private void setConfig(Configuration config) {
        this.config = config;
        onConfigReload();
    }

    private void onConfigReload() {
        modifyItemAttributes();
        modifyDefaultEntityAttributes();
        LOGGER.info("Modification done.");
    }

    private void modifyItemAttributes() {
        LOGGER.info("Modifying item attributes...");
        ItemAttributeModifierProvider provider = config.itemModifierConfiguration();
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);

            if (!provider.shouldModifyItem(id, item)) {
                continue;
            }

            AttributeModifiersComponent attributeModifiers = provider.getModifiers(id, item);
            ((ItemExtension) item).combatEdit$setAttributeModifiers(attributeModifiers);
            LOGGER.debug("Modified attribute modifiers for item {}.", id);
        }
    }

    private void modifyDefaultEntityAttributes() {
        LOGGER.info("Modifying default entity attributes...");
        EntityAttributeModifierProvider modifierProvider = config.entityModifierConfiguration();
        ImmutableMap.Builder<EntityType<? extends LivingEntity>, DefaultAttributeContainer> builder = ImmutableMap.builder();
        builder.putAll(DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY);

        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            if (!modifierProvider.shouldModifyEntity(id, type)) {
                continue;
            }

            builder.put(type, modifierProvider.getModifiers(id, type));
            LOGGER.debug("Added modification for entity type {}", id);
        }

        DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY = builder.buildKeepingLast();
    }

    public Configuration getConfig() {
        return config;
    }

    public static CombatEdit getInstance() {
        return INSTANCE;
    }
}
