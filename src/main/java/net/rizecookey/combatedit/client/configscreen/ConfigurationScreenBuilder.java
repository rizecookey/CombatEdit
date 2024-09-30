package net.rizecookey.combatedit.client.configscreen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Language;
import net.rizecookey.clothconfig2.extension.api.ExtendedConfigEntryBuilder;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectAdapter;
import net.rizecookey.clothconfig2.extension.gui.entries.ObjectListEntry;
import net.rizecookey.clothconfig2.extension.impl.builders.ExtendedDropdownMenus;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ConfigurationScreenBuilder {
    private static final ExtendedConfigEntryBuilder ENTRY_BUILDER = ExtendedConfigEntryBuilder.create();

    public static Screen buildScreen(CombatEdit combatEdit, Path savePath, Screen parentScreen) {
        Settings settings;
        try {
            settings = combatEdit.loadSettings();
            settings.validate();
        } catch (InvalidConfigurationException e) {
            return new InvalidConfigScreen(e, () -> MinecraftClient.getInstance().setScreen(buildScreen(combatEdit, savePath, parentScreen)));
        }

        var builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Text.translatable("title.combatedit.config"))
                .setSavingRunnable(() -> {
                    try {
                        settings.save(savePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e); // TODO Maybe show notification?
                    }

                    var client = MinecraftClient.getInstance();
                    if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection().isLocal()) {
                        var server = MinecraftClient.getInstance().getServer();
                        assert server != null;
                        server.reloadResources(server.getDataPackManager().getEnabledIds());
                    }
                });

        var config = settings.getConfigurationOverrides();
        createProfileCategory(settings, builder);
        createEntityCategory(config.getEntityAttributes(), builder);
        createItemCategory(config.getItemAttributes(), builder);
        createSoundCategory(config, builder);
        createMiscCategory(config.getMiscOptions(), builder);

        return builder.build();
    }

    private static void addLocalWarning(ConfigCategory category) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || client.getNetworkHandler().getConnection().isLocal()) {
            return;
        }

        category.addEntry(ENTRY_BUILDER.startTextDescription(Text
                .translatable("option.combatedit.warn.local_only")
                .styled(style -> style.withColor(Formatting.RED))).build());
    }

    private static void addIngameWarning(ConfigCategory category) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || !client.getNetworkHandler().getConnection().isLocal()) {
            return;
        }

        category.addEntry(ENTRY_BUILDER.startTextDescription(Text
                .translatable("option.combatedit.warn.ingame")
                .styled(style -> style.withColor(Formatting.RED))).build());
    }

    private static void createProfileCategory(Settings settings, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.profile"));
        addLocalWarning(category);
        addIngameWarning(category);

        List<BaseProfile.Info> baseProfiles = new ArrayList<>(Arrays.stream(BaseProfile.IntegratedProfiles.values())
                .map(BaseProfile.IntegratedProfiles::getInfo)
                .toList());
        var customProfile = new BaseProfile.Info(null,
                Text.translatable("option.combatedit.profile.base_profile.custom.name"),
                Text.translatable("option.combatedit.profile.base_profile.custom.description"));
        baseProfiles.add(customProfile);

        BaseProfile.Info currentSelected = baseProfiles.stream()
                .filter(data -> settings.getSelectedBaseProfile().equals(data.id()))
                .findFirst()
                .orElse(customProfile);

        var profileSelector = ENTRY_BUILDER.startSelector(Text.translatable("option.combatedit.profile.base_profile"), baseProfiles.toArray(new BaseProfile.Info[0]), currentSelected)
                .setNameProvider(BaseProfile.Info::name)
                .setTooltipSupplier(info -> Optional.of(new Text[] { info.description() }))
                .setSaveConsumer(value -> {
                    if (value.id() != null) {
                        settings.setSelectedBaseProfile(value.id());
                    }
                })
                .build();
        category.addEntry(profileSelector);
        category.addEntry(ENTRY_BUILDER.startTextField(Text.translatable("option.combatedit.profile.custom_base_profile"), settings.getSelectedBaseProfile().toString())
                .setDisplayRequirement(() -> profileSelector.getValue().id() == null)
                .setSaveConsumer(value -> {
                    if (profileSelector.getValue().id() == null) {
                        settings.setSelectedBaseProfile(new Identifier(value));
                    }
                })
                .setErrorSupplier(value -> {
                    try {
                        new Identifier(value);
                    } catch (InvalidIdentifierException e) {
                        return Optional.of(Text.translatable("error.combatedit.invalid_identifier"));
                    }

                    return Optional.empty();
                })
                .build());
        category.addEntry(ENTRY_BUILDER.startTextDescription(Text
                        .translatable("option.combatedit.profile.custom_base_profile.notice")
                        .styled(style -> style.withColor(Formatting.RED)))
                .setDisplayRequirement(() -> profileSelector.getValue().id() == null)
                .build());
    }

    private static void createEntityCategory(List<EntityAttributes> entityAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.entity"));
        addLocalWarning(category);
        addIngameWarning(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.entity.entity_attributes"), entityAttributes, (value, list) -> createEntry(value != null ? value : EntityAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    entityAttributes.clear();
                    entityAttributes.addAll(value);
        }).setExpanded(true).build());
    }

    private static void createItemCategory(List<ItemAttributes> itemAttributes, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.item"));
        addLocalWarning(category);
        addIngameWarning(category);
        category.addEntry(ENTRY_BUILDER.startObjectList(Text.translatable("option.combatedit.item.item_attributes"), itemAttributes, (value, list) -> createEntry(value != null ? value : ItemAttributes.getDefault(), list.getValue().size()))
                .setSaveConsumer(value -> {
                    itemAttributes.clear();
                    itemAttributes.addAll(value);
                })
                .setExpanded(true)
                .build());
    }

    private static EnumListEntry<?> optionalBooleanEntry(Text fieldName, Boolean initialValue, Consumer<Boolean> saveConsumer) {
        return ENTRY_BUILDER.startEnumSelector(fieldName, TriStateOption.class, TriStateOption.fromBoolean(initialValue))
                .setSaveConsumer(value -> saveConsumer.accept(value.asBoolean()))
                .setEnumNameProvider(anEnum -> ((TriStateOption) anEnum).getText())
                .build();
    }

    private static void createSoundCategory(MutableConfiguration configuration, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.sounds"));
        addLocalWarning(category);
        for (var sound : Configuration.CONFIGURABLE_SOUNDS) {
            String translationKey = determineSoundTranslationKey(sound);
            category.addEntry(optionalBooleanEntry(Text.translatable(translationKey), configuration.isSoundEnabled(sound.getId()).orElse(null),
                    value -> configuration.setSoundEnabled(sound.getId(), value)));
        }
    }

    private static void createMiscCategory(MutableConfiguration.MiscOptions miscOptions, ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(Text.translatable("category.combatedit.misc"));
        addLocalWarning(category);
        category.addEntry(optionalBooleanEntry(Text.translatable("option.combatedit.misc.enable_1_8_knockback"), miscOptions.is1_8KnockbackEnabled().orElse(null),
                miscOptions::set1_8KnockbackEnabled));
        category.addEntry(optionalBooleanEntry(Text.translatable("option.combatedit.misc.disable_sweeping_without_enchantment"), miscOptions.isSweepingWithoutEnchantmentDisabled().orElse(null),
                miscOptions::setSweepingWithoutEnchantmentDisabled));
    }

    private static String determineSoundTranslationKey(SoundEvent sound) {
        Language language = Language.getInstance();
        String key = "subtitles." + sound.getId().getPath();
        if (language.hasTranslation(key)) {
            return key;
        }

        return "combatedit.sound." + sound.getId().getPath();
    }

    private static ObjectListEntry<EntityAttributes> createEntry(EntityAttributes attributes, int entryIndex) {
        var copy = attributes.copy();

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

        return ENTRY_BUILDER.startObjectField(Text.translatable("option.combatedit.entity.entity_attributes.entry"),
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
