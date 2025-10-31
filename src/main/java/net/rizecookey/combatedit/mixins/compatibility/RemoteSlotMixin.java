package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.world.inventory.RemoteSlot;
import net.rizecookey.combatedit.extension.RemoteSlotExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RemoteSlot.class)
public interface RemoteSlotMixin extends RemoteSlotExtension {
}
