package net.rizecookey.combatedit.extension;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

public class DynamicDefaultAttributeContainer extends DefaultAttributeContainer implements DefaultAttributeContainerInstancesProvider {
    private static boolean USE_EXCHANGEABLE = false;

    private final DefaultAttributeContainer original;
    private DefaultAttributeContainer exchangeable;

    public DynamicDefaultAttributeContainer(DefaultAttributeContainer original) {
        super(Map.copyOf(extension(original).combatEdit$getInstances()));
        this.original = original;
    }

    private static DefaultAttributeContainerExtensions extension(DefaultAttributeContainer container) {
        return (DefaultAttributeContainerExtensions) container;
    }

    public DefaultAttributeContainer getOriginal() {
        return original;
    }

    public DefaultAttributeContainer getExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(DefaultAttributeContainer exchangeable) {
        this.exchangeable = exchangeable;
    }

    public static boolean shouldUseExchangeable() {
        return USE_EXCHANGEABLE;
    }

    public static void setUseExchangeable(boolean useExchangeable) {
        USE_EXCHANGEABLE = useExchangeable;
    }

    @Override
    public Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> combatEdit$getInstances() {
        return USE_EXCHANGEABLE ? extension(exchangeable).combatEdit$getInstances() : extension(original).combatEdit$getInstances();
    }
}
