package net.rizecookey.combatedit.mixins.extension;

import net.minecraft.text.TranslatableTextContent;
import net.rizecookey.combatedit.extension.TranslatableTextContentExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TranslatableTextContent.class)
public abstract class TranslatableTextContentMixin implements TranslatableTextContentExtension {
    @Shadow @Final @Mutable private @Nullable String fallback;

    @Override
    public void combatEdit$setFallback(String fallback) {
        this.fallback = fallback;
    }
}
