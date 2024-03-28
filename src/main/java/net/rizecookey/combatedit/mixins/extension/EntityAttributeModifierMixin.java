package net.rizecookey.combatedit.mixins.extension;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.rizecookey.combatedit.extension.EntityAttributeModifierExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityAttributeModifier.class)
public class EntityAttributeModifierMixin implements EntityAttributeModifierExtension {
    @Shadow @Final private String name;

    @Override
    public String getName() {
        return this.name;
    }
}
