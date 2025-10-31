package net.rizecookey.combatedit.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ComponentUtils {
    private ComponentUtils() {
    }

    public static Component fallBackToServerTranslation(Component text) {
        Deque<Component> parts = new ArrayDeque<>(List.of(text));
        while (!parts.isEmpty()) {
            Component part = parts.removeFirst();
            setFallbacks(part);
            parts.addAll(additionalParts(part));
        }

        return text;
    }

    private static List<Component> additionalParts(Component text) {
        List<Component> additionalParts = new ArrayList<>(text.getSiblings());
        var hoverEvent = text.getStyle().getHoverEvent();
        if (hoverEvent instanceof HoverEvent.ShowText(Component value)) {
            additionalParts.add(value);
        }

        if (text.getContents().codec().equals(TranslatableContents.MAP_CODEC)) {
            Arrays.stream(((TranslatableContents) text.getContents()).getArgs())
                    .filter(arg -> arg instanceof Component)
                    .forEach(arg -> additionalParts.add((Component) arg));
        }

        return additionalParts;
    }

    private static void setFallbacks(Component text) {
        if (!text.getContents().codec().equals(TranslatableContents.MAP_CODEC)) {
            return;
        }

        TranslatableContents textContent = (TranslatableContents) text.getContents();
        Language language = Language.getInstance();
        String fallback = language.getOrDefault(textContent.getKey());
        textContent.combatEdit$setFallback(fallback);
    }
}
