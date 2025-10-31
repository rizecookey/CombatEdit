package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.rizecookey.combatedit.utils.ComponentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @ModifyVariable(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component addFallback(Component value) {
        return ComponentUtils.fallBackToServerTranslation(value);
    }
}
