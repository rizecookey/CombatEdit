package net.rizecookey.combatedit.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.rizecookey.combatedit.client.event.ClientEvents;
import net.rizecookey.combatedit.client.extension.MinecraftExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtension {
    @Unique
    private List<Function<Runnable, Screen>> additionalInitScreens;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initalize(GameConfig args, CallbackInfo ci) {
        this.additionalInitScreens = new ArrayList<>();
    }

    @Inject(method = "addInitialScreens", at = @At("TAIL"))
    private void addCustomInitScreens(List<Function<Runnable, Screen>> list, CallbackInfoReturnable<Boolean> cir) {
        list.addAll(additionalInitScreens);
    }

    @Inject(method = "onResourceLoadFinished", at = @At("TAIL"))
    private void callFinishedLoadingEvent(CallbackInfo ci) {
        ClientEvents.CLIENT_FINISHED_LOADING.invoker().onClientFinishedLoading((Minecraft) (Object) this);
    }

    @Override
    public void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider) {
        this.additionalInitScreens.add(screenProvider);
    }
}
