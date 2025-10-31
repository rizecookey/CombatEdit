package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.rizecookey.combatedit.extension.AttributeMapExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    @Inject(method = "create*", at = @At("HEAD"))
    private static void setSaveCall(CallbackInfoReturnable<Optional<Entity>> cir) {
        AttributeMapExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "create*", at = @At("RETURN"))
    private static void unsetSaveCall(CallbackInfoReturnable<Optional<Entity>> cir) {
        AttributeMapExtension.IS_SAVE_CALL.get().pop();
    }
}
