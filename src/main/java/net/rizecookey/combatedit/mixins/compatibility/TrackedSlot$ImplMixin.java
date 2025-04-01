package net.rizecookey.combatedit.mixins.compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.sync.ComponentChangesHash;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.screen.sync.TrackedSlot;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.extension.TrackedSlotExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrackedSlot.Impl.class)
public abstract class TrackedSlot$ImplMixin implements TrackedSlotExtension {
    @Shadow private @Nullable ItemStackHash receivedHash;
    @Shadow @Final private ComponentChangesHash.ComponentHasher hasher;
    @Unique private boolean compareWithDisplayModified;

    @Override
    public void combatEdit$setCompareWithDisplayModified(boolean value) {
        compareWithDisplayModified = value;
    }

    @ModifyExpressionValue(method = "isInSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/sync/ItemStackHash;hashEquals(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/sync/ComponentChangesHash$ComponentHasher;)Z"))
    public boolean compareHashWithDisplayModified(boolean value, ItemStack actualStack) {
        if (!compareWithDisplayModified) {
            return value;
        }

        ItemStack displayModified = ConfigurationManager.getInstance().getAttributeHelper().getDisplayModified(actualStack);
        if (displayModified == actualStack) {
            return value;
        }

        assert this.receivedHash != null;
        return this.receivedHash.hashEquals(displayModified, this.hasher);
    }
}
