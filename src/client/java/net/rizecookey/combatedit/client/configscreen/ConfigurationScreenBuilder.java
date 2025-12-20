package net.rizecookey.combatedit.client.configscreen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.rizecookey.clothconfig2.extension.api.ExtendedConfigEntryBuilder;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import net.rizecookey.clothconfig2.extension.impl.builders.ExtendedDropdownMenus;
import net.rizecookey.combatedit.client.CombatEditClient;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.combatedit.utils.ReservedIdentifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.rizecookey.combatedit.client.CombatEditClient.LOGGER;

@SuppressWarnings("UnstableApiUsage")
public class ConfigurationScreenBuilder {
    private static final ExtendedConfigEntryBuilder ENTRY_BUILDER = ExtendedConfigEntryBuilder.create();

    public static Screen buildScreen(CombatEditClient combatEditClient, Screen parentScreen) {
        Minecraft client = Minecraft.getInstance();
        Settings settings = combatEditClient.getCurrentSettings().copy();

        var builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("title.combatedit.config"))
                .setSavingRunnable(() -> {
                    try {
                        combatEditClient.saveSettings(settings);
                    } catch (IOException e) {
                        LOGGER.error("Failed to save the settings file", e);
                        CombatEditClient.sendErrorNotification(client, "settings_save_error");
                        return;
                    }

                    if (client.getConnection() != null && client.getConnection().getConnection().isMemoryConnection()) {
                        var server = Minecraft.getInstance().getSingleplayerServer();
                        assert server != null;
                        server.reloadResources(server.getPackRepository().getSelectedIds());
                    }
                });

        var config = settings.getConfigurationOverrides();
        createProfileCategory(settings, builder);
        createClientCategory(settings.getClientOnly(), builder);
        createEntityCategory(config.getEntityAttributes(), builder);
        createItemCategory(config.getItemAttributes(), config.getItemComponents(), builder);
        createSoundCategory(config, builder);
        createMiscCategory(config.getMiscOptions(), builder);

