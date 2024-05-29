package net.rizecookey.combatedit.configuration;

import java.util.List;

public class Configuration {
    private List<ItemAttributes> itemAttributes;
    private List<EntityAttributes> entityAttributes;

    public Configuration(List<ItemAttributes> itemAttributes, List<EntityAttributes> entityAttributes) {
        this.itemAttributes = itemAttributes;
        this.entityAttributes = entityAttributes;
    }

    protected Configuration() {}

    public List<ItemAttributes> getItemAttributes() {
        return itemAttributes;
    }

    public List<EntityAttributes> getEntityAttributes() {
        return entityAttributes;
    }
}
