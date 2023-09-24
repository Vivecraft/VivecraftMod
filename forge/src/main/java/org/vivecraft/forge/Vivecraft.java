package org.vivecraft.forge;

import org.vivecraft.server.config.ServerConfig;

import net.minecraftforge.fml.common.Mod;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public Vivecraft() {
        // init server config
        ServerConfig.init(null);
    }
}
