package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.lang.reflect.Type;

public class TextSerializer implements JsonDeserializer<Text>, JsonSerializer<Text> {
    @Override
    public Text deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return TextCodecs.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        } catch (IllegalStateException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Text src, Type typeOfSrc, JsonSerializationContext context) {
        return TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }
}
