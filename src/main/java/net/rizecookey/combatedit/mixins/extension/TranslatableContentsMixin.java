package net.rizecookey.combatedit.mixins.extension;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.rizecookey.combatedit.extension.TranslatableContentsExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin implements TranslatableContentsExtension {
    @Shadow @Final @Mutable private @Nullable String fallback;

    @Override
    public void combatEdit$setFallback(String fallback) {
        this.fallback = fallback;
    }
}
