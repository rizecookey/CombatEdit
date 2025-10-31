package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class ComponentSerializer implements JsonDeserializer<Component>, JsonSerializer<Component> {
    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        } catch (IllegalStateException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }
}
