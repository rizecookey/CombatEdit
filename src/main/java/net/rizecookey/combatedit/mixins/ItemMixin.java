package net.rizecookey.combatedit.mixins;

import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.rizecookey.combatedit.item.WeaponStats;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public abstract class ItemMixin {
    @Mixin(SwordItem.class)
    public static class SwordItemMixin extends ToolItem {

        @Mutable
        @Shadow @Final private float attackDamage;

        public SwordItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/item/SwordItem;attackDamage:F"))
        public void changeAttackDamage(SwordItem swordItem, float f) {
            this.attackDamage = WeaponStats.getAttackDamage(this.getClass(), this.getMaterial());
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
            return builder;
        }
    }
    @Mixin(MiningToolItem.class)
    public static class MiningToolItemMixin extends ToolItem {

        @Mutable
        @Shadow @Final private float attackDamage;

        public MiningToolItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/item/MiningToolItem;attackDamage:F"))
        public void changeAttackDamage(MiningToolItem miningToolItem, float value) {
            this.attackDamage = WeaponStats.getAttackDamage(this.getClass(), this.getMaterial());
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
            return builder;
        }
    }
    @Mixin(TridentItem.class)
    public static class TridentItemMixin extends Item {
        public TridentItemMixin(Settings settings) {
            super(settings);
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 0))
        public Builder<EntityAttribute, EntityAttributeModifier> changeAttackDamage(Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
            return builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 6.0D, EntityAttributeModifier.Operation.ADDITION));
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
            return builder;
        }
    }
}
