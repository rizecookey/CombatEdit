package net.rizecookey.combatedit.modification;

import net.minecraft.component.ComponentMap;
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
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtensions;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import net.rizecookey.combatedit.extension.DynamicDefaultAttributeContainer;
import net.rizecookey.combatedit.modification.entity.EntityAttributeMap;
import net.rizecookey.combatedit.modification.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.modification.item.ItemAttributeMap;
import net.rizecookey.combatedit.modification.item.ItemAttributeModifierProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttributesModifier {
    private final ConfigurationManager configurationProvider;
    private ItemAttributeModifierProvider currentItemModifierProvider;
    private EntityAttributeModifierProvider currentEntityModifierProvider;

    public AttributesModifier(ConfigurationManager configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public void makeModifications() {
        reloadModifierProviders();
        modifyItemAttributes();
        modifyEntityAttributes();
        updateAttributesToClients();
    }

    public ItemAttributeModifierProvider getCurrentItemModifierProvider() {
        return currentItemModifierProvider;
    }

    public EntityAttributeModifierProvider getCurrentEntityModifierProvider() {
        return currentEntityModifierProvider;
    }

    public void reloadModifierProviders() {
        Configuration configuration = configurationProvider.getConfiguration();
        currentEntityModifierProvider = EntityAttributeMap.fromConfiguration(configuration.getEntityAttributes(), this::getOriginalDefaults);
        currentItemModifierProvider = ItemAttributeMap.fromConfiguration(configuration.getItemAttributes(), this::getOriginalDefaults);
    }

    private void modifyItemAttributes() {
        List<Item> incompatibles = null;
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            ComponentMap components = item.getComponents();
            if (!(components instanceof DynamicComponentMap dynamicComponents)) {
                if (!currentItemModifierProvider.shouldModifyItem(id, item)) {
                    continue;
                }
                if (incompatibles == null) {
                    incompatibles = new ArrayList<>();
                }
                incompatibles.add(item);
                continue;
            }

            if (!currentItemModifierProvider.shouldModifyItem(id, item)) {
                dynamicComponents.setExchangeable(dynamicComponents.getOriginal());
                continue;
            }

            var modifiers = currentItemModifierProvider.getModifiers(id, item, dynamicComponents.getOriginal().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
            dynamicComponents.setExchangeable(ComponentMap.builder()
                    .addAll(item.getComponents())
                    .add(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers)
                    .build());
        }

        if (incompatibles != null) {
            configurationProvider.getCombatEdit().warnAboutItemIncompatibility(incompatibles);
        }
    }

    private void modifyEntityAttributes() {
        List<EntityType<? extends LivingEntity>> incompatibles = null;
        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            var defaults = DefaultAttributeRegistry.get(type);
            if (!(defaults instanceof DynamicDefaultAttributeContainer entry)) {
                if (!currentEntityModifierProvider.shouldModifyEntity(id, type)) {
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
            if (!currentEntityModifierProvider.shouldModifyEntity(id, type)) {
                entry.setExchangeable(entry.getOriginal());
            } else {
                entryExt.combatEdit$setSendAllAttributes(true);
                entry.setExchangeable(currentEntityModifierProvider.getModifiers(id, type, entry.getOriginal()));
            }

            updateEntitiesAttributeContainers(type, previousDefaults);
        }

        if (incompatibles != null) {
            configurationProvider.getCombatEdit().warnAboutEntityIncompatibility(incompatibles);
        }
    }

    private void updateEntitiesAttributeContainers(EntityType<? extends LivingEntity> type, DefaultAttributeContainer previousDefaults) {
        MinecraftServer server;
        if ((server = configurationProvider.getCurrentServer()) == null) {
            return;
        }

        server.getWorlds()
                .forEach(world -> world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), entity -> entity.getType().equals(type))
                        .forEach(entity -> ((AttributeContainerExtension) entity.getAttributes()).combatEdit$patchWithNewDefaults(type, previousDefaults))
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

    public DefaultAttributeContainer getOriginalDefaults(EntityType<? extends LivingEntity> type) {
        var defaultAttributes = DefaultAttributeRegistry.get(type);
        if (!(defaultAttributes instanceof DynamicDefaultAttributeContainer dynamicDefaults)) {
            return defaultAttributes;
        }
        return dynamicDefaults.getOriginal();
    }

    public AttributeModifiersComponent getOriginalDefaults(Item item) {
        var components = item.getComponents();
        if (!(components instanceof DynamicComponentMap dynamicComponents)) {
            return components.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        }
        return Objects.requireNonNullElse(dynamicComponents.getOriginal().get(DataComponentTypes.ATTRIBUTE_MODIFIERS), AttributeModifiersComponent.DEFAULT);
    }
}
