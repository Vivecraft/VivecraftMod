package org.vivecraft.neoforge;

import net.neoforged.fml.common.Mod;
import org.vivecraft.server.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public Vivecraft() {
        // init server config
        // this is too early for the lang files to be loaded, is needed to register the commands though
        // server config is validated again later to have the comments
        ServerConfig.init(null);
    }
}