        return builder.build();
    }

    private static void addLocalWarning(ConfigCategory category) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.getConnection().getConnection().isMemoryConnection()) {
            return;
        }

        category.addEntry(ENTRY_BUILDER.startTextDescription(Component
                .translatable("option.combatedit.warn.local_only")
                .withStyle(style -> style.withColor(ChatFormatting.RED))).build());
    }

    private static void addIngameWarning(ConfigCategory category) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || !client.getConnection().getConnection().isMemoryConnection()) {
            return;
        }

        category.addEntry(ENTRY_BUILDER.startTextDescription(Component
                .translatable("option.combatedit.warn.ingame")
                .withStyle(style -> style.withColor(ChatFormatting.RED))).build());
    }

    private static void createProfileCategory(Settings settings, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Component.translatable("category.combatedit.profile"));
        addLocalWarning(category);
        addIngameWarning(category);

        List<BaseProfile.Info> baseProfiles = new ArrayList<>(Arrays.stream(BaseProfile.IntegratedProfiles.values())
                .map(BaseProfile.IntegratedProfiles::getInfo)
                .toList());
        var customProfile = new BaseProfile.Info(null,
                Component.translatable("option.combatedit.profile.base_profile.custom.name"),
                Component.translatable("option.combatedit.profile.base_profile.custom.description"));
        baseProfiles.add(customProfile);

        BaseProfile.Info currentSelected = baseProfiles.stream()
                .filter(data -> settings.getSelectedBaseProfile().equals(data.id()))
                .findFirst()
                .orElse(customProfile);

        var profileSelector = ENTRY_BUILDER.startSelector(Component.translatable("option.combatedit.profile.base_profile"), baseProfiles.toArray(new BaseProfile.Info[0]), currentSelected)
                .setNameProvider(BaseProfile.Info::name)
                .setTooltipSupplier(info -> Optional.of(new Component[] { info.description() }))
                .setSaveConsumer(value -> {
                    if (value.id() != null) {
                        settings.setSelectedBaseProfile(value.id());
                    }
                })
                .build();
        category.addEntry(profileSelector);
        category.addEntry(ENTRY_BUILDER.startTextField(Component.translatable("option.combatedit.profile.custom_base_profile"), settings.getSelectedBaseProfile().toString())
                .setDisplayRequirement(() -> profileSelector.getValue().id() == null)
                .setSaveConsumer(value -> {
                    if (profileSelector.getValue().id() == null) {
                        settings.setSelectedBaseProfile(Identifier.parse(value));
                    }
                })
                .setErrorSupplier(value -> {
                    try {
                        Identifier.parse(value);
                    } catch (IdentifierException e) {
                        return Optional.of(Component.translatable("error.combatedit.invalid_identifier"));
                    }

                    return Optional.empty();
                })
                .build());
        category.addEntry(ENTRY_BUILDER.startTextDescription(Component
                        .translatable("option.combatedit.profile.custom_base_profile.notice")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)))
                .setDisplayRequirement(() -> profileSelector.getValue().id() == null)
                .build());
    }

    private static void createClientCategory(Settings.ClientOnly clientOnly, ConfigBuilder builder) {
        builder.getOrCreateCategory(Component.translatable("category.combatedit.client_only"))
                .addEntry(ENTRY_BUILDER.startBooleanToggle(Component.translatable("option.combatedit.client_only.disable_new_tooltips"), clientOnly.shouldDisableNewTooltips())
                        .setSaveConsumer(clientOnly::setDisableNewTooltips)
                        .build());
    }

    private static void createEntityCategory(List<EntityAttributes> entityAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Component.translatable("category.combatedit.entity"));
        addLocalWarning(category);
        addIngameWarning(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.entity.entity_attributes"), entityAttributes, (value, list) -> createEntry(value != null ? value : EntityAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    entityAttributes.clear();
                    entityAttributes.addAll(value);
        }).setExpanded(true).build());
    }

    private static void createItemCategory(List<ItemAttributes> itemAttributes, List<ItemComponents> itemComponents, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Component.translatable("category.combatedit.item"));
        addLocalWarning(category);
        addIngameWarning(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.item.item_attributes"), itemAttributes, (value, list) -> createEntry(value != null ? value : ItemAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    itemAttributes.clear();
                    itemAttributes.addAll(value);
                })
                .setExpanded(true)
                .build());
        category.addEntry(ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.item.item_components"), itemComponents, (value, list) -> createEntry(value != null ? value : ItemComponents.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    itemComponents.clear();
                    itemComponents.addAll(value);
                })
                .setExpanded(true)
                .build());
    }

    private static EnumListEntry<?> optionalBooleanEntry(Component fieldName, Boolean initialValue, Consumer<Boolean> saveConsumer) {
        return ENTRY_BUILDER.startEnumSelector(fieldName, TriStateOption.class, TriStateOption.fromBoolean(initialValue))
                .setSaveConsumer(value -> saveConsumer.accept(value.asBoolean()))
                .setEnumNameProvider(anEnum -> ((TriStateOption) anEnum).getText())
                .build();
    }

    private static void createSoundCategory(MutableConfiguration configuration, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Component.translatable("category.combatedit.sounds"));
        addLocalWarning(category);
        for (var sound : Configuration.CONFIGURABLE_SOUNDS) {
            String translationKey = determineSoundTranslationKey(sound);
            category.addEntry(optionalBooleanEntry(Component.translatable(translationKey), configuration.isSoundEnabled(sound.location()).orElse(null),
                    value -> configuration.setSoundEnabled(sound.location(), value)));
        }
    }

    private static void createMiscCategory(MutableConfiguration.MiscOptions miscOptions, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Component.translatable("category.combatedit.misc"));
        addLocalWarning(category);
        category.addEntry(optionalBooleanEntry(Component.translatable("option.combatedit.misc.enable_1_8_knockback"), miscOptions.is1_8KnockbackEnabled().orElse(null),
                miscOptions::set1_8KnockbackEnabled));
        category.addEntry(optionalBooleanEntry(Component.translatable("option.combatedit.misc.disable_sweeping_without_enchantment"), miscOptions.isSweepingWithoutEnchantmentDisabled().orElse(null),
                miscOptions::setSweepingWithoutEnchantmentDisabled));
    }

    private static String determineSoundTranslationKey(SoundEvent sound) {
        Language language = Language.getInstance();
        String key = "subtitles." + sound.location().getPath();
        if (language.has(key)) {
            return key;
        }

        return "combatedit.sound." + sound.location().getPath();
    }

    private static ObjectListEntry<EntityAttributes> createEntry(EntityAttributes attributes, int entryIndex) {
        var copy = attributes.copy();

        var entityTypeEntry = ENTRY_BUILDER.startDropdownMenu(Component.translatable("option.combatedit.entity.entity_attributes.entity"),
                        ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(BuiltInRegistries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getValue(attributes.getEntityId())),
                        ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(BuiltInRegistries.ENTITY_TYPE))
                .setSelections(BuiltInRegistries.ENTITY_TYPE.keySet())
                .build();
        var attributeValueMap = ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry"), List.copyOf(attributes.getBaseValues()),
                        (value, list) -> createEntry(value != null ? value : EntityAttributes.AttributeBaseValue.getDefault()))
                .setSaveConsumer(values -> {
                    attributes.getBaseValues().clear();
                    attributes.getBaseValues().addAll(values);
                })
                .setExpanded(true)
                .build();

        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.entity.entry"),
                List.of(
                        entityTypeEntry,
                        attributeValueMap
                ),
                ObjectAdapter.create(
                        () -> {
                            copy.setEntityId(entityTypeEntry.getValue());
                            copy.getBaseValues().clear();
                            copy.getBaseValues().addAll(attributeValueMap.getValue());

                            return copy;
                        },
                        () -> Optional.of(EntityAttributes.getDefault())
                )).setExpanded(entryIndex < 5).build();
    }

    private static ObjectListEntry<EntityAttributes.AttributeBaseValue> createEntry(EntityAttributes.AttributeBaseValue baseValue) {
        var attributeEntry = ENTRY_BUILDER.startDropdownMenu(
                Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.attribute"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(BuiltInRegistries.ATTRIBUTE, BuiltInRegistries.ATTRIBUTE.getValue(baseValue.attribute())),
                ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(BuiltInRegistries.ATTRIBUTE)
        ).setSelections(BuiltInRegistries.ATTRIBUTE.keySet()).build();
        var baseValueEntry = ENTRY_BUILDER.startDoubleField(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.base_value"), baseValue.baseValue())
                .build();
        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.entity.entity_attributes.attribute_entry.entry"),
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

    private static DropdownBoxEntry<Identifier> createItemEntry(Identifier currentItemId) {
        return ENTRY_BUILDER.startDropdownMenu(Component.translatable("option.combatedit.item.item_attributes.item"),
                        DropdownMenuBuilder.TopCellElementBuilder.ofItemIdentifier(BuiltInRegistries.ITEM.getValue(currentItemId)),
                        ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(BuiltInRegistries.ITEM))
                .setSelections(BuiltInRegistries.ITEM.keySet())
                .build();
    }

    private static ObjectListEntry<ItemAttributes> createEntry(ItemAttributes attributes, int entryIndex) {
        var copy = new ItemAttributes(attributes.getItemId(), List.copyOf(attributes.getModifiers()), attributes.isOverrideDefault());

        var itemEntry = createItemEntry(attributes.getItemId());
        var modifiersEntry = ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.item.item_attributes.modifier_entry"),
                        List.copyOf(attributes.getModifiers()),
                        (value, list) -> createEntry(value != null ? value : ItemAttributes.ModifierEntry.getDefault()))
                .setSaveConsumer(value -> {
                    attributes.getModifiers().clear();
                    attributes.getModifiers().addAll(value);
                })
                .setExpanded(true)
                .build();
        var overrideDefaultToggle = ENTRY_BUILDER.startBooleanToggle(Component.translatable("option.combatedit.item.item_attributes.override_defaults"), attributes.isOverrideDefault())
                .setSaveConsumer(attributes::setOverrideDefault)
                .build();

        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.item.entry"),
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
                Component.translatable("option.combatedit.item.item_attributes.modifier_entry.attribute"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(BuiltInRegistries.ATTRIBUTE, BuiltInRegistries.ATTRIBUTE.getValue(modifierEntry.attribute())),
                ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(BuiltInRegistries.ATTRIBUTE)
        ).setSelections(BuiltInRegistries.ATTRIBUTE.keySet()).build();
        var modifierIdEntry = ENTRY_BUILDER.startStrField(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.modifier_id"), modifierEntry.modifierId() != null ? modifierEntry.modifierId().toString() : "")
                .setErrorSupplier(attributeModifierIdErrorSupplier())
                .setTooltip(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.modifier_id.tooltip"))
                .build();
        var valueEntry = ENTRY_BUILDER.startDoubleField(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.value"), modifierEntry.value())
                .build();
        var operationEntry = ENTRY_BUILDER.startDropdownMenu(
                Component.translatable("option.combatedit.item.item_attributes.modifier_entry.operation"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofStringIdentifiable(AttributeModifier.Operation.class, modifierEntry.operation()),
                ExtendedDropdownMenus.CellCreatorBuilder.ofStringIdentifiableAutoWidth(AttributeModifier.Operation.class)
        ).setSelections(List.of(AttributeModifier.Operation.values())).build();
        var slotEntry = ENTRY_BUILDER.startDropdownMenu(
                Component.translatable("option.combatedit.item.item_attributes.modifier_entry.slot"),
                ExtendedDropdownMenus.TopCellElementBuilder.ofStringIdentifiable(EquipmentSlotGroup.class, modifierEntry.slot()),
                ExtendedDropdownMenus.CellCreatorBuilder.ofStringIdentifiableAutoWidth(EquipmentSlotGroup.class)
        ).setSelections(List.of(EquipmentSlotGroup.values())).build();

        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.item.item_attributes.modifier_entry.entry"),
                List.of(
                        attributeEntry,
                        modifierIdEntry,
                        valueEntry,
                        operationEntry,
                        slotEntry
                ),
                ObjectAdapter.create(
                        () -> new ItemAttributes.ModifierEntry(attributeEntry.getValue(),
                                !modifierIdEntry.getValue().isEmpty() ? Identifier.parse(modifierIdEntry.getValue()) : null,
                                valueEntry.getValue(),
                                operationEntry.getValue(),
                                slotEntry.getValue()),
                        () -> Optional.of(ItemAttributes.ModifierEntry.getDefault())))
                .setExpanded(true)
                .build();
    }
    private static ObjectListEntry<ItemComponents> createEntry(ItemComponents components, int entryIndex) {
        var copy = new ItemComponents(components.getItemId(), List.copyOf(components.getChanges()));

        var itemEntry = createItemEntry(components.getItemId());
        var componentsEntry = ENTRY_BUILDER.startObjectList(Component.translatable("option.combatedit.item.item_components.component_change_entry"),
                        List.copyOf(components.getChanges()),
                        (value, list) -> createEntry(value != null ? value : ItemComponents.ComponentChangeEntry.getDefault()))
                .setSaveConsumer(value -> {
                    components.getChanges().clear();
                    components.getChanges().addAll(value);
                })
                .setExpanded(true)
                .build();

        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.item.entry"),
                        List.of(
                                itemEntry,
                                componentsEntry
                        ),
                        ObjectAdapter.create(
                                () -> {
                                    copy.setItemId(itemEntry.getValue());
                                    copy.getChanges().clear();
                                    copy.getChanges().addAll(componentsEntry.getValue());

                                    return copy;
                                },
                                () -> Optional.of(ItemComponents.getDefault())
                        ))
                .setExpanded(entryIndex < 5)
                .build();
    }

    private static ObjectListEntry<ItemComponents.ComponentChangeEntry> createEntry(ItemComponents.ComponentChangeEntry componentChangeEntry) {
        var componentTypeEntry = ENTRY_BUILDER.startDropdownMenu(
                Component.translatable("option.combatedit.item.item_components.component_change_entry.component"),
                        ExtendedDropdownMenus.TopCellElementBuilder.ofRegistryIdentifier(BuiltInRegistries.DATA_COMPONENT_TYPE, BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentChangeEntry.componentType())),
                        ExtendedDropdownMenus.CellCreatorBuilder.ofRegistryIdentifier(BuiltInRegistries.DATA_COMPONENT_TYPE)
                ).setSelections(BuiltInRegistries.DATA_COMPONENT_TYPE.keySet())
                .setErrorSupplier(ident -> {
                    if (!DataComponents.ATTRIBUTE_MODIFIERS.equals(BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(ident))) {
                        return Optional.empty();
                    }
                    return Optional.of(Component.translatable("option.combatedit.item.item_components.component_change_entry.component.attribute_modifiers_unsupported"));
                })
                .build();
        var changeTypeEntry = ENTRY_BUILDER.startEnumSelector(Component.translatable("option.combatedit.item.item_components.component_change_entry.change_type"), ItemComponents.ChangeType.class, componentChangeEntry.changeType())
                .setEnumNameProvider(anEnum -> ((ItemComponents.ChangeType) anEnum).getText())
                .build();
        var valueEntry = ENTRY_BUILDER.startStrField(Component.translatable("option.combatedit.item.item_components.component_change_entry.value"), componentChangeEntry.value())
                .setTooltipSupplier(componentValueTooltipSupplier(componentTypeEntry, changeTypeEntry))
                .setErrorSupplier(componentValueErrorSupplier(componentTypeEntry, changeTypeEntry))
                .build();

        return ENTRY_BUILDER.startObjectField(Component.translatable("option.combatedit.item.item_components.component_change_entry.entry"),
                        List.of(
                                componentTypeEntry,
                                changeTypeEntry,
                                valueEntry
                        ),
                        ObjectAdapter.create(
                                () -> new ItemComponents.ComponentChangeEntry(componentTypeEntry.getValue(),
                                        changeTypeEntry.getValue(),
                                        valueEntry.getValue()),
                                () -> Optional.of(ItemComponents.ComponentChangeEntry.getDefault())))
                .setExpanded(true)
                .build();
    }

    private static Function<String, Optional<Component>> attributeModifierIdErrorSupplier() {
        return value -> {
            if (value.isEmpty()) {
                return Optional.empty();
            }
            Identifier id;
            try {
                id = Identifier.parse(value);
            } catch (IdentifierException e) {
                return Optional.of(Component.translatable("error.combatedit.invalid_identifier"));
            }
            if (id.getNamespace().equals(ReservedIdentifiers.RESERVED_NAMESPACE)) {
                return Optional.of(Component.translatable("error.combatedit.disallowed_namespace"));
            }
            return Optional.empty();
        };
    }

    private static Supplier<Optional<Component[]>> componentValueTooltipSupplier(DropdownBoxEntry<Identifier> componentTypeEntry, EnumListEntry<ItemComponents.ChangeType> changeTypeEntry) {
        return () -> {
            if (changeTypeEntry.getValue().equals(ItemComponents.ChangeType.REMOVE)) {
                return Optional.of(new Component[] {Component.translatable("option.combatedit.item.item_components.component_change_entry.value.remove_tooltip")});
            }

            var componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentTypeEntry.getValue());
            if (componentType == null) {
                return Optional.empty();
            }

            if (Unit.CODEC.equals(componentType.codec())) {
                return Optional.of(new Component[] {Component.translatable("option.combatedit.item.item_components.component_change_entry.value.unit_tooltip")});
            }

            return Optional.empty();
        };
    }

    private static Function<String, Optional<Component>> componentValueErrorSupplier(DropdownBoxEntry<Identifier> componentTypeEntry, EnumListEntry<ItemComponents.ChangeType> changeTypeEntry) {
        return val -> {
            if (changeTypeEntry.getValue().equals(ItemComponents.ChangeType.REMOVE)) return Optional.empty();

            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentTypeEntry.getValue());
            if (type == null) return Optional.empty();
            if (Unit.CODEC.equals(type.codec())) return Optional.empty();

            var reader = TagParser.create(NbtOps.INSTANCE);
            Tag element;
            try {
                element = reader.parseFully(val);
            } catch (CommandSyntaxException e) {
                return Optional.of(Component.literal(e.getMessage()));
            }
            var result = type.codecOrThrow().parse(NbtOps.INSTANCE, element);
            return result.mapOrElse(
                    ignored -> Optional.empty(),
                    error -> Optional.of(Component.translatable("arguments.item.component.malformed",
                            componentTypeEntry.getValue(), error.message())));
        };
    }
}
