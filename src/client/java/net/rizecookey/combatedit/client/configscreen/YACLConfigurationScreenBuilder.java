package net.rizecookey.combatedit.client.configscreen;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.rizecookey.combatedit.client.CombatEditClient;
import net.rizecookey.combatedit.client.configscreen.controller.EntityAttributeSubconfigButtonController;
import net.rizecookey.combatedit.client.configscreen.controller.ItemAttributeSubconfigButtonController;
import net.rizecookey.combatedit.client.configscreen.controller.ItemComponentSubconfigButtonController;
import net.rizecookey.combatedit.configuration.representation.Configuration;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;
import net.rizecookey.yacl3.extension.gui.controllers.IdentifierController;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.EntityAttributes;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public final class YACLConfigurationScreenBuilder {
    private static final Settings DEFAULTS = Settings.loadDefault();

    private YACLConfigurationScreenBuilder() {}

    public static Screen buildScreen(CombatEditClient combatEditClient, Screen parentScreen) {
        Minecraft client = Minecraft.getInstance();
        Settings settings = combatEditClient.getCurrentSettings().copy();
        var config = settings.getConfigurationOverrides();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("title.combatedit.config"))
                .save(() -> {
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
                })
                .categories(List.of(
                        createProfileCategory(settings),
                        createClientCategory(settings.getClientOnly()),
                        createEntityCategory(config.getEntityAttributes()),
                        createItemCategory(config.getItemAttributes(), config.getItemComponents()),
                        createSoundCategory(config),
                        createMiscCategory(config.getMiscOptions())
                ))
                .build().generateScreen(parentScreen);
    }

    private static void addLocalWarning(ConfigCategory.Builder category) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.getConnection().getConnection().isMemoryConnection()) {
            return;
        }

        category.option(LabelOption.createBuilder()
                .line(Component
                        .translatable("option.combatedit.warn.local_only")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)))
                .build());
    }

    private static void addIngameWarning(ConfigCategory.Builder category) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || !client.getConnection().getConnection().isMemoryConnection()) {
            return;
        }

        category.option(LabelOption.createBuilder()
                .line(Component
                        .translatable("option.combatedit.warn.ingame")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)))
                .build());
    }

    private static ConfigCategory createProfileCategory(Settings settings) {
        var category = ConfigCategory.createBuilder()
                .name(Component.translatable("category.combatedit.profile"));
        addLocalWarning(category);
        addIngameWarning(category);

        List<BaseProfile.Info> baseProfiles = new ArrayList<>(Arrays.stream(BaseProfile.IntegratedProfiles.values())
                .map(BaseProfile.IntegratedProfiles::getInfo)
                .toList());
        var customProfile = new BaseProfile.Info(null,
                Component.translatable("option.combatedit.profile.base_profile.custom.name"),
                Component.translatable("option.combatedit.profile.base_profile.custom.description"));
        baseProfiles.add(customProfile);

        Function<Identifier, BaseProfile.Info> infoProvider = identifier -> baseProfiles.stream()
                .filter(data -> identifier.equals(data.id()))
                .findFirst()
                .orElse(customProfile);

        var profileSelector = Option.<BaseProfile.Info>createBuilder()
                .name(Component.translatable("option.combatedit.profile.base_profile"))
                .description(info -> OptionDescription.of(info.description()))
                .controller(opt -> CyclingListControllerBuilder.create(opt)
                        .values(baseProfiles)
                        .formatValue(BaseProfile.Info::name))
                .binding(
                        infoProvider.apply(DEFAULTS.getSelectedBaseProfile()),
                        () -> infoProvider.apply(settings.getSelectedBaseProfile()),
                        value -> {
                            if (value.id() == null) {
                                return;
                            }

                            settings.setSelectedBaseProfile(value.id());
                        }
                ).build();
        category.option(profileSelector);

        var customProfileSelector = Option.<String>createBuilder()
                .name(Component.translatable("option.combatedit.profile.custom_base_profile"))
                .description(OptionDescription.of(Component
                        .translatable("option.combatedit.profile.custom_base_profile.notice")
                        .withStyle(style -> style.withColor(ChatFormatting.RED))))
                .customController(IdentifierController::new)
                .binding(
                        DEFAULTS.getSelectedBaseProfile().toString(),
                        () -> settings.getSelectedBaseProfile().toString(),
                        value -> {
                            Identifier identifier;
                            try {
                                identifier = Identifier.parse(value);
                            } catch (IdentifierException e) {
                                identifier = DEFAULTS.getSelectedBaseProfile();
                            }
                            settings.setSelectedBaseProfile(identifier);
                        }
                )
                .available(profileSelector.pendingValue().id() == null)
                .build();
        category.option(customProfileSelector);
        profileSelector.addEventListener((opt, event) -> {
            if (event != OptionEventListener.Event.STATE_CHANGE) {
                return;
            }

            boolean available = opt.pendingValue().id() == null;
            customProfileSelector.setAvailable(available);
            if (!available) {
                customProfileSelector.requestSet(opt.pendingValue().id().toString());
            }
        });
        return category.build();
    }

    private static ConfigCategory createClientCategory(Settings.ClientOnly clientOnly) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("category.combatedit.client_only"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("option.combatedit.client_only.disable_new_tooltips"))
                        .controller(TickBoxControllerBuilder::create)
                        .binding(
                                DEFAULTS.getClientOnly().shouldDisableNewTooltips(),
                                clientOnly::shouldDisableNewTooltips,
                                clientOnly::setDisableNewTooltips
                        ).build())
                .build();
    }

    private static ConfigCategory createEntityCategory(List<EntityAttributes> entityAttributes) {
        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("category.combatedit.entity"));
        addLocalWarning(builder);
        addIngameWarning(builder);
        return builder.group(ListOption.<EntityAttributes>createBuilder()
                        .name(Component.translatable("option.combatedit.entity.entity_attributes"))
                        .binding(
                                DEFAULTS.getConfigurationOverrides().getEntityAttributes(),
                                () -> entityAttributes,
                                newList -> {
                                    entityAttributes.clear();
                                    entityAttributes.addAll(newList);
                                })
                        .initial(EntityAttributes::getDefault)
                        .customController(EntityAttributeSubconfigButtonController::new)
                        .build()
                ).build();
    }

    private static ConfigCategory createItemCategory(List<ItemAttributes> itemAttributes, List<ItemComponents> itemComponents) {
        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("category.combatedit.item"));
        addLocalWarning(builder);
        addIngameWarning(builder);
        return builder
                .group(ListOption.<ItemAttributes>createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_attributes"))
                        .binding(
                                DEFAULTS.getConfigurationOverrides().getItemAttributes(),
                                () -> itemAttributes,
                                newList -> {
                                    itemAttributes.clear();
                                    itemAttributes.addAll(newList);
                                }
                        ).initial(ItemAttributes::getDefault)
                        .customController(ItemAttributeSubconfigButtonController::new)
                        .build()
                ).group(ListOption.<ItemComponents>createBuilder()
                        .name(Component.translatable("option.combatedit.item.item_components"))
                        .binding(
                                DEFAULTS.getConfigurationOverrides().getItemComponents(),
                                () -> itemComponents,
                                newList -> {
                                    itemComponents.clear();
                                    itemComponents.addAll(newList);
                                }
                        ).initial(ItemComponents::getDefault)
                        .customController(ItemComponentSubconfigButtonController::new)
                        .build()
                ).build();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Option<TriStateOption> triStateOption(Component name,
                                                         Optional<Boolean> defaultValue,
                                                         Supplier<Optional<Boolean>> getter,
                                                         Consumer<@Nullable Boolean> setter) {
        return Option.<TriStateOption>createBuilder()
                .name(name)
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(TriStateOption.class)
                        .formatValue(TriStateOption::getText))
                .binding(Binding.generic(
                        defaultValue,
                        getter,
                        value -> setter.accept(value.orElse(null))
                ).xmap(
                        boolOpt -> TriStateOption.fromBoolean(boolOpt.orElse(null)),
                        triState -> Optional.ofNullable(triState.asBoolean()))
                ).build();
    }

    private static ConfigCategory createSoundCategory(MutableConfiguration configuration) {
        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("category.combatedit.sounds"));
        addLocalWarning(builder);

        for (var sound : Configuration.CONFIGURABLE_SOUNDS) {
            String translationKey = determineSoundTranslationKey(sound);
            builder.option(triStateOption(Component.translatable(translationKey),
                    DEFAULTS.getConfigurationOverrides().isSoundEnabled(sound.location()),
                    () -> configuration.isSoundEnabled(sound.location()),
                    value -> configuration.setSoundEnabled(sound.location(), value)
            )).build();
        }

        return builder.build();
    }

    private static ConfigCategory createMiscCategory(MutableConfiguration.MiscOptions miscOptions) {
        var builder = ConfigCategory.createBuilder()
                        .name(Component.translatable("category.combatedit.misc"));
        addLocalWarning(builder);

        var defaultMiscOptions = DEFAULTS.getConfigurationOverrides().getMiscOptions();

        return builder
                .option(triStateOption(
                        Component.translatable("option.combatedit.misc.enable_1_8_knockback"),
                        defaultMiscOptions.is1_8KnockbackEnabled(),
                        miscOptions::is1_8KnockbackEnabled,
                        miscOptions::set1_8KnockbackEnabled
                )).option(triStateOption(
                        Component.translatable("option.combatedit.misc.disable_sweeping_without_enchantment"),
                        defaultMiscOptions.isSweepingWithoutEnchantmentDisabled(),
                        miscOptions::isSweepingWithoutEnchantmentDisabled,
                        miscOptions::setSweepingWithoutEnchantmentDisabled
                )).build();
    }

    private static String determineSoundTranslationKey(SoundEvent sound) {
        Language language = Language.getInstance();
        String key = "subtitles." + sound.location().getPath();
        if (language.has(key)) {
            return key;
        }

        return "combatedit.sound." + sound.location().getPath();
    }
}
