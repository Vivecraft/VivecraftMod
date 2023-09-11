package org.vivecraft.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.vivecraft.client.ClientConfig;
import org.vivecraft.server.ServerConfig;
import org.vivecraft.server.ServerUtil;

public class VivecraftMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // init server config
        ServerConfig.init(null);
        ClientConfig.init(null);

        // add server config commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            ServerUtil.registerCommands(dispatcher));
    }
}
