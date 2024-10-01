package net.rizecookey.combatedit.mixins.client.compatibility;

import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public class ConnectScreenInnerThreadMixin {
    @Inject(method = "run", at = @At("HEAD"))
    private void ensureRegistriesAreUnmodified(CallbackInfo ci) {
        var configurationManager = ServerConfigurationManager.getInstance();
        var modifier = configurationManager.getModifier();
        if (modifier.areRegistriesModified()) {
            configurationManager.revertModifications();
        }
    }
}
