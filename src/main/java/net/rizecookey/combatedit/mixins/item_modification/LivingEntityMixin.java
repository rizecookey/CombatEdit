package net.rizecookey.combatedit.mixins.item_modification;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityExtension {
    @Unique
    private long lastKnownReload;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void saveLastKnownReload(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
    }

    @ModifyExpressionValue(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;areItemsDifferent(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean forceReloadItemModifiers(boolean original) {
        return original || lastKnownReload < combatEdit$configurationManager().getLastAttributeReload();
    }

    @Inject(method = "getEquipmentChanges", at = @At("TAIL"))
    private void setLastKnownReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        lastKnownReload = combatEdit$configurationManager().getLastAttributeReload();
    }
}
