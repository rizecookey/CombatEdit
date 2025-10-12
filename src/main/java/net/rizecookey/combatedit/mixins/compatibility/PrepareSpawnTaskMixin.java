package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "net.minecraft.server.network.PrepareSpawnTask$PlayerSpawn")
public class PrepareSpawnTaskMixin {
    @Inject(method = "onReady", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;loadPlayerData(Lnet/minecraft/server/PlayerConfigEntry;)Ljava/util/Optional;"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V")
    ), at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private void setSaveCall(CallbackInfoReturnable<Optional<ReadView>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "onReady", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;loadPlayerData(Lnet/minecraft/server/PlayerConfigEntry;)Ljava/util/Optional;"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V")
    ), at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void unsetSaveCall(CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
