package net.rizecookey.combatedit.mixins.extension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.rizecookey.combatedit.extension.AttributeSupplierExtensions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(AttributeSupplier.class)
public class AttributeSupplierMixin implements AttributeSupplierExtensions {
    @Shadow @Final @Mutable
    private Map<Holder<Attribute>, AttributeInstance> instances;
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
    public Map<Holder<Attribute>, AttributeInstance> combatEdit$getInstances() {
        return instances;
    }

    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;instances:Ljava/util/Map;", opcode = Opcodes.GETFIELD))
    private Map<Holder<Attribute>, AttributeInstance> useGetter(Map<Holder<Attribute>, AttributeInstance> previous) {
        return combatEdit$getInstances();
    }
}
