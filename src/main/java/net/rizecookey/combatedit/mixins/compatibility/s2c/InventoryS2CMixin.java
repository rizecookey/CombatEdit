package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.rizecookey.combatedit.CombatEdit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InventoryS2CPacket.class)
public abstract class InventoryS2CMixin {
    @Shadow @Final @Mutable private List<ItemStack> contents;

    @Unique
    private static CombatEdit COMBAT_EDIT;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void modifyItemStacks(CallbackInfo ci) {
        if (COMBAT_EDIT == null) {
            COMBAT_EDIT = CombatEdit.getInstance();
        }

        List<ItemStack> modifiedSlotStackList = DefaultedList.ofSize(this.contents.size(), ItemStack.EMPTY);
        for (ItemStack itemStack : this.contents) {
            modifiedSlotStackList.set(this.contents.indexOf(itemStack), COMBAT_EDIT.getAttributeHelper().getDisplayModified(itemStack));
        }
        this.contents = modifiedSlotStackList;
    }
}
