package net.rizecookey.combatedit.mixins.modification.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentMap;
import net.rizecookey.combatedit.extension.DataComponentMapBuilderExtension;
import net.rizecookey.combatedit.extension.DynamicDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataComponentMap.Builder.class)
public abstract class DataComponentMap$BuilderMixin implements DataComponentMapBuilderExtension {
    @Unique
    private boolean combatEdit$preventDynamicWrap = false;

    @Unique private static final ThreadLocal<Boolean> COMBAT_EDIT$NO_DYNAMIC_WRAPPING = ThreadLocal.withInitial(() -> false);

    @Override
    public DataComponentMap.Builder combatEdit$preventDynamicWrap() {
        combatEdit$preventDynamicWrap = true;
        return (DataComponentMap.Builder) (Object) this;
    }

    @Inject(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentMap$Builder;buildFromMapTrusted(Ljava/util/Map;)Lnet/minecraft/core/component/DataComponentMap;"))
    private void preventDynamicWrapOnBuild(CallbackInfoReturnable<DataComponentMap> cir) {
        COMBAT_EDIT$NO_DYNAMIC_WRAPPING.set(combatEdit$preventDynamicWrap);
    }

    @ModifyReturnValue(method = "buildFromMapTrusted", at = @At("RETURN"))
    private static DataComponentMap replaceWithDynamic(DataComponentMap original) {
        boolean noDynamicWrap = COMBAT_EDIT$NO_DYNAMIC_WRAPPING.get();
        COMBAT_EDIT$NO_DYNAMIC_WRAPPING.remove();
        if (noDynamicWrap) {
            return original;
        }

        return new DynamicDataComponentMap(original);
    }
}
