package net.rizecookey.combatedit.mixins.compatibility.c2s;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickWindowC2SPacketMixin {
    @Final @Shadow @Mutable private ItemStack stack;

    @Shadow @Final @Mutable private Int2ObjectMap<ItemStack> modifiedStacks;

    @Unique
    private static CombatEdit COMBAT_EDIT;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStack(CallbackInfo ci) {
        if (COMBAT_EDIT == null) {
            COMBAT_EDIT = CombatEdit.getInstance();
        }
        ItemStackAttributeHelper helper = COMBAT_EDIT.getAttributeHelper();

        this.stack = helper.reverseDisplayModifiers(this.stack);
        Int2ObjectMap<ItemStack> newMap = new Int2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : this.modifiedStacks.int2ObjectEntrySet()) {
            newMap.put(entry.getIntKey(), helper.reverseDisplayModifiers(entry.getValue()));
        }
        this.modifiedStacks = Int2ObjectMaps.unmodifiable(newMap);
    }
}
