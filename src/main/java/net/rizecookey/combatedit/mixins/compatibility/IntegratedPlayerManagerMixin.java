package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.server.integrated.IntegratedPlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedPlayerManager.class)
public class IntegratedPlayerManagerMixin {
    @Inject(method = "savePlayerData", at = @At("HEAD"))
    private void setSaveCall(ServerPlayerEntity player, CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "savePlayerData", at = @At("RETURN"))
    private void unsetSaveCall(ServerPlayerEntity player, CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
