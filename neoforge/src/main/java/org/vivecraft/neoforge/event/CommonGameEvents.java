package org.vivecraft.neoforge.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.server.ServerUtil;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = Vivecraft.MODID)
public class CommonGameEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerUtil.registerCommands(event.getDispatcher(), event.getBuildContext());
    }
}
