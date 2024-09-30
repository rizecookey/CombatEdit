package net.rizecookey.combatedit.mixins.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import net.rizecookey.combatedit.configuration.provider.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique
    private ServerConfigurationManager serverConfigurationManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createConfigurationProvider(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        serverConfigurationManager = ServerConfigurationManager.getInstance();
        serverConfigurationManager.setCurrentServer(((MinecraftServer) (Object) this));
    }

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer$ResourceManagerHolder;close()V", shift = At.Shift.AFTER))
    private void revertRegistryModifications(CallbackInfo ci) {
        LOGGER.info("Reverting entity and item attribute modifications...");
        serverConfigurationManager.revertModifications();
        serverConfigurationManager.setCurrentServer(null);
        LOGGER.info("Done.");
    }
}
