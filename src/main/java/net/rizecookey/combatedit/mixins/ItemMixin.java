package net.rizecookey.combatedit.mixins;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.rizecookey.combatedit.item.ExtendedItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class ItemMixin {
    @Mixin(Item.class)
    public static class BaseItemMixin implements ExtendedItem {
        private final Item instance = (Item) (Object) this;
        private Item.Settings settings = new Item.Settings();

        @Inject(method = "<init>", at = @At("TAIL"))
        public void initSettings(Item.Settings settings, CallbackInfo ci) {
            this.settings = settings;
        }

        public Item.Settings getSettings() {
            return settings;
        }

        public Item item() {
            return instance;
        }

    }
    @Mixin(SwordItem.class)
    public static class SwordItemMixin extends ToolItem {

        public SwordItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public ImmutableMultimap.Builder cancelAttackSpeedModifier(ImmutableMultimap.Builder builder, Object key, Object value) {
            return builder;
        }
    }
    @Mixin(MiningToolItem.class)
    public static class MiningToolItemMixin extends ToolItem {

        public MiningToolItemMixin(ToolMaterial material, Settings settings) {
            super(material, settings);
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public ImmutableMultimap.Builder cancelAttackSpeedModifier(ImmutableMultimap.Builder builder, Object key, Object value) {
            return builder;
        }
    }
    @Mixin(TridentItem.class)
    public static class TridentItemMixin extends Item {

        @Shadow @Final private Multimap<EntityAttribute, EntityAttributeModifier> field_23746;

        public TridentItemMixin(Settings settings) {
            super(settings);
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 0))
        public ImmutableMultimap.Builder changeAttackDamage(ImmutableMultimap.Builder builder, Object key, Object value) {
            return builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 6.0D, EntityAttributeModifier.Operation.ADDITION));
        }

        @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1))
        public ImmutableMultimap.Builder cancelAttackSpeedModifier(ImmutableMultimap.Builder builder, Object key, Object value) {
            return builder;
        }
    }
}
