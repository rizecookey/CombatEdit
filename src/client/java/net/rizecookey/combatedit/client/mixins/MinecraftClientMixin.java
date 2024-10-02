package net.rizecookey.combatedit.client.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.rizecookey.combatedit.client.event.ClientEvents;
import net.rizecookey.combatedit.client.extension.MinecraftClientExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements MinecraftClientExtension {
    @Unique
    private List<Function<Runnable, Screen>> additionalInitScreens;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initalize(RunArgs args, CallbackInfo ci) {
        this.additionalInitScreens = new ArrayList<>();
    }

    @Inject(method = "createInitScreens", at = @At("TAIL"))
    private void addCustomInitScreens(List<Function<Runnable, Screen>> list, CallbackInfo ci) {
        list.addAll(additionalInitScreens);
    }

    @Inject(method = "onFinishedLoading", at = @At("TAIL"))
    private void callFinishedLoadingEvent(CallbackInfo ci) {
        ClientEvents.CLIENT_FINISHED_LOADING.invoker().onClientFinishedLoading((MinecraftClient) (Object) this);
    }

    @Override
    public void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider) {
        this.additionalInitScreens.add(screenProvider);
    }
}
