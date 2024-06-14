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

    public static Configuration createDefault() {
        return new Configuration(List.of(), List.of());
    }
}
