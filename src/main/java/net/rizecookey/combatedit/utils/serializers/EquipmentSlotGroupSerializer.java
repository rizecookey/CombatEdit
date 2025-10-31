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
import net.minecraft.world.entity.EquipmentSlotGroup;

public class EquipmentSlotGroupSerializer implements JsonDeserializer<EquipmentSlotGroup>, JsonSerializer<EquipmentSlotGroup> {
    @Override
    public EquipmentSlotGroup deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Expected %s to be a string representing a modifier slot, but was not".formatted(json.getAsString()));
        }

        var result = Arrays.stream(EquipmentSlotGroup.values())
                .filter(slot -> slot.getSerializedName().matches(json.getAsString()))
                .findFirst();

        if (result.isEmpty()) {
            throw new JsonParseException("%s is not a valid modifier slot".formatted(json.getAsString()));
        }

        return result.orElseThrow();
    }

    @Override
    public JsonElement serialize(EquipmentSlotGroup src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getSerializedName());
    }
}
