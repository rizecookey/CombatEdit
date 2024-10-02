package net.rizecookey.combatedit.mixins.modification.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import net.rizecookey.combatedit.utils.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {
    @Shadow public abstract AttributeContainer getAttributes();

    @Unique
    private long lastKnownReload;

    @Unique
    private List<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> trackedModifiers;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
        trackedModifiers = new ArrayList<>();
    }

    @Inject(method = "getEquipmentChanges", at = @At("HEAD"))
    private void clearAfterReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (lastKnownReload >= combatEdit$configurationManager().getLastAttributeReload()) {
            return;
        }

        for (var trackedModifier : trackedModifiers) {
            var instance = getAttributes().getCustomInstance(trackedModifier.first());
            if (instance != null) {
                instance.removeModifier(trackedModifier.second());
            }
        }
        trackedModifiers.clear();
    }

    @ModifyVariable(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), ordinal = 0)
    private ItemStack useEmptyStackToForceModifierReload(ItemStack previous) {
        return lastKnownReload < combatEdit$configurationManager().getLastAttributeReload() ? ItemStack.EMPTY : previous;
    }

    @ModifyArg(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> removeOldItemModifiers(BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, @Local(ordinal = 0) ItemStack target) {
        return (entry, modifier) -> {
            trackedModifiers.remove(new Pair<>(entry, modifier));
            attributeModifierConsumer.accept(entry, modifier);
        };
    }

    @ModifyArg(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 1))
    private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> storeItemModifiers(BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, @Local(ordinal = 1) ItemStack target) {
        return (entry, modifier) -> {
            trackedModifiers.add(new Pair<>(entry, modifier));
            attributeModifierConsumer.accept(entry, modifier);
        };
    }

    @Inject(method = "getEquipmentChanges", at = @At("RETURN"))
    private void setLastKnownReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (getWorld().isClient()) {
            return;
        }
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
    }
}
