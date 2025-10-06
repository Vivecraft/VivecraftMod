package org.vivecraft.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.forge.Vivecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Vivecraft.MODID)
public class ClientForgeEvents {

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
