package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public class EntityTypeMixin<T extends Entity> {
    @Inject(method = "getEntityFromNbt", at = @At("HEAD"))
    private static void setSaveCall(NbtCompound nbt, World world, CallbackInfoReturnable<Optional<Entity>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "getEntityFromNbt", at = @At("RETURN"))
    private static void unsetSaveCall(NbtCompound nbt, World world, CallbackInfoReturnable<Optional<Entity>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
