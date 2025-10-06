package org.vivecraft.fabric.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;

public class ClientEvents {
    public static void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("vivecraft-client-config").executes(context -> {
                Minecraft mc = context.getSource().getClient();
                mc.schedule(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                return 1;
            })));
    }
}
