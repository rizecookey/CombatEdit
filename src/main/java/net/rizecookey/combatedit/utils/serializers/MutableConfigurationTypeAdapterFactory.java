package net.rizecookey.combatedit.utils.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.utils.ConfigMigration;

import java.io.IOException;

public class MutableConfigurationTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == MutableConfiguration.class) {
            return (TypeAdapter<T>) createForMutableConfig(gson, TypeToken.get(MutableConfiguration.class));
        }
        return gson.getDelegateAdapter(this, type);
    }

    public TypeAdapter<MutableConfiguration> createForMutableConfig(Gson gson, TypeToken<MutableConfiguration> type) {
        var delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<MutableConfiguration>() {
            @Override
            public void write(JsonWriter out, MutableConfiguration value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public MutableConfiguration read(JsonReader in) throws IOException {
                JsonObject object;
                try {
                    object = gson.fromJson(in, JsonObject.class);
                } catch (JsonIOException e) {
                    throw new IOException(e);
                }

                var configurationVersion = object.get("configuration_version");
                // if no version is set, assume newest
                if (configurationVersion != null && configurationVersion.isJsonPrimitive() && configurationVersion.getAsInt() < MutableConfiguration.CURRENT_VERSION) {
                    object = ConfigMigration.migrateToNewerVersion(object, configurationVersion.getAsInt());
                }

                return delegate.fromJsonTree(object);
            }
        }.nullSafe();
    }
}
