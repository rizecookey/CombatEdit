package net.rizecookey.combatedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.provider.ConfigurationManager;
import net.rizecookey.combatedit.utils.TextUtils;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.rizecookey.combatedit.CombatEdit.LOGGER;

public class CombatEditCommand implements CommandRegistrationCallback {
    private static final DynamicCommandExceptionType INVALID_ID = new DynamicCommandExceptionType(
            id -> TextUtils.fallBackToServerTranslation(Text.translatable("command.combatedit.profile.set.error.invalid_base_profile", id.toString()))
    );
    private static final DynamicCommandExceptionType ALREADY_SET = new DynamicCommandExceptionType(
            id -> TextUtils.fallBackToServerTranslation(Text.translatable("command.combatedit.profile.set.error.already_enabled", id.toString()))
    );
    private static final DynamicCommandExceptionType FAILED_TO_SAVE = new DynamicCommandExceptionType(
            none -> TextUtils.fallBackToServerTranslation(Text.translatable("command.combatedit.profile.set.error.settings_save_failed"))
    );

    private final CombatEdit combatEdit;
    private final ConfigurationManager configurationManager;

    public CombatEditCommand(CombatEdit combatEdit) {
        this.combatEdit = combatEdit;
        this.configurationManager = combatEdit.getConfigurationManager();
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("combatedit")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("profile")
                        .then(literal("list")
                                .executes(this::executeList))
                        .then(literal("get")
                                .executes(this::executeGet))
                        .then(literal("set")
                                .then(argument("profile", IdentifierArgumentType.identifier())
                                        .suggests(this::provideSuggestions)
                                        .executes(this::executeSet)))));
    }

    private int executeList(CommandContext<ServerCommandSource> ctx) {
        var baseProfiles = configurationManager.getBaseProfiles();
        ctx.getSource().sendFeedback(() -> Text.translatable("command.combatedit.profile.list",
                baseProfiles.size(),
                Texts.join(baseProfiles.entrySet(), entry -> baseProfileToText(entry.getKey(), entry.getValue())
        )), false);
        return 1;
    }

    private int executeGet(CommandContext<ServerCommandSource> ctx) {
        var baseProfiles = configurationManager.getBaseProfiles();
        var selectedId = combatEdit.getCurrentSettings().getSelectedBaseProfile();

        ctx.getSource().sendFeedback(() -> Text.translatable("command.combatedit.profile.get", baseProfileToText(selectedId, baseProfiles.get(selectedId))), false);
        return 1;
    }

    private CompletableFuture<Suggestions> provideSuggestions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Set<Identifier> validIds = configurationManager.getBaseProfiles().keySet();
        validIds.stream()
                .filter(id -> id.toString().startsWith(builder.getRemaining()) || (id.getNamespace().equals("minecraft") && id.getPath().startsWith(builder.getRemaining())))
                .forEach(id -> builder.suggest(id.toString()));

        return builder.buildFuture();
    }

    private int executeSet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier id = ctx.getArgument("profile", Identifier.class);
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
        server.reloadResources(server.getDataPackManager().getEnabledIds());
        ctx.getSource().sendFeedback(() -> Text.translatable("command.combatedit.profile.set", baseProfileToText(id, baseProfiles.get(id))), true);

        return 1;
    }

    private static Text baseProfileToText(Identifier id, BaseProfile baseProfile) {
        return Text.literal(id.toString())
                .styled(style -> style.withColor(Formatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.empty()
                                .append(baseProfile.getName())
                                .append("\n")
                                .append(baseProfile.getDescription()))));
    }
}
