package net.rizecookey.combatedit.client.mixins;

import net.minecraft.client.Minecraft;
import net.rizecookey.combatedit.client.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "onResourceLoadFinished", at = @At("TAIL"))
    private void callFinishedLoadingEvent(CallbackInfo ci) {
        ClientEvents.CLIENT_FINISHED_LOADING.invoker().onClientFinishedLoading((Minecraft) (Object) this);
    }
}
