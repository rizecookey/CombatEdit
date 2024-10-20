package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.world.PlayerSaveHandler;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSaveHandler.class)
public class PlayerSaveHandlerMixin {
    @Inject(method = { "loadPlayerData*", "savePlayerData" }, at = @At("HEAD"))
    private void setSaveCall(CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = { "loadPlayerData*", "savePlayerData" }, at = @At("RETURN"))
    private void unsetSaveCall(CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
