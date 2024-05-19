package net.rizecookey.combatedit.extension;

import net.minecraft.entity.attribute.EntityAttributeInstance;

import java.util.Collection;

public interface DefaultAttributeContainerExtension {
    boolean combatEdit$sendAllAttributes();
    void combatEdit$setSendAllAttributes(boolean sendAll);

    Collection<EntityAttributeInstance> combatEdit$getInstances();
}
