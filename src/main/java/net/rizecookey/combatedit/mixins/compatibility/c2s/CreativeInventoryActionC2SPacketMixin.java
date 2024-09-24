package net.rizecookey.combatedit.mixins.compatibility.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.extension.AttributePatchReversible;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeInventoryActionC2SPacket.class)
public abstract class CreativeInventoryActionC2SPacketMixin implements AttributePatchReversible {
    @Shadow @Final @Mutable private ItemStack stack;

    @Override
    public void combatEdit$reverseAttributePatches() {
        this.stack = CombatEdit.getInstance().getAttributeHelper().reverseDisplayModifiers(this.stack);
    }
}
