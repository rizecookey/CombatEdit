package net.rizecookey.combatedit.client.mixins.tooltips;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.utils.ReservedUuids;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private boolean isTooltipGreen;

    @Unique
    private boolean isDefaultTooltip;

    @Inject(method = "appendAttributeModifierTooltip", at = @At("HEAD"))
    private void resetTooltipFields(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo ci) {
        isTooltipGreen = false;
        isDefaultTooltip = false;
    }

    @ModifyExpressionValue(method = "appendAttributeModifierTooltip", at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;ATTACK_DAMAGE_MODIFIER_ID:Ljava/util/UUID;", ordinal = 0, opcode = Opcodes.GETSTATIC))
    private UUID enableOrDisableGreenTooltipForAttackDamage(UUID original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        if (isDefaultTooltip) {
            return original;
        }

        isDefaultTooltip = modifier.uuid() == Item.ATTACK_DAMAGE_MODIFIER_ID || modifier.uuid().equals(ReservedUuids.ATTACK_DAMAGE_MODIFIER_ID_ALT);
        isTooltipGreen = !settings.getClientOnly().shouldDisableNewTooltips() && isDefaultTooltip;
        return isTooltipGreen ? modifier.uuid() : null;
    }

    @ModifyExpressionValue(method = "appendAttributeModifierTooltip", at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;ATTACK_SPEED_MODIFIER_ID:Ljava/util/UUID;", ordinal = 0, opcode = Opcodes.GETSTATIC))
    private UUID enableOrDisableGreenTooltipForAttackSpeed(UUID original, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        if (isDefaultTooltip) {
            return original;
        }

        isDefaultTooltip = modifier.uuid() == Item.ATTACK_SPEED_MODIFIER_ID || modifier.uuid().equals(ReservedUuids.ATTACK_SPEED_MODIFIER_ID_ALT);
        isTooltipGreen = !settings.getClientOnly().shouldDisableNewTooltips() && isDefaultTooltip;
        return isTooltipGreen ? modifier.uuid() : null;
    }

    @SuppressWarnings("deprecation")
    @ModifyVariable(method = "appendAttributeModifierTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;operation()Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;", ordinal = 0), ordinal = 0)
    private double addEnchantmentModifiers(double previous, @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute, @Local(argsOnly = true) EntityAttributeModifier modifier) {
        if (!isDefaultTooltip || isTooltipGreen || !attribute.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            return previous;
        }
        return previous + EnchantmentHelper.getAttackDamage((ItemStack) (Object) this, null);
    }
}
