package org.vivecraft.fabric.client;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.Minecraft;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;

public class ClientEvents {
    public static void registerClientCommands() {
        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("vivecraft-client-config").executes(context -> {
                Minecraft mc = context.getSource().getClient();
                mc.tell(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                return 1;
            }));
    }
}
