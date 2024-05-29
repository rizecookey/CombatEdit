package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.component.type.AttributeModifierSlot;

import java.lang.reflect.Type;
import java.util.Arrays;

public class AttributeModifierSlotSerializer implements JsonDeserializer<AttributeModifierSlot>, JsonSerializer<AttributeModifierSlot> {
    @Override
    public AttributeModifierSlot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected %s to be a string representing a modifier slot, but was not".formatted(json.getAsString()));
        }

        var result = Arrays.stream(AttributeModifierSlot.values())
                .filter(slot -> slot.asString().matches(json.getAsString()))
                .findFirst();

        if (result.isEmpty()) {
            throw new JsonParseException("%s is not a valid modifier slot".formatted(json.getAsString()));
        }

        return result.orElseThrow();
    }

    @Override
    public JsonElement serialize(AttributeModifierSlot src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.asString());
    }
}
