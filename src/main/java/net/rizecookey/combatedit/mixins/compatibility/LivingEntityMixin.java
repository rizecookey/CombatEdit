package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.rizecookey.combatedit.extension.AttributeMapExtension;
import net.rizecookey.combatedit.extension.LivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {
    @Shadow
    public abstract AttributeMap getAttributes();

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyArg(method = "readAdditionalSaveData", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;internalSetAbsorptionAmount(F)V"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/world/effect/MobEffectInstance;CODEC:Lcom/mojang/serialization/Codec;")),
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", ordinal = 0), index = 0)
    public Consumer<? super List<AttributeInstance.Packed>> useNewDefaults(Consumer<? super List<AttributeInstance.Packed>> action) {
        return list -> {
            action.accept(list);
            if (!AttributeMapExtension.IS_SAVE_CALL.get().getFirst()) {
                return;
            }

            @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
            getAttributes().combatEdit$patchWithNewDefaults(type,
                    combatEdit$configurationManager().getModifier().entities().getVanillaDefaultAttributes(type));
        };
    }

    @ModifyExpressionValue(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeMap;"))
    private AttributeMap useOldDefaults(AttributeMap original) {
        if (!AttributeMapExtension.IS_SAVE_CALL.get().getFirst()) {
            return original;
        }

        @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> type = ((EntityType<? extends LivingEntity>) getType());
        return original.combatEdit$getWithOriginalDefaults(type);
    }
}
