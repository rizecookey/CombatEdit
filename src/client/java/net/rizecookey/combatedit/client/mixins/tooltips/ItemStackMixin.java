package net.rizecookey.combatedit.client.mixins.tooltips;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.utils.ReservedIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttributeModifiersComponent.Display.Default.class)
public abstract class ItemStackMixin {
    @ModifyExpressionValue(method = "addTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;idMatches(Lnet/minecraft/util/Identifier;)Z", ordinal = 0))
    private boolean enableOrDisableGreenTooltipForAttackDamage(boolean original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        return !settings.getClientOnly().shouldDisableNewTooltips() && (original || modifier.idMatches(ReservedIdentifiers.ATTACK_DAMAGE_MODIFIER_ID_ALT));
    }

    @ModifyExpressionValue(method = "addTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;idMatches(Lnet/minecraft/util/Identifier;)Z", ordinal = 1))
    private boolean enableOrDisableGreenTooltipForAttackSpeed(boolean original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        return !settings.getClientOnly().shouldDisableNewTooltips() && (original || modifier.idMatches(ReservedIdentifiers.ATTACK_SPEED_MODIFIER_ID_ALT));
    }
}
