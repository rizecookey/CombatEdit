package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

import java.lang.reflect.Type;

public class NbtCompoundSerializer implements JsonSerializer<NbtCompound>, JsonDeserializer<NbtCompound> {
    @Override
    public NbtCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected %s to be a stringified NbtCompound, but was not".formatted(json.getAsString()));
        }
        try {
            return StringNbtReader.parse(json.getAsString());
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Error while parsing NBT: ", e);
        }
    }

    @Override
    public JsonElement serialize(NbtCompound src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.asString());
    }
}
