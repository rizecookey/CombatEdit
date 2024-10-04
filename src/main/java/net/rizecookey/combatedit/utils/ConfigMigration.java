package net.rizecookey.combatedit.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.UUID;

public final class ConfigMigration {
    private ConfigMigration() {}

    public static JsonObject migrateToNewerVersion(JsonObject old, int version) {
        JsonObject copy = old.deepCopy();
        if (version < 1) {
            copy.addProperty("configuration_version", 1);
        }
        if (version < 2 && old.has("item_attributes") && old.get("item_attributes").isJsonArray()) {
            var oldArray = old.get("item_attributes").getAsJsonArray();
            JsonArray newAttributes = migrateModifierUUIDsToIdentifiers(oldArray);
            copy.remove("item_attributes");
            copy.add("item_attributes", newAttributes);
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

            newEntry.remove("modifiers");
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
}
