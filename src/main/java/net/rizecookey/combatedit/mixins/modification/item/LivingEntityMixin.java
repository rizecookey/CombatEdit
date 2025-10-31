package net.rizecookey.combatedit.mixins.modification.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
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
    @Shadow public abstract AttributeMap getAttributes();

    @Unique
    private long lastKnownReload;

    @Unique
    private List<Pair<Holder<Attribute>, AttributeModifier>> trackedModifiers;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(EntityType<? extends LivingEntity> entityType, Level world, CallbackInfo ci) {
        if (world.isClientSide()) {
            return;
        }
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
        trackedModifiers = new ArrayList<>();
    }

    @Inject(method = "collectEquipmentChanges", at = @At("HEAD"))
    private void clearAfterReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (lastKnownReload >= combatEdit$configurationManager().getLastAttributeReload()) {
            return;
        }

        for (var trackedModifier : trackedModifiers) {
            var instance = getAttributes().getInstance(trackedModifier.first());
            if (instance != null) {
                instance.removeModifier(trackedModifier.second());
            }
        }

        trackedModifiers.clear();

        EnchantmentHelper.stopLocationBasedEffects(((LivingEntity) (Object) this));
    }

    @ModifyVariable(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), ordinal = 0)
    private ItemStack useEmptyStackToForceModifierReload(ItemStack previous) {
        return lastKnownReload < combatEdit$configurationManager().getLastAttributeReload() ? ItemStack.EMPTY : previous;
    }

    @ModifyArg(method = "stopLocationBasedEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private BiConsumer<Holder<Attribute>, AttributeModifier> removeOldItemModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer) {
        return (entry, modifier) -> {
            trackedModifiers.remove(new Pair<>(entry, modifier));
            attributeModifierConsumer.accept(entry, modifier);
        };
    }

    @ModifyArg(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private BiConsumer<Holder<Attribute>, AttributeModifier> storeItemModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer) {
        return (entry, modifier) -> {
            trackedModifiers.add(new Pair<>(entry, modifier));
            attributeModifierConsumer.accept(entry, modifier);
        };
    }

    @Inject(method = "collectEquipmentChanges", at = @At("RETURN"))
    private void setLastKnownReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (level().isClientSide()) {
            return;
        }
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
    }
}
