package net.rizecookey.combatedit.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.stream.Collector;

public final class ConfigMigration {
    private ConfigMigration() {}

    public static JsonObject migrateToNewerVersion(JsonObject old, int version) {
        JsonObject copy = old.deepCopy();
        if (version < 1) {
            copy.addProperty("configuration_version", 1);
        }
        if (version < 2 && copy.has("item_attributes") && copy.get("item_attributes").isJsonArray()) {
            var oldArray = copy.get("item_attributes").getAsJsonArray();
            JsonArray newAttributes = migrateModifierUUIDsToIdentifiers(oldArray);
            copy.remove("item_attributes");
            copy.add("item_attributes", newAttributes);
        }
        if (version < 3) {
            if (copy.has("item_attributes") && copy.get("item_attributes").isJsonArray()) {
                var oldArray = copy.get("item_attributes").getAsJsonArray();
                JsonArray newAttributes = migrateItemAttributesAttributeIds(oldArray);
                copy.add("item_attributes", newAttributes);
            }

            if (copy.has("entity_attributes") && copy.get("entity_attributes").isJsonArray()) {
                var oldArray = copy.get("entity_attributes").getAsJsonArray();
                JsonArray newDefaults = migrateEntityAttributesAttributeIds(oldArray);
                copy.add("entity_attributes", newDefaults);
            }
        }
        return copy;
    }

    private static JsonArray migrateModifierUUIDsToIdentifiers(JsonArray itemAttributes) {
        JsonArray result = new JsonArray();
        for (var entry : itemAttributes) {
            if (!entry.isJsonObject()) {
                continue;
            }
            var oldEntry = entry.getAsJsonObject();
            var newEntry = oldEntry.deepCopy();

            if (!oldEntry.has("modifiers") || !oldEntry.get("modifiers").isJsonArray()) {
                continue;
            }

            var oldModifiers = oldEntry.get("modifiers").getAsJsonArray();
            var newModifiers = new JsonArray();

            for (var modifier : oldModifiers) {
                if (!modifier.isJsonObject()) {
                    continue;
                }

                var newModifier = migrateUUIDToIdentifier(modifier.getAsJsonObject());
                newModifiers.add(newModifier);
            }
            newEntry.add("modifiers", newModifiers);
            result.add(newEntry);
        }

        return result;
    }

    private static JsonObject migrateUUIDToIdentifier(JsonObject modifier) {
        if (!modifier.has("uuid") || !modifier.get("uuid").isJsonPrimitive()) {
            return modifier;
        }

        String uuidString = modifier.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return modifier;
        }
        JsonObject result = modifier.deepCopy();
        result.remove("uuid");
        result.addProperty("modifier_id", transformUUID(uuid).toString());

        return result;
    }

    private static Identifier transformUUID(UUID uuid) {
        if (UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf").equals(uuid)) {
            return Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
        }

        if (UUID.fromString("fa233e1c-4180-4865-b01b-bcce9785aca3").equals(uuid)) {
            return Item.BASE_ATTACK_SPEED_MODIFIER_ID;
        }

        return Identifier.of("combatedit", "generated/" + uuid);
    }

    private static JsonArray migrateItemAttributesAttributeIds(JsonArray itemAttributes) {
        return itemAttributes.asList().stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(entry -> {
                    if (!entry.getAsJsonObject().has("modifiers") || !entry.getAsJsonObject().get("modifiers").isJsonArray()) {
                        return entry;
                    }

                    return migrateItemAttributeEntryAttributeIds(entry);
                })
                .collect(jsonArrayCollector());
    }

    private static JsonObject migrateItemAttributeEntryAttributeIds(JsonObject itemAttribute) {
        var result = itemAttribute.deepCopy();
        result.add("modifiers", itemAttribute.get("modifiers").getAsJsonArray().asList().stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(modifier -> modifier.has("attribute")
                        && modifier.get("attribute").isJsonPrimitive()
                        && Identifier.validate(modifier.get("attribute").getAsString()).isSuccess())
                .map(modifier -> {
                    JsonObject newModifier = modifier.getAsJsonObject().deepCopy();
                    newModifier.addProperty("attribute", migrateAttributeId(Identifier.of(modifier.get("attribute").getAsString())).toString());
                    return newModifier;
                })
                .collect(jsonArrayCollector()));
        return result;
    }

    private static JsonArray migrateEntityAttributesAttributeIds(JsonArray entityAttributes) {
        return entityAttributes.asList().stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(entry -> {
                    if (!entry.getAsJsonObject().has("base_values") || !entry.getAsJsonObject().get("base_values").isJsonArray()) {
                        return entry;
                    }

                    return migrateEntityAttributeEntryAttributeIds(entry);
                })
                .collect(jsonArrayCollector());
    }

    private static JsonObject migrateEntityAttributeEntryAttributeIds(JsonObject entityAttributeEntry) {
        var result = entityAttributeEntry.deepCopy();
        result.add("base_values", entityAttributeEntry.get("base_values").getAsJsonArray().asList().stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(baseValue -> baseValue.has("attribute")
                        && baseValue.get("attribute").isJsonPrimitive()
                        && Identifier.validate(baseValue.get("attribute").getAsString()).isSuccess())
                .map(baseValue -> {
                    JsonObject newBaseValue = baseValue.deepCopy();
                    newBaseValue.addProperty("attribute", migrateAttributeId(Identifier.of(baseValue.get("attribute").getAsString())).toString());
                    return newBaseValue;
                })
                .collect(jsonArrayCollector()));

        return result;
    }

    private static Identifier migrateAttributeId(Identifier previous) {
        return Identifier.of(previous.getNamespace(),
                previous.getPath()
                        .replaceFirst("^generic\\.", "")
                        .replaceFirst("^player\\.", ""));
    }

    private static Collector<JsonElement, JsonArray, JsonArray> jsonArrayCollector() {
        return Collector.of(JsonArray::new, JsonArray::add, (JsonArray array1, JsonArray array2) -> {
            var result = new JsonArray();
            result.addAll(array1);
            result.addAll(array2);
            return result;
        });
    }
}
