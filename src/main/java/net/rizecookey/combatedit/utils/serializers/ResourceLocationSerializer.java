package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationSerializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
    @Override
    public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected %s to be a string representing an identifier, but was not".formatted(json.getAsString()));
        }

        return ResourceLocation.parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
