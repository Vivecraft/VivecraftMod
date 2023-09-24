package org.vivecraft.fabric;

import org.vivecraft.server.ServerUtil;
import org.vivecraft.server.config.ServerConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class VivecraftMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // init server config
        ServerConfig.init(null);

        // add server config commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            ServerUtil.registerCommands(dispatcher));
    }
}
