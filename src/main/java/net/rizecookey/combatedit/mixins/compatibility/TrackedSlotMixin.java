package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.screen.sync.TrackedSlot;
import net.rizecookey.combatedit.extension.TrackedSlotExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TrackedSlot.class)
public interface TrackedSlotMixin extends TrackedSlotExtension {
}
