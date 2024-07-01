package net.rizecookey.combatedit.client.configscreen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.rizecookey.clothconfig2.extension.api.ExtendedConfigEntryBuilder;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import net.rizecookey.clothconfig2.extension.impl.builders.ExtendedDropdownMenus;
import net.rizecookey.combatedit.configuration.Configuration;
import net.rizecookey.combatedit.configuration.EntityAttributes;
import net.rizecookey.combatedit.configuration.ItemAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigurationScreenBuilder {
    private static final ExtendedConfigEntryBuilder ENTRY_BUILDER = ExtendedConfigEntryBuilder.create();

    public static Screen buildScreen(Configuration config, Path savePath, Screen parentScreen) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Text.translatable("title.combatedit.config"))
                .setSavingRunnable(() -> {
                    try {
                        config.save(savePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e); // TODO Maybe show notification?
                    }
                });

        createEntityCategory(config.getEntityAttributes(), builder);
        createItemCategory(config.getItemAttributes(), builder);

        return builder.build();
    }

    private static void addLocalAndIngameWarnings(ConfigCategory category) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) {
            return;
        }

        String warningId = client.getNetworkHandler().getConnection().isLocal() ? "option.combatedit.warn.ingame" : "option.combatedit.warn.local_only";
        category.addEntry(ENTRY_BUILDER.startTextDescription(Text
                .translatable(warningId)
                .styled(style -> style.withColor(Formatting.RED))).build());
    }

    private static void createEntityCategory(List<EntityAttributes> entityAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.entity"));
        addLocalAndIngameWarnings(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.entity.entity_attributes"), entityAttributes, (value, list) -> createEntry(value != null ? value : EntityAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    entityAttributes.clear();
                    entityAttributes.addAll(value);
        }).setExpanded(true).build());
    }

    private static void createItemCategory(List<ItemAttributes> itemAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.item"));
        addLocalAndIngameWarnings(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.item.item_attributes"), itemAttributes, (value, list) -> createEntry(value != null ? value : ItemAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    itemAttributes.clear();
                    itemAttributes.addAll(value);
                })
                .setExpanded(true)
                .build());
    }

    private static ObjectListEntry<EntityAttributes> createEntry(EntityAttributes attributes, int entryIndex) {
        var copy = new EntityAttributes(attributes.getEntityId(), List.copyOf(attributes.getBaseValues()), attributes.isOverrideDefault());

        var entityTypeEntry = ENTRY_BUILDER.startDropdownMenu(Text.translatable("option.combatedit.entity.entity_attributes.entity"),
                        ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(Registries.ENTITY_TYPE, Registries.ENTITY_TYPE.get(attributes.getEntityId())),
                        ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(Registries.ENTITY_TYPE))
                .setSelections(Registries.ENTITY_TYPE.getIds())
                .build();
        var attributeValueMap = ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry"), List.copyOf(attributes.getBaseValues()),
                        (value, list) -> createEntry(value != null ? value : EntityAttributes.AttributeBaseValue.getDefault()))
                .setSaveConsumer(values -> {
                    attributes.getBaseValues().clear();
                    attributes.getBaseValues().addAll(values);
                })
                .setExpanded(true)
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
                            copy.setEntityId(entityTypeEntry.getValue());
                            copy.getBaseValues().clear();
                            copy.getBaseValues().addAll(attributeValueMap.getValue());
                            copy.setOverrideDefault(overrideDefaultToggle.getValue());

                            return copy;
                        },
                        () -> Optional.of(EntityAttributes.getDefault())
                )).setExpanded(entryIndex < 5).build();
    }

    private static ObjectListEntry<EntityAttributes.AttributeBaseValue> createEntry(EntityAttributes.AttributeBaseValue baseValue) {
        var attributeEntry = ENTRY_BUILDER.startDropdownMenu(
                Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.attribute"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(Registries.ATTRIBUTE, Registries.ATTRIBUTE.get(baseValue.attribute())),
                ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(Registries.ATTRIBUTE)
        ).setSelections(Registries.ATTRIBUTE.getIds()).build();
        var baseValueEntry = ENTRY_BUILDER.startDoubleField(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.base_value"), baseValue.baseValue())
                .build();
        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"),
                List.of(
                        attributeEntry,
                        baseValueEntry
                ),
                ObjectAdapter.create(
                        () -> new EntityAttributes.AttributeBaseValue(attributeEntry.getValue(), baseValueEntry.getValue()),
                        () -> Optional.of(EntityAttributes.AttributeBaseValue.getDefault())
                ))
                .setExpanded(true)
                .build();
    }

    private static ObjectListEntry<ItemAttributes> createEntry(ItemAttributes attributes, int entryIndex) {
        var copy = new ItemAttributes(attributes.getItemId(), List.copyOf(attributes.getModifiers()), attributes.isOverrideDefault());

        var itemEntry = ENTRY_BUILDER.startDropdownMenu(Text.translatable("option.combatedit.item.item_attributes.item"),
                        DropdownMenuBuilder.TopCellElementBuilder.ofItemIdentifier(Registries.ITEM.get(attributes.getItemId())),
                        ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(Registries.ITEM))
                .setSelections(Registries.ITEM.getIds())
                .build();
        var modifiersEntry = ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.item.item_attributes.modifier_entry"),
                        List.copyOf(attributes.getModifiers()),
                        (value, list) -> createEntry(value != null ? value : ItemAttributes.ModifierEntry.getDefault()))
                .setSaveConsumer(value -> {
                    attributes.getModifiers().clear();
                    attributes.getModifiers().addAll(value);
                })
                .setExpanded(true)
                .build();
        var overrideDefaultToggle = ENTRY_BUILDER.startBooleanToggle(Text.translatable("option.combatedit.item.item_attributes.override_defaults"), attributes.isOverrideDefault())
                .setSaveConsumer(attributes::setOverrideDefault)
                .build();

        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.item.item_attributes.entry"),
                List.of(
                        itemEntry,
                        modifiersEntry,
                        overrideDefaultToggle
                ),
                ObjectAdapter.create(
                        () -> {
                            copy.setItemId(itemEntry.getValue());
                            copy.getModifiers().clear();
                            copy.getModifiers().addAll(modifiersEntry.getValue());
                            copy.setOverrideDefault(overrideDefaultToggle.getValue());

                            return copy;
                        },
                        () -> Optional.of(ItemAttributes.getDefault())
                ))
                .setExpanded(entryIndex < 5)
                .build();
    }

    private static ObjectListEntry<ItemAttributes.ModifierEntry> createEntry(ItemAttributes.ModifierEntry modifierEntry) {
        var attributeEntry = ENTRY_BUILDER.startDropdownMenu(
                Text.translatable("option.combatedit.item.item_attributes.modifier_entry.attribute"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(Registries.ATTRIBUTE, Registries.ATTRIBUTE.get(modifierEntry.attribute())),
                ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(Registries.ATTRIBUTE)
        ).setSelections(Registries.ATTRIBUTE.getIds()).build();
        var uuidEntry = ENTRY_BUILDER.startStrField(Text.translatable("option.combatedit.item.item_attributes.modifier_entry.uuid"), modifierEntry.uuid() != null ? modifierEntry.uuid().toString() : "")
                .setErrorSupplier(value -> {
                    if (value.isEmpty()) {
                        return Optional.empty();
                    }
                    try {
                        UUID.fromString(value);
                    } catch (IllegalArgumentException e) {
                        return Optional.of(Text.translatable("error.combatedit.invalid_uuid"));
                    }
                    return Optional.empty();
                })
                .setTooltip(Text.translatable("option.combatedit.item.item_attributes.modifier_entry.uuid.tooltip"))
                .build();
        var nameEntry = ENTRY_BUILDER.startStrField(Text.translatable("option.combatedit.item.item_attributes.modifier_entry.name"), modifierEntry.name() != null ? modifierEntry.name() : "")
                .build();
        var valueEntry = ENTRY_BUILDER.startDoubleField(Text.translatable("option.combatedit.item.item_attributes.modifier_entry.value"), modifierEntry.value())
                .build();
        var operationEntry = ENTRY_BUILDER.startDropdownMenu(
                Text.translatable("option.combatedit.item.item_attributes.modifier_entry.operation"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofStringIdentifiable(EntityAttributeModifier.Operation.class, modifierEntry.operation()),
                ExtendedDropdownMenus.CellCreatorBuilder.ofStringIdentifiableAutoWidth(EntityAttributeModifier.Operation.class)
        ).setSelections(List.of(EntityAttributeModifier.Operation.values())).build();
        var slotEntry = ENTRY_BUILDER.startDropdownMenu(
                Text.translatable("option.combatedit.item.item_attributes.modifier_entry.slot"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofStringIdentifiable(AttributeModifierSlot.class, modifierEntry.slot()),
                ExtendedDropdownMenus.CellCreatorBuilder.ofStringIdentifiableAutoWidth(AttributeModifierSlot.class)
        ).setSelections(List.of(AttributeModifierSlot.values())).build();

        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.item.item_attributes.modifier_entry"),
                List.of(
                        attributeEntry,
                        uuidEntry,
                        nameEntry,
                        valueEntry,
                        operationEntry,
                        slotEntry
                ),
                ObjectAdapter.create(
                        () -> new ItemAttributes.ModifierEntry(attributeEntry.getValue(),
                                !uuidEntry.getValue().isEmpty() ? UUID.fromString(uuidEntry.getValue()) : UUID.randomUUID(),
                                nameEntry.getValue(),
                                valueEntry.getValue(),
                                operationEntry.getValue(),
                                slotEntry.getValue()),
                        () -> Optional.of(ItemAttributes.ModifierEntry.getDefault())))
                .setExpanded(true)
                .build();
    }
}
