package net.rizecookey.combatedit.client.configscreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.rizecookey.combatedit.client.CombatEditClient;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> ConfigurationScreenBuilder.buildScreen(CombatEditClient.getInstance(), parentScreen);
    }
}
