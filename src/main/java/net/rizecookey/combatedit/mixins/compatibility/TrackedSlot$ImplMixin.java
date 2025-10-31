package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.TrackedSlotExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RemoteSlot.Synchronized.class)
public abstract class TrackedSlot$ImplMixin implements TrackedSlotExtension {
    @Shadow private @Nullable HashedStack remoteHash;
    @Shadow @Final private HashedPatchMap.HashGenerator hasher;
    @Unique private boolean compareWithDisplayModified;

    @Override
    public void combatEdit$setCompareWithDisplayModified(boolean value) {
        compareWithDisplayModified = value;
    }

    @ModifyExpressionValue(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/HashedStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/network/HashedPatchMap$HashGenerator;)Z"))
    public boolean compareHashWithDisplayModified(boolean value, ItemStack actualStack) {
        if (!compareWithDisplayModified) {
            return value;
        }

        ItemStack displayModified = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(actualStack);
        if (displayModified == actualStack) {
            return value;
        }

        assert this.remoteHash != null;
        return this.remoteHash.matches(displayModified, this.hasher);
    }
}
