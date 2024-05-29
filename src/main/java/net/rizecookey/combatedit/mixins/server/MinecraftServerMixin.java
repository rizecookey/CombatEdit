package net.rizecookey.combatedit.mixins.server;

import net.minecraft.server.MinecraftServer;
import net.rizecookey.combatedit.CombatEdit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger(CombatEdit.class);

    @Unique
    private CombatEdit combatEdit;

    @Inject(method = "loadWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createWorlds(Lnet/minecraft/server/WorldGenerationProgressListener;)V", shift = At.Shift.BEFORE))
    private void makeRegistryModifications(CallbackInfo ci) {
        combatEdit = CombatEdit.getInstance();

        LOGGER.info("Modifying item and entity attributes...");
        combatEdit.getModifier().makeModifications();
        LOGGER.info("Done modifying attributes.");
    }

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer$ResourceManagerHolder;close()V", shift = At.Shift.AFTER))
    private void revertRegistryModifications(CallbackInfo ci) {
        LOGGER.info("Reverting item and entity attribute modifications...");
        combatEdit.getModifier().revertModifications();
        LOGGER.info("Done reverting modifications.");
    }
}
