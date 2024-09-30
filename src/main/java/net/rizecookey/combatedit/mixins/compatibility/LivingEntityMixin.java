package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {
    @Shadow public abstract AttributeContainer getAttributes();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;readNbt(Lnet/minecraft/nbt/NbtList;)V", shift = At.Shift.AFTER))
    private void useNewDefaults(NbtCompound nbt, CallbackInfo ci) {
        if (!AttributeContainerExtension.IS_SAVE_CALL.get().getFirst()) {
            return;
        }

        @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
        ((AttributeContainerExtension) getAttributes()).combatEdit$patchWithNewDefaults(type, combatEdit$configurationManager().getModifier().getOriginalDefaults(type));
    }

    @ModifyExpressionValue(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributes()Lnet/minecraft/entity/attribute/AttributeContainer;"))
    private AttributeContainer useOldDefaults(AttributeContainer original) {
        if (!AttributeContainerExtension.IS_SAVE_CALL.get().getFirst()) {
            return original;
        }

        @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
        return ((AttributeContainerExtension) original).combatEdit$getWithOriginalDefaults(type);
    }
}
