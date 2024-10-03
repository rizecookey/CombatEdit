package net.rizecookey.combatedit.utils;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;
import net.rizecookey.combatedit.extension.TranslatableTextContentExtension;

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
        if (hoverEvent != null && hoverEvent.getAction().equals(HoverEvent.Action.SHOW_TEXT)) {
            additionalParts.add(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT));
        }

        if (text.getContent().getType().equals(TranslatableTextContent.TYPE)) {
            Arrays.stream(((TranslatableTextContent) text.getContent()).getArgs())
                    .filter(arg -> arg instanceof Text)
                    .forEach(arg -> additionalParts.add((Text) arg));
        }

        return additionalParts;
    }

    private static void setFallbacks(Text text) {
        if (!text.getContent().getType().equals(TranslatableTextContent.TYPE)) {
            return;
        }

        TranslatableTextContent textContent = (TranslatableTextContent) text.getContent();
        Language language = Language.getInstance();
        String fallback = language.get(textContent.getKey());
        ((TranslatableTextContentExtension) textContent).combatEdit$setFallback(fallback);
    }
}
