package net.rizecookey.combatedit;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.TypeFilter;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.entity_modification.EntityAttributeMap;
import net.rizecookey.combatedit.entity_modification.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtension;
import net.rizecookey.combatedit.extension.ItemExtension;
import net.rizecookey.combatedit.extension.MutableDefaultAttributeContainer;
import net.rizecookey.combatedit.item_modification.ItemAttributeMap;
import net.rizecookey.combatedit.item_modification.ItemAttributeModifierProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributesModifier {
    private final ServerConfigurationManager configurationProvider;
    private ItemAttributeModifierProvider currentItemModifierProvider;
    private EntityAttributeModifierProvider currentEntityModifierProvider;

    private ItemAttributeModifierProvider previousItemModifierProvider;
    private EntityAttributeModifierProvider previousEntityModifierProvider;

    private boolean registriesModified = false;
    private final Map<Pair<Identifier, Item>, AttributeModifiersComponent> originalItemModifiers = new HashMap<>();
    private Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalEntityModifiers;

    public AttributesModifier(ServerConfigurationManager configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public boolean areRegistriesModified() {
        return registriesModified;
    }

    public void makeModifications() {
        previousEntityModifierProvider = currentEntityModifierProvider;
        previousItemModifierProvider = currentItemModifierProvider;

        reloadModifierProviders();
        modifyItemAttributes();
        modifyEntityAttributes();

        previousEntityModifierProvider = null;
        previousItemModifierProvider = null;

        registriesModified = true;
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
            var idItemPair = new Pair<>(id, item);
            ItemExtension itemExt = (ItemExtension) item;

            if (!areRegistriesModified()) {
                var originalModifiers = itemExt.combatEdit$getAttributeModifiers();
                originalItemModifiers.put(idItemPair, originalModifiers);
            }

            if (!currentItemModifierProvider.shouldModifyItem(id, item)) {
                if (previousItemModifierProvider != null && previousItemModifierProvider.shouldModifyItem(id, item)) {
                    itemExt.combatEdit$setAttributeModifiers(originalItemModifiers.get(idItemPair));
                }
                continue;
            }

            var attributeModifiers = currentItemModifierProvider.getModifiers(id, item, itemExt.combatEdit$getAttributeModifiers());
            itemExt.combatEdit$setAttributeModifiers(attributeModifiers);
        }
    }

    private void modifyEntityAttributes() {
        if (!areRegistriesModified()) {
            originalEntityModifiers = DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.entrySet()
                    .stream()
                    .map(entry -> Map.entry(entry.getKey(), MutableDefaultAttributeContainer.copyOf(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            var defaultsEntry = DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.get(type);
            var isModifiedInCurrentConfiguration = currentEntityModifierProvider.shouldModifyEntity(id, type);
            if (!isModifiedInCurrentConfiguration && (previousEntityModifierProvider == null || !previousEntityModifierProvider.shouldModifyEntity(id, type))) {
                continue;
            }

            var originalEntry = MutableDefaultAttributeContainer.copyOf(defaultsEntry);
            DefaultAttributeContainer modifiedEntry = isModifiedInCurrentConfiguration
                    ? currentEntityModifierProvider.getModifiers(id, type, DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.get(type))
                    : originalEntityModifiers.get(type);
            ((DefaultAttributeContainerExtension) modifiedEntry).combatEdit$setSendAllAttributes(isModifiedInCurrentConfiguration);
            ((MutableDefaultAttributeContainer) defaultsEntry).replaceValuesBy(modifiedEntry);
            updateEntitiesAttributeContainers(type, originalEntry);
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

    public void revertModifications() {
        if (!registriesModified) {
            return;
        }

        for (Pair<Identifier, Item> pair : originalItemModifiers.keySet()) {
            ((ItemExtension) pair.getRight()).combatEdit$setAttributeModifiers(originalItemModifiers.get(pair));
        }
        originalItemModifiers.clear();

        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            var entry = DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.get(type);
            ((MutableDefaultAttributeContainer) entry).replaceValuesBy(originalEntityModifiers.get(type));
        }
        originalEntityModifiers = null;

        currentEntityModifierProvider = null;
        currentItemModifierProvider = null;
        registriesModified = false;
    }

    public DefaultAttributeContainer getOriginalDefaults(EntityType<? extends LivingEntity> type) {
        if (!registriesModified) {
            return DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.get(type);
        }
        return originalEntityModifiers.get(type);
    }

    public AttributeModifiersComponent getOriginalDefaults(Item item) {
        if (!registriesModified) {
            return ((ItemExtension) item).combatEdit$getAttributeModifiers();
        }

        return originalItemModifiers.getOrDefault(new Pair<>(Registries.ITEM.getId(item), item), AttributeModifiersComponent.DEFAULT);
    }
}
