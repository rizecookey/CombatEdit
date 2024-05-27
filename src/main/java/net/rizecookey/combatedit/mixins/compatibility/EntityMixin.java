package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "saveNbt", at = @At("HEAD"))
    private void setSaveCall(NbtCompound nbt, CallbackInfoReturnable<Boolean> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "saveNbt", at = @At("RETURN"))
    private void unsetSaveCall(NbtCompound nbt, CallbackInfoReturnable<Boolean> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
