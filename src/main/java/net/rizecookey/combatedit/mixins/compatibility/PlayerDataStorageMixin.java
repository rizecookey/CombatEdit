package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.world.level.storage.PlayerDataStorage;
import net.rizecookey.combatedit.extension.AttributeMapExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin {
    @Inject(method = { "load*", "save" }, at = @At("HEAD"))
    private void setSaveCall(@Coerce CallbackInfo ci) {
        AttributeMapExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = { "load*", "save" }, at = @At("RETURN"))
    private void unsetSaveCall(@Coerce CallbackInfo ci) {
        AttributeMapExtension.IS_SAVE_CALL.get().pop();
    }
}
