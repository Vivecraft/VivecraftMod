package org.vivecraft.forge.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.client.Xplat;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    @SubscribeEvent
    public static void crashWithForgeExtension(ServerAboutToStartEvent event) {
        if (Xplat.isModLoaded("vivecraftforgeextensions")) {
            throw new RuntimeException("The vivecraft mod cannot be used together with the 'Vivecraft Forge Extension'.\nThe Vivecraft Mod implements all features the forge extension has.\nRemove the 'Vivecraft Forge Extension' to resolve this error");
        }
    }
}
