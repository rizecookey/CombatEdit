package net.rizecookey.combatedit.mixins.item;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item {
    public TridentItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 0, remap = false))
    public ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> changeAttackDamage(ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
        return builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 6.0D, EntityAttributeModifier.Operation.ADD_VALUE));
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1, remap = false))
    public ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
        return builder;
    }
}
