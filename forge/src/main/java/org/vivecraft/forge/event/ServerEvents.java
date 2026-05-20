package org.vivecraft.forge.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.Xloader;
import org.vivecraft.forge.Vivecraft;
import org.vivecraft.server.config.ServerConfig;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = Vivecraft.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void crashWithForgeExtension(ServerAboutToStartEvent event) {
        if (Xloader.INSTANCE.isModLoaded("vivecraftforgeextensions")) {
            throw new RuntimeException(
                "The vivecraft mod cannot be used together with the 'Vivecraft Forge Extension'.\nThe Vivecraft Mod implements all features the forge extension has.\nRemove the 'Vivecraft Forge Extension' to resolve this error");
        }
    }

    @SubscribeEvent
    public static void loadServerConfig(ServerStartingEvent serverStartingEvent) {
        // on the server reinit the ServerConfig here again, after the lang files got loaded, to have comments
        ServerConfig.init(null);
    }
}
