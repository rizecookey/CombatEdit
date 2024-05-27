package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At("HEAD"))
    private void setSaveCall(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "loadPlayerData", at = @At("RETURN"))
    private void unsetSaveCall(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
