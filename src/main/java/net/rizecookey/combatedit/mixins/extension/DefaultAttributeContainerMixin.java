package net.rizecookey.combatedit.mixins.extension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.rizecookey.combatedit.extension.DefaultAttributeContainerExtensions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(DefaultAttributeContainer.class)
public class DefaultAttributeContainerMixin implements DefaultAttributeContainerExtensions {
    @Shadow @Final @Mutable
    private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances;
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
    public Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> combatEdit$getInstances() {
        return instances;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/DefaultAttributeContainer;instances:Ljava/util/Map;", opcode = Opcodes.GETFIELD))
    private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> useGetter(Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> previous) {
        return combatEdit$getInstances();
    }
}
