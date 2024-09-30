package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract AttributeContainer getAttributes();

    @Unique
    private long lastKnownReload;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void saveLastKnownReload(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        lastKnownReload = configurationProvider().getLastAttributeReload();
    }

    @Unique
    private static ServerConfigurationManager configurationProvider() {
        return ServerConfigurationManager.getInstance();
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;readNbt(Lnet/minecraft/nbt/NbtList;)V", shift = At.Shift.AFTER))
    private void useNewDefaults(NbtCompound nbt, CallbackInfo ci) {
        if (!AttributeContainerExtension.IS_SAVE_CALL.get().getFirst()) {
            return;
        }

        @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
        ((AttributeContainerExtension) getAttributes()).combatEdit$patchWithNewDefaults(type, configurationProvider().getModifier().getOriginalDefaults(type));
    }

    @ModifyExpressionValue(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributes()Lnet/minecraft/entity/attribute/AttributeContainer;"))
    private AttributeContainer useOldDefaults(AttributeContainer original) {
        if (!AttributeContainerExtension.IS_SAVE_CALL.get().getFirst()) {
            return original;
        }

        @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
        return ((AttributeContainerExtension) original).combatEdit$getWithOriginalDefaults(type);
    }

    @ModifyExpressionValue(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;areItemsDifferent(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean forceReloadItemModifiers(boolean original) {
        return original || lastKnownReload < configurationProvider().getLastAttributeReload();
    }

    @Inject(method = "getEquipmentChanges", at = @At("TAIL"))
    private void setLastKnownReload(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        lastKnownReload = configurationProvider().getLastAttributeReload();
    }
}
