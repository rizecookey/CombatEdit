package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class EntityAttributeModifier$OperationSerializer implements JsonSerializer<AttributeModifier.Operation>, JsonDeserializer<AttributeModifier.Operation> {
    @Override
    public AttributeModifier.Operation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected a primitive to represent the operation id");
        }

        return Arrays.stream(AttributeModifier.Operation.values())
                .filter(operation -> operation.getSerializedName().equals(json.getAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Operation type %s does not exist".formatted(json.getAsString())));
    }

    @Override
    public JsonElement serialize(AttributeModifier.Operation src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getSerializedName());
    }
}
