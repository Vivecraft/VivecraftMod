package org.vivecraft.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.server.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public Vivecraft() {
        // init server config
        ServerConfig.init(null);

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new VivecraftMainSettings(screen)));
    }
}
