package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.world.entity.Entity;
import net.rizecookey.combatedit.extension.AttributeMapExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "save", at = @At("HEAD"))
    private void setSaveCall(CallbackInfoReturnable<Boolean> cir) {
        AttributeMapExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void unsetSaveCall(CallbackInfoReturnable<Boolean> cir) {
        AttributeMapExtension.IS_SAVE_CALL.get().pop();
    }
}
