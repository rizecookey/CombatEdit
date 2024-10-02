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
import net.rizecookey.combatedit.extension.ItemExtension;
import net.rizecookey.combatedit.modification.entity.EntityAttributeMap;
import net.rizecookey.combatedit.modification.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.modification.item.ItemAttributeMap;
import net.rizecookey.combatedit.modification.item.ItemAttributeModifierProvider;

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
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            DynamicComponentMap components = ((ItemExtension) item).combatEdit$getDynamicComponents();

            if (!currentItemModifierProvider.shouldModifyItem(id, item)) {
                components.setExchangeable(components.getOriginal());
                continue;
            }

            var modifiers = currentItemModifierProvider.getModifiers(id, item, components.getOriginal().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
            components.setExchangeable(ComponentMap.builder()
                    .addAll(item.getComponents())
                    .add(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers)
                    .build());
        }
    }

    private void modifyEntityAttributes() {
        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            var entry = (DynamicDefaultAttributeContainer) DefaultAttributeRegistry.get(type);
            var entryExt = (DefaultAttributeContainerExtensions) entry;
            if (!currentEntityModifierProvider.shouldModifyEntity(id, type)) {
                entry.setExchangeable(entry.getOriginal());
                continue;
            }

            var previousDefaults = entry.getExchangeable();
            entryExt.combatEdit$setSendAllAttributes(true);
            entry.setExchangeable(currentEntityModifierProvider.getModifiers(id, type, entry.getOriginal()));
            updateEntitiesAttributeContainers(type, previousDefaults);
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

        // TODO picked up items lose their tooltip during reload?
    }

    public DefaultAttributeContainer getOriginalDefaults(EntityType<? extends LivingEntity> type) {
        return ((DynamicDefaultAttributeContainer) DefaultAttributeRegistry.get(type)).getOriginal();
    }

    public AttributeModifiersComponent getOriginalDefaults(Item item) {
        var itemExt = (ItemExtension) item;
        return Objects.requireNonNullElse(itemExt.combatEdit$getDynamicComponents().getOriginal().get(DataComponentTypes.ATTRIBUTE_MODIFIERS), AttributeModifiersComponent.DEFAULT);
    }
}
