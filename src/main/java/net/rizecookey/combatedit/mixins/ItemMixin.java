package net.rizecookey.combatedit.mixins;

import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public abstract class ItemMixin {
    @Mixin(SwordItem.class)
    public static class SwordItemMixin extends ToolItem {

        public SwordItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "getModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 1))
        public boolean cancelAttackSpeedModifier(Multimap multimap, Object key, Object value) {
            return false;
        }
    }
    @Mixin(MiningToolItem.class)
    public static class MiningToolItemMixin extends ToolItem {

        public MiningToolItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "getModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 1))
        public boolean cancelAttackSpeedModifier(Multimap multimap, Object key, Object value) {
            return false;
        }
    }
    @Mixin(TridentItem.class)
    public static class TridentItemMixin extends Item {

        public TridentItemMixin(Settings settings) {
            super(settings);
        }

        @Redirect(method = "getModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 0))
        public boolean changeAttackDamage(Multimap multimap, Object key, Object value) {
            return multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Tool modifier", 6.0D, EntityAttributeModifier.Operation.ADDITION));
        }

        @Redirect(method = "getModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 1))
        public boolean cancelAttackSpeedModifier(Multimap multimap, Object key, Object value) {
            return false;
        }
    }
    @Mixin(HoeItem.class)
    public static class HoeItemMixin extends ToolItem {

        public HoeItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "getModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z", ordinal = 1))
        public boolean cancelAttackSpeedModifier(Multimap multimap, Object key, Object value) {
            return false;
        }
    }
}
