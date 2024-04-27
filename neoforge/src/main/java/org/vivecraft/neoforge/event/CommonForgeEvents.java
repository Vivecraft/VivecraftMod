package org.vivecraft.neoforge.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.vivecraft.server.ServerUtil;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerUtil.registerCommands(event.getDispatcher());
    }
}
