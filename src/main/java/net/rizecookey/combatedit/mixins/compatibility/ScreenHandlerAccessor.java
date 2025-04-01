package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Accessor
    DefaultedList<TrackedSlot> getTrackedSlots();

    @Accessor
    TrackedSlot getTrackedCursorSlot();

    @Accessor
    ScreenHandlerSyncHandler getSyncHandler();
}
