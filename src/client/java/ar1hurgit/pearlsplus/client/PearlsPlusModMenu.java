package ar1hurgit.pearlsplus.client;

import ar1hurgit.pearlsplus.client.config.PearlsPlusConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class PearlsPlusModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PearlsPlusConfigScreen::new;
    }
}
