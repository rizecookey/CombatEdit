package net.rizecookey.combatedit.mixins.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.rizecookey.combatedit.extension.DamageToolItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends ToolItem implements DamageToolItem {
    @Shadow @Final @Mutable private float attackDamage;

    @Shadow @Final @Mutable private Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public SwordItemMixin(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1, remap = false))
    public ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
        return builder;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public void setAttackDamage(float damage) {
        this.attackDamage = damage;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : this.attributeModifiers.entries()) {
            if (!entry.getKey().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", this.attackDamage, EntityAttributeModifier.Operation.ADDITION));

        this.attributeModifiers = builder.build();
    }
}
