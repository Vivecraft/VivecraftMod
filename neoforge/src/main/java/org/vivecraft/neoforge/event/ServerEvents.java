package org.vivecraft.neoforge.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.server.config.ServerConfig;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = EventBusSubscriber.Bus.GAME, modid = Vivecraft.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void loadServerConfig(ServerStartingEvent serverStartingEvent) {
        // on the server reinit the ServerConfig here again, after the lang files got loaded, to have comments
        ServerConfig.init(null);
    }
}
