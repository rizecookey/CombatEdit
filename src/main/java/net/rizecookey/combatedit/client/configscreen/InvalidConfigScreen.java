package net.rizecookey.combatedit.client.configscreen;

import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;

import java.io.IOException;

public class InvalidConfigScreen extends WarningScreen {
    private static final Text TITLE = Text.translatable("error.combatedit.invalid_config.title");
    private static final Text RESET_CONFIG = Text.translatable("button.combatedit.reset_config");
    private static final Text CLOSE_GAME = Text.translatable("button.combatedit.exit");

    private final Runnable onClose;

    public InvalidConfigScreen(InvalidConfigurationException exception, Runnable onClose) {
        super(TITLE, getErrorText(exception), getErrorText(exception));
        this.onClose = onClose;
    }

    private static Text getErrorText(InvalidConfigurationException exception) {
        return Text.translatable("error.combatedit.invalid_config.description", exception.getMessage())
                .styled(style -> style.withColor(Formatting.RED));
    }

    @Override
    protected LayoutWidget getLayout() {
        DirectionalLayoutWidget horizontalButtons = DirectionalLayoutWidget.horizontal().spacing(8);
        horizontalButtons.add(ButtonWidget
                .builder(RESET_CONFIG, button -> {
                    try {
                        CombatEdit.getInstance().resetSettings();
                    } catch (IOException e) {
                        throw new RuntimeException(e); // TODO maybe add notification
                    }
                    onClose.run();
                })
                .build());
        horizontalButtons.add(ButtonWidget
                .builder(CLOSE_GAME, button -> {
                    assert client != null;
                    client.stop();
                })
                .build());


        return horizontalButtons;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
