package net.rizecookey.combatedit.utils;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class TextUtils {
    private TextUtils() {
    }

    public static Text fallBackToServerTranslation(Text text) {
        Deque<Text> parts = new ArrayDeque<>(List.of(text));
        while (!parts.isEmpty()) {
            Text part = parts.removeFirst();
            setFallbacks(part);
            parts.addAll(additionalParts(part));
        }

        return text;
    }

    private static List<Text> additionalParts(Text text) {
        List<Text> additionalParts = new ArrayList<>(text.getSiblings());
        var hoverEvent = text.getStyle().getHoverEvent();
        if (hoverEvent instanceof HoverEvent.ShowText(Text value)) {
            additionalParts.add(value);
        }

        if (text.getContent().getCodec().equals(TranslatableTextContent.CODEC)) {
            Arrays.stream(((TranslatableTextContent) text.getContent()).getArgs())
                    .filter(arg -> arg instanceof Text)
                    .forEach(arg -> additionalParts.add((Text) arg));
        }

        return additionalParts;
    }

    private static void setFallbacks(Text text) {
        if (!text.getContent().getCodec().equals(TranslatableTextContent.CODEC)) {
            return;
        }

        TranslatableTextContent textContent = (TranslatableTextContent) text.getContent();
        Language language = Language.getInstance();
        String fallback = language.get(textContent.getKey());
        textContent.combatEdit$setFallback(fallback);
    }
}
