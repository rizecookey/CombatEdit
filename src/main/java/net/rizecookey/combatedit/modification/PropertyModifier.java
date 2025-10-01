package net.rizecookey.combatedit.modification;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtensions;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import net.rizecookey.combatedit.extension.DynamicDefaultAttributeContainer;
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

    private void updateEntitiesAttributeContainers(EntityType<? extends LivingEntity> type, DefaultAttributeContainer previousDefaults) {
        MinecraftServer server;
        if ((server = configurationProvider.getCurrentServer()) == null) {
            return;
        }

        server.getWorlds()
                .forEach(world -> world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), entity -> entity.getType().equals(type))
                        .forEach(entity -> entity.getAttributes().combatEdit$patchWithNewDefaults(type, previousDefaults))
                );
    }

    private void updateAttributesToClients() {
        MinecraftServer currentServer = configurationProvider.getCurrentServer();
        if (currentServer == null) {
            return;
        }

        for (var player : currentServer.getPlayerManager().getPlayerList()) {
            player.currentScreenHandler.updateToClient();
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
        public ComponentMap getVanillaComponents(Item item) {
            var components = item.getComponents();
            if (!(components instanceof DynamicComponentMap dynamicComponentMap)) {
                return components;
            }

            return dynamicComponentMap.getOriginal();
        }

        @Override
        public AttributeModifiersComponent getVanillaAttributeModifiers(Item item) {
            var components = getVanillaComponents(item);
            return Objects.requireNonNullElse(components.get(DataComponentTypes.ATTRIBUTE_MODIFIERS),
                    AttributeModifiersComponent.DEFAULT);
        }

        private void reloadModificationProvider() {
            Configuration configuration = configurationProvider.getConfiguration();
            modificationProvider = ItemModificationMap.fromConfiguration(configuration.getItemAttributes(), configuration.getItemComponents(), items);
        }

        @SuppressWarnings("unchecked")
        private void modify() {
            List<Item> incompatibles = null;
            for (Item item : Registries.ITEM) {
                Identifier id = Registries.ITEM.getId(item);
                ComponentMap components = item.getComponents();
                if (!(components instanceof DynamicComponentMap dynamicComponents)) {
                    if (!modificationProvider.shouldModifyAttributes(id, item) && !modificationProvider.shouldModifyDefaultComponents(id, item)) {
                        continue;
                    }
                    if (incompatibles == null) {
                        incompatibles = new ArrayList<>();
                    }
                    incompatibles.add(item);
                    continue;
                }

                var builder = ComponentMap.builder();
                modificationProvider.getComponents(id, item, dynamicComponents.getOriginal())
                        .forEach(component -> builder.add((ComponentType<Object>) component.type(), component.value()));

                if (modificationProvider.shouldModifyAttributes(id, item)) {
                    var modifiers = modificationProvider.getAttributeModifiers(id, item, dynamicComponents.getOriginal().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
                    builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers);
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
        public DefaultAttributeContainer getVanillaDefaultAttributes(EntityType<? extends LivingEntity> entityType) {
            var defaultAttributes = DefaultAttributeRegistry.get(entityType);
            if (!(defaultAttributes instanceof DynamicDefaultAttributeContainer dynamicDefaults)) {
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
            for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
                Identifier id = Registries.ENTITY_TYPE.getId(type);
                var defaults = DefaultAttributeRegistry.get(type);
                if (!(defaults instanceof DynamicDefaultAttributeContainer entry)) {
                    if (!modificationProvider.shouldModifyEntity(id, type)) {
                        continue;
                    }
                    if (incompatibles == null) {
                        incompatibles = new ArrayList<>();
                    }
                    incompatibles.add(type);
                    continue;
                }

                var entryExt = (DefaultAttributeContainerExtensions) entry;
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
