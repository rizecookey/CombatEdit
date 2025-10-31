package net.rizecookey.combatedit.client.configscreen;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum TriStateOption {
    USE_DEFAULT(null, Component.translatable("option.combatedit.tristate.use_default")),
    TRUE(true, Component.translatable("option.combatedit.tristate.true").withStyle(style -> style.withColor(ChatFormatting.GREEN))),
    FALSE(false, Component.translatable("option.combatedit.tristate.false").withStyle(style -> style.withColor(ChatFormatting.RED))),;

    private final Boolean booleanValue;
    private final Component text;

    TriStateOption(Boolean booleanValue, Component text) {
        this.booleanValue = booleanValue;
        this.text = text;
    }

    public Boolean asBoolean() {
        return booleanValue;
    }

    public Component getText() {
        return text;
    }

    public static TriStateOption fromBoolean(Boolean booleanValue) {
        return Arrays.stream(values())
                .filter(triState -> Objects.equals(triState.asBoolean(), booleanValue))
                .findFirst()
                .orElseThrow();
    }
}
