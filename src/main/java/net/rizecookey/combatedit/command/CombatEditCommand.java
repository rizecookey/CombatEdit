package net.rizecookey.combatedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.utils.ComponentUtils;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class CombatEditCommand implements CommandRegistrationCallback {
    private static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
            id -> ComponentUtils.fallBackToServerTranslation(Component.translatable("command.combatedit.profile.set.error.invalid_base_profile", id.toString()))
    );
    private static final DynamicCommandExceptionType ALREADY_SET = new DynamicCommandExceptionType(
            id -> ComponentUtils.fallBackToServerTranslation(Component.translatable("command.combatedit.profile.set.error.already_enabled", id.toString()))
    );
    private static final DynamicCommandExceptionType FAILED_TO_SAVE = new DynamicCommandExceptionType(
            none -> ComponentUtils.fallBackToServerTranslation(Component.translatable("command.combatedit.profile.set.error.settings_save_failed"))
    );

    private final CombatEdit combatEdit;
    private final ConfigurationManager configurationManager;

    public CombatEditCommand(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.configurationManager = combatEdit.getConfigurationManager();
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(literal("combatedit")
                .requires(source -> source.hasPermission(2))
                .then(literal("profile")
                        .then(literal("list")
                                .executes(this::executeList))
                        .then(literal("get")
                                .executes(this::executeGet))
                        .then(literal("set")
                                .then(argument("profile", ResourceLocationArgument.id())
                                        .suggests(this::provideSuggestions)
                                        .executes(this::executeSet)))));
    }

    private int executeList(CommandContext<CommandSourceStack> ctx) {
        var baseProfiles = configurationManager.getBaseProfiles();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.combatedit.profile.list",
                baseProfiles.size(),
                net.minecraft.network.chat.ComponentUtils.formatList(baseProfiles.entrySet(), entry -> baseProfileToText(entry.getKey(), entry.getValue())
        )), false);
        return 1;
    }

    private int executeGet(CommandContext<CommandSourceStack> ctx) {
        var baseProfiles = configurationManager.getBaseProfiles();
        var selectedId = combatEdit.getCurrentSettings().getSelectedBaseProfile();

        ctx.getSource().sendSuccess(() -> Component.translatable("command.combatedit.profile.get", baseProfileToText(selectedId, baseProfiles.get(selectedId))), false);
        return 1;
    }

    private CompletableFuture<Suggestions> provideSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Set<ResourceLocation> validIds = configurationManager.getBaseProfiles().keySet();
        validIds.stream()
                .filter(id -> id.toString().startsWith(builder.getRemaining()) || (id.getNamespace().equals("minecraft") && id.getPath().startsWith(builder.getRemaining())))
                .forEach(id -> builder.suggest(id.toString()));

        return builder.buildFuture();
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation id = ctx.getArgument("profile", ResourceLocation.class);
        Settings settings = combatEdit.getCurrentSettings().copy();
        if (settings.getSelectedBaseProfile().equals(id)) {
            throw ALREADY_SET.create(id);
        }

        var baseProfiles = configurationManager.getBaseProfiles();
        if (!baseProfiles.containsKey(id)) {
            throw INVALID_ID.create(id);
        }

        settings.setSelectedBaseProfile(id);
        try {
            combatEdit.saveSettings(settings);
        } catch (IOException e) {
            var cmdException = FAILED_TO_SAVE.create(null);
            LOGGER.error("Failed to save settings file after changing base profile", e);
            throw cmdException;
        }

        MinecraftServer server = ctx.getSource().getServer();
        server.reloadResources(server.getPackRepository().getSelectedIds());
        ctx.getSource().sendSuccess(() -> Component.translatable("command.combatedit.profile.set", baseProfileToText(id, baseProfiles.get(id))), true);

        return 1;
    }

    private static Component baseProfileToText(ResourceLocation id, BaseProfile baseProfile) {
        return Component.literal(id.toString())
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent.ShowText(Component.empty()
                                .append(baseProfile.getName())
                                .append("\n")
                                .append(baseProfile.getDescription()))));
    }
}
