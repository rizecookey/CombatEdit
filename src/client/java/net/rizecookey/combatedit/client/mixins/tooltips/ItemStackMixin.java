package net.rizecookey.combatedit.client.mixins.tooltips;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.utils.ReservedUuids;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyExpressionValue(method = "appendAttributeModifierTooltip", at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;ATTACK_DAMAGE_MODIFIER_ID:Ljava/util/UUID;", ordinal = 0, opcode = Opcodes.GETSTATIC))
    private UUID enableOrDisableGreenTooltipForAttackDamage(UUID original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        var shouldBeGreen = modifier.uuid() == Item.ATTACK_DAMAGE_MODIFIER_ID || modifier.uuid().equals(ReservedUuids.ATTACK_DAMAGE_MODIFIER_ID_ALT);
        var enableTooltip = !settings.getClientOnly().shouldDisableNewTooltips() && shouldBeGreen;
        return enableTooltip ? modifier.uuid() : null;
    }

    @ModifyExpressionValue(method = "appendAttributeModifierTooltip", at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;ATTACK_SPEED_MODIFIER_ID:Ljava/util/UUID;", ordinal = 0, opcode = Opcodes.GETSTATIC))
    private UUID enableOrDisableGreenTooltipForAttackSpeed(UUID original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        var shouldBeGreen = modifier.uuid() == Item.ATTACK_SPEED_MODIFIER_ID || modifier.uuid().equals(ReservedUuids.ATTACK_SPEED_MODIFIER_ID_ALT);
        var enableTooltip = !settings.getClientOnly().shouldDisableNewTooltips() && shouldBeGreen;
        return enableTooltip ? modifier.uuid() : null;
    }
}
