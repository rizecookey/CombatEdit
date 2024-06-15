package net.rizecookey.combatedit.configuration;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private List<ItemAttributes> itemAttributes;
    private List<EntityAttributes> entityAttributes;

    public Configuration(List<ItemAttributes> itemAttributes, List<EntityAttributes> entityAttributes) {
        this.itemAttributes = new ArrayList<>(itemAttributes);
        this.entityAttributes = new ArrayList<>(entityAttributes);
    }

    protected Configuration() {}

    public List<ItemAttributes> getItemAttributes() {
        return itemAttributes;
    }

    public List<EntityAttributes> getEntityAttributes() {
        return entityAttributes;
    }

    public void validate() throws InvalidConfigurationException {
        for (var itemAttributes : itemAttributes) {
            itemAttributes.validate();
        }

        for (var entityAttributes : entityAttributes) {
            entityAttributes.validate();
        }
    }

    public static Configuration createDefault() {
        return new Configuration(List.of(), List.of());
    }
}
