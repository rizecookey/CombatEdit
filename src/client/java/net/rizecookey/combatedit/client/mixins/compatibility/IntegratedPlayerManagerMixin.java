package net.rizecookey.combatedit.client.mixins.compatibility;

import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedPlayerList.class)
public class IntegratedPlayerManagerMixin {
    @Inject(method = "save", at = @At("HEAD"))
    private void setSaveCall(ServerPlayer player, CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void unsetSaveCall(ServerPlayer player, CallbackInfo ci) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
