package net.rizecookey.combatedit.client.configscreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.Component;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.client.CombatEditClient;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class InvalidConfigScreen extends WarningScreen {
    private static final Component TITLE = Component.translatable("error.combatedit.invalid_config.title");
    private static final Component RESET_CONFIG = Component.translatable("button.combatedit.reset_config");
    private static final Component CLOSE_GAME = Component.translatable("button.combatedit.exit");

    private final Runnable onClose;

    public InvalidConfigScreen(InvalidConfigurationException exception, Runnable onClose) {
        super(TITLE, getErrorText(exception), getErrorText(exception));
        this.onClose = onClose;
    }

    private static Component getErrorText(InvalidConfigurationException exception) {
        return Component.translatable("error.combatedit.invalid_config.description", exception.getMessage())
                .withStyle(style -> style.withColor(ChatFormatting.RED));
    }

    @Override
    protected @NotNull Layout addFooterButtons() {
        LinearLayout horizontalButtons = LinearLayout.horizontal().spacing(8);
        horizontalButtons.addChild(Button
                .builder(RESET_CONFIG, button -> {
                    try {
                        CombatEdit.getInstance().resetSettings();
                    } catch (IOException e) {
                        CombatEditClient.sendErrorNotification(this.minecraft, "settings_save_error");
                    }
                    onClose.run();
                })
                .build());
        horizontalButtons.addChild(Button
                .builder(CLOSE_GAME, button -> {
                    minecraft.destroy();
                })
                .build());


        return horizontalButtons;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
