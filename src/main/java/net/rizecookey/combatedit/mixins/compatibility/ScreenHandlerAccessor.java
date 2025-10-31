package net.rizecookey.combatedit.mixins.compatibility;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface ScreenHandlerAccessor {
    @Accessor
    NonNullList<RemoteSlot> getRemoteSlots();

    @Accessor
    RemoteSlot getRemoteCarried();

    @Accessor
    ContainerSynchronizer getSynchronizer();
}
