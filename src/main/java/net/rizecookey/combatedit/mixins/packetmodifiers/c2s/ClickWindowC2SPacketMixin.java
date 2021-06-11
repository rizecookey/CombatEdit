package net.rizecookey.combatedit.mixins.packetmodifiers.c2s;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.rizecookey.combatedit.utils.AttributeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickWindowC2SPacketMixin {
    @Final @Shadow @Mutable private ItemStack stack;

    @Shadow @Final @Mutable private Int2ObjectMap<ItemStack> modifiedStacks;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStack(CallbackInfo ci) {
        this.stack = AttributeHelper.reverseDisplayModifiers(this.stack);
        Int2ObjectMap<ItemStack> newMap = new Int2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : this.modifiedStacks.int2ObjectEntrySet()) {
            newMap.put(entry.getIntKey(), AttributeHelper.reverseDisplayModifiers(entry.getValue()));
        }
        this.modifiedStacks = Int2ObjectMaps.unmodifiable(newMap);
    }
}
