package net.rizecookey.combatedit.client.configscreen;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Objects;

public enum TriStateOption {
    USE_DEFAULT(null, Text.translatable("option.combatedit.tristate.use_default")),
    TRUE(true, Text.translatable("option.combatedit.tristate.true").styled(style -> style.withColor(Formatting.GREEN))),
    FALSE(false, Text.translatable("option.combatedit.tristate.false").styled(style -> style.withColor(Formatting.RED))),;

    private final Boolean booleanValue;
    private final Text text;

    TriStateOption(Boolean booleanValue, Text text) {
        this.booleanValue = booleanValue;
        this.text = text;
    }

    public Boolean asBoolean() {
        return booleanValue;
    }

    public Text getText() {
        return text;
    }

    public static TriStateOption fromBoolean(Boolean booleanValue) {
        return Arrays.stream(values())
                .filter(triState -> Objects.equals(triState.asBoolean(), booleanValue))
                .findFirst()
                .orElseThrow();
    }
}
