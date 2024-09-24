package net.rizecookey.combatedit.mixins.compatibility.c2s;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.extension.AttributePatchReversible;
import net.rizecookey.combatedit.utils.ItemStackAttributeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickWindowC2SPacketMixin implements AttributePatchReversible {
    @Final @Shadow @Mutable private ItemStack stack;

    @Shadow @Final @Mutable private Int2ObjectMap<ItemStack> modifiedStacks;

    @Unique
    @Override
    public void combatEdit$reverseAttributePatches() {
        ItemStackAttributeHelper helper = CombatEdit.getInstance().getAttributeHelper();

        this.stack = helper.reverseDisplayModifiers(this.stack);
        Int2ObjectMap<ItemStack> newMap = new Int2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : this.modifiedStacks.int2ObjectEntrySet()) {
            newMap.put(entry.getIntKey(), helper.reverseDisplayModifiers(entry.getValue()));
        }
        this.modifiedStacks = Int2ObjectMaps.unmodifiable(newMap);
    }
}
