package net.rizecookey.combatedit.mixins.compatibility;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.rizecookey.combatedit.extension.AttributeContainerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(LevelProperties.class)
public class LevelPropertiesMixin {
    @Inject(method = "readProperties", at = @At("HEAD"))
    private static <T> void setSaveCall(Dynamic<T> dynamic, LevelInfo info, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().push(true);
    }

    @Inject(method = "readProperties", at = @At("RETURN"))
    private static <T> void unsetSaveCall(Dynamic<T> dynamic, LevelInfo info, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        AttributeContainerExtension.IS_SAVE_CALL.get().pop();
    }
}
