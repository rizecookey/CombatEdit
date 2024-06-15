package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.lang.reflect.Type;
import java.util.Arrays;

public class EntityAttributeModifier$OperationSerializer implements JsonSerializer<EntityAttributeModifier.Operation>, JsonDeserializer<EntityAttributeModifier.Operation> {
    @Override
    public EntityAttributeModifier.Operation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected a primitive to represent the operation id");
        }

        return Arrays.stream(EntityAttributeModifier.Operation.values())
                .filter(operation -> operation.asString().equals(json.getAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Operation type %s does not exist".formatted(json.getAsString())));
    }

    @Override
    public JsonElement serialize(EntityAttributeModifier.Operation src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.asString());
    }
}
