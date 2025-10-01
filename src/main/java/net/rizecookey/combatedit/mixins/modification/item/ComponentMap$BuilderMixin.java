package net.rizecookey.combatedit.mixins.modification.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.ComponentMap;
import net.rizecookey.combatedit.extension.ComponentMapBuilderExtension;
import net.rizecookey.combatedit.extension.DynamicComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComponentMap.Builder.class)
public abstract class ComponentMap$BuilderMixin implements ComponentMapBuilderExtension {
    @Unique
    private boolean combatEdit$preventDynamicWrap = false;

    @Unique private static final ThreadLocal<Boolean> COMBAT_EDIT$NO_DYNAMIC_WRAPPING = ThreadLocal.withInitial(() -> false);

    @Override
    public ComponentMap.Builder combatEdit$preventDynamicWrap() {
        combatEdit$preventDynamicWrap = true;
        return (ComponentMap.Builder) (Object) this;
    }

    @Inject(method = "build()Lnet/minecraft/component/ComponentMap;", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/ComponentMap$Builder;build(Ljava/util/Map;)Lnet/minecraft/component/ComponentMap;"))
    private void preventDynamicWrapOnBuild(CallbackInfoReturnable<ComponentMap> cir) {
        COMBAT_EDIT$NO_DYNAMIC_WRAPPING.set(combatEdit$preventDynamicWrap);
    }

    @ModifyReturnValue(method = "build(Ljava/util/Map;)Lnet/minecraft/component/ComponentMap;", at = @At("RETURN"))
    private static ComponentMap replaceWithDynamic(ComponentMap original) {
        boolean noDynamicWrap = COMBAT_EDIT$NO_DYNAMIC_WRAPPING.get();
        COMBAT_EDIT$NO_DYNAMIC_WRAPPING.remove();
        if (noDynamicWrap) {
            return original;
        }

        return new DynamicComponentMap(original);
    }
}
