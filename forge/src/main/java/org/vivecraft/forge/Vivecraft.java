package org.vivecraft.forge;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.vivecraft.server.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public Vivecraft() {
        // init server config
        ServerConfig.init(null);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, ()-> new IExtensionPoint.DisplayTest(
            () -> NetworkConstants.IGNORESERVERONLY, // only needed on server, client is optional
            (s,b) -> true // any version is good
        ));
    }
}
