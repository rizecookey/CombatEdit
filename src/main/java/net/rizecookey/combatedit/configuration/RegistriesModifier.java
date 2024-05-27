package net.rizecookey.combatedit.configuration;

import com.google.common.collect.ImmutableMap;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtension;
import net.rizecookey.combatedit.extension.ItemExtension;
import net.rizecookey.combatedit.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.item.ItemAttributeModifierProvider;

import java.util.HashMap;
import java.util.Map;

public class RegistriesModifier {
    private boolean registriesModified = false;
    private final Map<Pair<Identifier, Item>, AttributeModifiersComponent> originalItemModifiers = new HashMap<>();
    private Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> originalEntityModifiers;

    public void makeModifications(ItemAttributeModifierProvider itemModifiers, EntityAttributeModifierProvider entityModifiers) {
        modifyItemAttributes(itemModifiers);
        modifyEntityAttributes(entityModifiers);

        registriesModified = true;
    }

    private void modifyItemAttributes(ItemAttributeModifierProvider provider) {
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);

            if (!provider.shouldModifyItem(id, item)) {
                continue;
            }

            AttributeModifiersComponent originalModifiers = ((ItemExtension) item).combatEdit$getAttributeModifiers();
            originalItemModifiers.put(new Pair<>(id, item), originalModifiers);

            AttributeModifiersComponent attributeModifiers = provider.getModifiers(id, item);
            ((ItemExtension) item).combatEdit$setAttributeModifiers(attributeModifiers);
        }
    }

    private void modifyEntityAttributes(EntityAttributeModifierProvider provider) {
        originalEntityModifiers = DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY;

        ImmutableMap.Builder<EntityType<? extends LivingEntity>, DefaultAttributeContainer> builder = ImmutableMap.builder();
        builder.putAll(DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY);

        for (EntityType<? extends LivingEntity> type : DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.keySet()) {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            if (!provider.shouldModifyEntity(id, type)) {
                continue;
            }

            DefaultAttributeContainer modifiers = provider.getModifiers(id, type);
            ((DefaultAttributeContainerExtension) modifiers).combatEdit$setSendAllAttributes(true);
            builder.put(type, modifiers);
        }

        DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY = builder.buildKeepingLast();
    }

    public void revertModifications() {
        if (!registriesModified) {
            return;
        }

        for (Pair<Identifier, Item> pair : originalItemModifiers.keySet()) {
            ((ItemExtension) pair.getRight()).combatEdit$setAttributeModifiers(originalItemModifiers.get(pair));
        }
        originalItemModifiers.clear();

        DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY = originalEntityModifiers;
        originalEntityModifiers = null;

        registriesModified = false;
    }

    public DefaultAttributeContainer getOriginalDefaults(EntityType<? extends LivingEntity> type) {
        if (!registriesModified || !originalEntityModifiers.containsKey(type)) {
            return DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.get(type);
        }
        return originalEntityModifiers.get(type);
    }
}
