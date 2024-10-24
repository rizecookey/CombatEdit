package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchable;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(InventoryS2CPacket.class)
public abstract class InventoryS2CPacketMixin implements AttributePatchable {
    @Shadow @Final @Mutable private List<ItemStack> contents;

    @Shadow @Final @Mutable private ItemStack cursorStack;

    @Override
    public void combatEdit$patchAttributes() {
        ItemStackAttributeHelper helper = ConfigurationManager.getInstance().getAttributeHelper();
        List<ItemStack> modifiedSlotStackList = DefaultedList.ofSize(this.contents.size(), ItemStack.EMPTY);
        for (ItemStack itemStack : this.contents) {
            modifiedSlotStackList.set(this.contents.indexOf(itemStack), helper.getDisplayModified(itemStack));
        }
        this.contents = modifiedSlotStackList;
        this.cursorStack = helper.getDisplayModified(this.cursorStack);
    }
}
