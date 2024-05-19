package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.Map;

@Mixin(DefaultAttributeContainer.class)
public class DefaultAttributeContainerMixin implements DefaultAttributeContainerExtension {
    @Shadow @Final private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances;
    @Unique
    private boolean combatEdit$sendAllAttributes = false;

    @Override
    public boolean combatEdit$sendAllAttributes() {
        return combatEdit$sendAllAttributes;
    }

    @Override
    public void combatEdit$setSendAllAttributes(boolean sendAll) {
        combatEdit$sendAllAttributes = sendAll;
    }

    @Override
    public Collection<EntityAttributeInstance> combatEdit$getInstances() {
        return instances.values();
    }
}
