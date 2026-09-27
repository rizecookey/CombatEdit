package net.rizecookey.combatedit.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.rizecookey.combatedit.client.extension.GuiExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(Gui.class)
public abstract class GuiMixin implements GuiExtension {
    @Unique
    private List<Function<Runnable, Screen>> additionalInitScreens;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialize(Minecraft minecraft, Hud hud, GuiRenderState guiRenderState, CallbackInfo ci) {
        this.additionalInitScreens = new ArrayList<>();
    }

    @Inject(method = "addInitialScreens", at = @At("TAIL"))
    private void addCustomInitScreens(List<Function<Runnable, Screen>> screens, CallbackInfoReturnable<Boolean> cir) {
        screens.addAll(additionalInitScreens);
    }

    @Override
    public void combatEdit$addInitScreen(Function<Runnable, Screen> screenProvider) {
        this.additionalInitScreens.add(screenProvider);
    }
}
