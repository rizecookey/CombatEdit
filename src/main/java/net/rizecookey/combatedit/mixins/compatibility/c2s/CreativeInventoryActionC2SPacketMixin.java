package net.rizecookey.combatedit.mixins.compatibility.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.AttributePatchReversible;
import net.rizecookey.combatedit.extension.CreativeInventoryActionC2SPacketExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeInventoryActionC2SPacket.class)
public abstract class CreativeInventoryActionC2SPacketMixin implements AttributePatchReversible, CreativeInventoryActionC2SPacketExtension {
    @Shadow @Final @Mutable private ItemStack stack;

    @Unique
    private boolean hadPacketModification;

    @Override
    public void combatEdit$reverseAttributePatches() {
        var unmodifiedStack = ConfigurationManager.getInstance().getAttributeHelper().reverseDisplayModifiers(this.stack);
        this.hadPacketModification = unmodifiedStack != this.stack;
        this.stack = unmodifiedStack;
    }

    @Override
    public boolean combatEdit$hadPacketModification() {
        return hadPacketModification;
    }
}
