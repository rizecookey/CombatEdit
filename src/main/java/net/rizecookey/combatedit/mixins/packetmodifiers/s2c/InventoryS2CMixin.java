package net.rizecookey.combatedit.mixins.packetmodifiers.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.rizecookey.combatedit.utils.AttributeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InventoryS2CPacket.class)
public abstract class InventoryS2CMixin {
    @Shadow
    private List<ItemStack> contents;

    @Inject(method = "<init>(ILnet/minecraft/util/collection/DefaultedList;)V", at = @At("TAIL"))
    public void modifyItemStacks(int guiId, DefaultedList<ItemStack> slotStackList, CallbackInfo ci) {
        List<ItemStack> modifiedSlotStackList = DefaultedList.ofSize(this.contents.size(), ItemStack.EMPTY);
        for (ItemStack itemStack : this.contents) {
            modifiedSlotStackList.set(this.contents.indexOf(itemStack), AttributeHelper.changeDisplayModifiers(itemStack));
        }
        this.contents = modifiedSlotStackList;
    }
}
