package net.rizecookey.combatedit.extension;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

public interface DefaultAttributeContainerInstancesProvider {
    Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> combatEdit$getInstances();
}
