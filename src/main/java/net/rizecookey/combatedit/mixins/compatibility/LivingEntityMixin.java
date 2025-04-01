package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {
    @Shadow
    public abstract AttributeContainer getAttributes();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "readCustomDataFromNbt", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmountUnclamped(F)V"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/nbt/NbtOps;INSTANCE:Lnet/minecraft/nbt/NbtOps;")),
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", ordinal = 0), index = 0)
    public Consumer<? super NbtList> useNewDefaults(Consumer<? super NbtList> action) {
        return list -> {
            action.accept(list);
            if (!AttributeContainerExtension.IS_SAVE_CALL.get().getFirst()) {
                return;
            }

            @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
            ((AttributeContainerExtension) getAttributes()).combatEdit$patchWithNewDefaults(type, combatEdit$configurationManager().getModifier().getOriginalDefaults(type));
        };
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
