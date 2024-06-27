package net.rizecookey.combatedit.mixins.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.client.configscreen.InvalidConfigScreen;
import net.rizecookey.combatedit.configuration.InvalidConfigurationException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "createInitScreens", at = @At("TAIL"))
    private void validateConfig(List<Function<Runnable, Screen>> list, CallbackInfo ci) {
        try {
            CombatEdit.getInstance().getConfig().validate();
        } catch (InvalidConfigurationException e) {
            CombatEdit.LOGGER.error("CombatEdit detected errors in the configuration!", e);
            list.add(onClose -> new InvalidConfigScreen(e, onClose));
        }
    }
}
