package org.vivecraft.forge.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.vivecraft.server.ServerUtil;

@EventBusSubscriber(bus = Bus.FORGE)
public class CommonEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerUtil.registerCommands(event.getDispatcher());
    }
}
