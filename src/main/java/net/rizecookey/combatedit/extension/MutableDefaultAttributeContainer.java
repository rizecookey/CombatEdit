package net.rizecookey.combatedit.extension;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MutableDefaultAttributeContainer extends DefaultAttributeContainer {
    private final Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instancesReference;

    private MutableDefaultAttributeContainer(Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances) {
        super(instances);
        this.instancesReference = instances;
    }

    public static MutableDefaultAttributeContainer create(Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances) {
        return new MutableDefaultAttributeContainer(new HashMap<>(instances));
    }

    public static MutableDefaultAttributeContainer copyOf(DefaultAttributeContainer container) {
        var newContainer = MutableDefaultAttributeContainer.create(Map.of());
        newContainer.replaceValuesBy(container);

        return newContainer;
    }

    public void replaceValuesBy(DefaultAttributeContainer newContainer) {
        this.instancesReference.clear();
        this.instancesReference.putAll(((DefaultAttributeContainerExtension) newContainer).combatEdit$getInstances()
                .stream()
                .collect(Collectors.toMap(EntityAttributeInstance::getAttribute, (EntityAttributeInstance instance) -> instance)));

        ((DefaultAttributeContainerExtension) this).combatEdit$setSendAllAttributes(((DefaultAttributeContainerExtension) newContainer).combatEdit$sendAllAttributes());
    }
}
