package net.rizecookey.combatedit.client.configscreen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rizecookey.clothconfig2.extension.api.ExtendedConfigEntryBuilder;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import net.rizecookey.combatedit.configuration.Configuration;
import net.rizecookey.combatedit.configuration.EntityAttributes;

import java.util.List;
import java.util.Optional;

public class ConfigurationScreenBuilder {
    private static final ExtendedConfigEntryBuilder ENTRY_BUILDER = ExtendedConfigEntryBuilder.create();

    public static Screen buildScreen(Configuration config, Screen parentScreen) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Text.translatable("title.combatedit.config"))
                .setSavingRunnable(() -> { /* TODO */ });

        createEntityCategory(config.getEntityAttributes(), builder);

        return builder.build();
    }

    private static void createEntityCategory(List<EntityAttributes> entityAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.entity"));
        category.addEntry(ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.entity.entity_attributes"), entityAttributes, (value, list) -> {
            if (value == null) {
                value = new EntityAttributes(new Identifier("minecraft:creeper"), List.of(), false);
            }
            return createEntry(value);
        }).setSaveConsumer(value -> {
            entityAttributes.clear();
            entityAttributes.addAll(value);
        }).build());
    }

    private static ObjectListEntry<EntityAttributes> createEntry(EntityAttributes attributes) {
        var copy = new EntityAttributes(attributes.getEntityId(), List.copyOf(attributes.getBaseValues()), attributes.isOverrideDefault());

        var entityTypeEntry = ENTRY_BUILDER.startTextField(Text.translatable("option.combatedit.entity.entity_attributes.entity"), attributes.getEntityId().toString())
                .setSaveConsumer(string -> attributes.setEntityId(new Identifier(string)))
                .build();
        var attributeValueMap = ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry"), List.copyOf(attributes.getBaseValues()),
                        (value, list) -> {
                            if (value == null) {
                                value = new EntityAttributes.AttributeBaseValue(new Identifier("minecraft:generic.attack_damage"), 1);
                            }
                            return createEntry(value);
                        })
                .setSaveConsumer(values -> {
                    attributes.getBaseValues().clear();
                    attributes.getBaseValues().addAll(values);
                })
                .build();
        var overrideDefaultToggle = ENTRY_BUILDER.startBooleanToggle(Text.translatable("option.combatedit.entity.entity_attributes.override_defaults"), attributes.isOverrideDefault())
                .setSaveConsumer(attributes::setOverrideDefault)
                .build();

        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.entity.entity_attributes.entry"),
                List.of(
                        entityTypeEntry,
                        attributeValueMap,
                        overrideDefaultToggle
                ),
                ObjectAdapter.create(
                        () -> {
                            copy.setEntityId(new Identifier(entityTypeEntry.getValue()));
                            copy.getBaseValues().clear();
                            copy.getBaseValues().addAll(attributeValueMap.getValue());
                            copy.setOverrideDefault(overrideDefaultToggle.getValue());

                            return copy;
                        },
                        () -> Optional.of(new EntityAttributes(new Identifier("minecraft:creeper"), List.of(), false))
                )).build();
    }

    private static ObjectListEntry<EntityAttributes.AttributeBaseValue> createEntry(EntityAttributes.AttributeBaseValue baseValue) {
        var attributeEntry = ENTRY_BUILDER.startStrField(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.attribute"), baseValue.attribute().toString())
                .build();
        var baseValueEntry = ENTRY_BUILDER.startDoubleField(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.base_value"), baseValue.baseValue())
                .build();
        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"),
                List.of(
                        attributeEntry,
                        baseValueEntry
                ),
                ObjectAdapter.create(
                        () -> new EntityAttributes.AttributeBaseValue(new Identifier(attributeEntry.getValue()), baseValueEntry.getValue()),
                        () -> Optional.of(new EntityAttributes.AttributeBaseValue(new Identifier("minecraft:generic.attack_speed"), 1.0))
                ))
                .build();
    }
}
