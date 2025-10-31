package net.rizecookey.combatedit.mixins.compatibility.s2c;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchable;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ClientboundContainerSetContentPacket.class)
public abstract class ClientboundContainerSetContentPacketMixin implements AttributePatchable {
    @Shadow @Final @Mutable private List<ItemStack> items;

    @Shadow @Final @Mutable private ItemStack carriedItem;

    @Override
    public void combatEdit$preSend(ServerGamePacketListenerImpl networkHandler) {
        ItemStackAttributeHelper helper = ConfigurationManager.getInstance().getAttributeHelper();
        List<ItemStack> modifiedSlotStackList = NonNullList.withSize(this.items.size(), ItemStack.EMPTY);
        for (ItemStack itemStack : this.items) {
            modifiedSlotStackList.set(this.items.indexOf(itemStack), helper.getDisplayModified(itemStack));
        }
        this.items = modifiedSlotStackList;
        this.carriedItem = helper.getDisplayModified(this.carriedItem);
    }
}
