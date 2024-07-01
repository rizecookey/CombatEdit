package net.rizecookey.combatedit.client.configscreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.rizecookey.combatedit.CombatEdit;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> ConfigurationScreenBuilder.buildScreen(CombatEdit.getInstance().getConfig(), CombatEdit.DEFAULT_CONFIG_PATH, parentScreen);
    }
}
