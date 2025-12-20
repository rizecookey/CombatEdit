package net.rizecookey.combatedit.modification;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.AttributeSupplierExtensions;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
import net.rizecookey.combatedit.extension.DynamicAttributeSupplier;
import net.rizecookey.combatedit.modification.entity.EntityModificationMap;
import net.rizecookey.combatedit.modification.entity.EntityModificationProvider;
import net.rizecookey.combatedit.modification.item.ItemModificationMap;
import net.rizecookey.combatedit.modification.item.ItemModificationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PropertyModifier implements DefaultsSupplier {
    private final ConfigurationManager configurationProvider;
    private final Items items;
    private final Entities entities;

    public PropertyModifier(ConfigurationManager configurationProvider) {
        this.configurationProvider = configurationProvider;
        this.items = new Items();
        this.entities = new Entities();
    }

    public void makeModifications() {
        reloadModificationProviders();
        items.modify();
        entities.modify();
        updateAttributesToClients();
    }

    public void reloadModificationProviders() {
        entities.reloadModificationProvider();
        items.reloadModificationProvider();
    }

    private void updateEntitiesAttributeContainers(EntityType<? extends LivingEntity> type, AttributeSupplier previousDefaults) {
        MinecraftServer server;
        if ((server = configurationProvider.getCurrentServer()) == null) {
            return;
        }

        server.getAllLevels()
                .forEach(world -> world.getEntities(EntityTypeTest.forClass(LivingEntity.class), entity -> entity.getType().equals(type))
                        .forEach(entity -> entity.getAttributes().combatEdit$patchWithNewDefaults(type, previousDefaults))
                );
    }

    private void updateAttributesToClients() {
        MinecraftServer currentServer = configurationProvider.getCurrentServer();
        if (currentServer == null) {
            return;
        }

        for (var player : currentServer.getPlayerList().getPlayers()) {
            player.containerMenu.broadcastFullState();
        }
    }

    @Override
    public Items items() {
        return items;
    }

    @Override
    public Entities entities() {
        return entities;
    }

    public class Items implements DefaultsSupplier.Items {
        private ItemModificationProvider modificationProvider = new ItemModificationMap(Map.of(), Map.of());

        public ItemModificationProvider modificationProvider() {
            return modificationProvider;
        }

        @Override
        public DataComponentMap getVanillaComponents(Item item) {
            var components = item.components();
            if (!(components instanceof DynamicDataComponentMap dynamicDataComponentMap)) {
                return components;
            }

            return dynamicDataComponentMap.getOriginal();
        }

        @Override
        public ItemAttributeModifiers getVanillaAttributeModifiers(Item item) {
            var components = getVanillaComponents(item);
            return Objects.requireNonNullElse(components.get(DataComponents.ATTRIBUTE_MODIFIERS),
                    ItemAttributeModifiers.EMPTY);
        }

        private void reloadModificationProvider() {
            Configuration configuration = configurationProvider.getConfiguration();
            modificationProvider = ItemModificationMap.fromConfiguration(configuration.getItemAttributes(), configuration.getItemComponents(), items);
        }

        @SuppressWarnings("unchecked")
        private void modify() {
            List<Item> incompatibles = null;
            for (Item item : BuiltInRegistries.ITEM) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                DataComponentMap components = item.components();
                if (!(components instanceof DynamicDataComponentMap dynamicComponents)) {
                    if (!modificationProvider.shouldModifyAttributes(id, item) && !modificationProvider.shouldModifyDefaultComponents(id, item)) {
                        continue;
                    }
                    if (incompatibles == null) {
                        incompatibles = new ArrayList<>();
                    }
                    incompatibles.add(item);
                    continue;
                }

                var builder = DataComponentMap.builder().combatEdit$preventDynamicWrap();
                modificationProvider.getComponents(id, item, dynamicComponents.getOriginal())
                        .forEach(component -> builder.set((DataComponentType<Object>) component.type(), component.value()));

                if (modificationProvider.shouldModifyAttributes(id, item)) {
                    var modifiers = modificationProvider.getAttributeModifiers(id, item, dynamicComponents.getOriginal().get(DataComponents.ATTRIBUTE_MODIFIERS));
                    builder.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
                }

                dynamicComponents.setExchangeable(builder.build());
            }

            if (incompatibles != null) {
                configurationProvider.getCombatEdit().warnAboutItemIncompatibility(incompatibles);
            }
        }
    }

    public class Entities implements DefaultsSupplier.Entities {
        private EntityModificationProvider modificationProvider = new EntityModificationMap(Map.of());

        public EntityModificationProvider modificationProvider() {
            return modificationProvider;
        }

        @Override
        public AttributeSupplier getVanillaDefaultAttributes(EntityType<? extends LivingEntity> entityType) {
            var defaultAttributes = DefaultAttributes.getSupplier(entityType);
            if (!(defaultAttributes instanceof DynamicAttributeSupplier dynamicDefaults)) {
                return defaultAttributes;
            }
            return dynamicDefaults.getOriginal();
        }

        private void reloadModificationProvider() {
            Configuration configuration = configurationProvider.getConfiguration();
            modificationProvider = EntityModificationMap.fromConfiguration(configuration.getEntityAttributes(), entities);
        }

        private void modify() {
            List<EntityType<? extends LivingEntity>> incompatibles = null;
            for (EntityType<? extends LivingEntity> type : DefaultAttributes.SUPPLIERS.keySet()) {
                Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                var defaults = DefaultAttributes.getSupplier(type);
                if (!(defaults instanceof DynamicAttributeSupplier entry)) {
                    if (!modificationProvider.shouldModifyEntity(id, type)) {
                        continue;
                    }
                    if (incompatibles == null) {
                        incompatibles = new ArrayList<>();
                    }
                    incompatibles.add(type);
                    continue;
                }

                var entryExt = (AttributeSupplierExtensions) entry;
                var previousDefaults = entry.getExchangeable();
                if (!modificationProvider.shouldModifyEntity(id, type)) {
                    entry.setExchangeable(entry.getOriginal());
                } else {
                    entryExt.combatEdit$setSendAllAttributes(true);
                    entry.setExchangeable(modificationProvider.getModifiers(id, type, entry.getOriginal()));
                }

                updateEntitiesAttributeContainers(type, previousDefaults);
            }

            if (incompatibles != null) {
                configurationProvider.getCombatEdit().warnAboutEntityIncompatibility(incompatibles);
            }
        }
    }
}
