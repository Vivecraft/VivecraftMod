package org.vivecraft.neoforge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.neoforge.Vivecraft;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME, modid = Vivecraft.MODID)
public class ClientGameEvents {

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent registerClientCommandsEvent) {
        registerClientCommandsEvent.getDispatcher()
            .register(Commands.literal("vivecraft-client-config").executes(context -> {
                Minecraft mc = Minecraft.getInstance();
                mc.schedule(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                return 1;
            }));
    }
}
