package org.vivecraft.forge.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.forge.Vivecraft;
import org.vivecraft.server.ServerUtil;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Vivecraft.MODID)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerUtil.registerCommands(event.getDispatcher(), event.getBuildContext());
    }
}
