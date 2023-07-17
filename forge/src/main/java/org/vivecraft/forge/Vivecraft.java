package org.vivecraft.forge;

import net.minecraftforge.fml.common.Mod;
import org.vivecraft.config.ClientConfig;
import org.vivecraft.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public Vivecraft() {
        // init server config
        ServerConfig.init(null);
        ClientConfig.init(null);
    }
}
