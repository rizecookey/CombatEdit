package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.rizecookey.combatedit.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @ModifyVariable(method = "sendMessageToClient", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Text addFallback(Text value) {
        return TextUtils.fallBackToServerTranslation(value);
    }
}
