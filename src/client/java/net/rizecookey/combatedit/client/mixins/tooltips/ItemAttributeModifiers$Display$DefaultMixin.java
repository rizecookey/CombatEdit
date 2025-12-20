package net.rizecookey.combatedit.client.mixins.tooltips;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.utils.ReservedIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemAttributeModifiers.Display.Default.class)
public abstract class ItemAttributeModifiers$Display$DefaultMixin {
    @ModifyExpressionValue(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;is(Lnet/minecraft/resources/Identifier;)Z", ordinal = 0))
    private boolean enableOrDisableGreenTooltipForAttackDamage(boolean original, @Local(argsOnly = true) AttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        return !settings.getClientOnly().shouldDisableNewTooltips() && (original || modifier.is(ReservedIdentifiers.ATTACK_DAMAGE_MODIFIER_ID_ALT));
    }

    @ModifyExpressionValue(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;is(Lnet/minecraft/resources/Identifier;)Z", ordinal = 1))
    private boolean enableOrDisableGreenTooltipForAttackSpeed(boolean original, @Local(argsOnly = true) AttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        return !settings.getClientOnly().shouldDisableNewTooltips() && (original || modifier.is(ReservedIdentifiers.ATTACK_SPEED_MODIFIER_ID_ALT));
    }
}
