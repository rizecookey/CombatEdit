package net.rizecookey.combatedit.mixins.compatibility;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(PrimaryLevelData.class)
public class LevelPropertiesMixin {
    @Inject(method = "parse", at = @At("HEAD"))
    private static <T> void setSaveCall(Dynamic<T> dynamic, LevelSettings info, PrimaryLevelData.SpecialWorldProperty specialProperty, WorldOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static <T> void unsetSaveCall(Dynamic<T> dynamic, LevelSettings info, PrimaryLevelData.SpecialWorldProperty specialProperty, WorldOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
