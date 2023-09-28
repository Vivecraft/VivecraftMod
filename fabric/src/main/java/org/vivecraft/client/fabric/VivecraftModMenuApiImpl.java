package org.vivecraft.client.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;

public class VivecraftModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return VivecraftMainSettings::new;
    }
}
