package org.vivecraft.mixin.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.Xplat;
import org.vivecraft.server.MinecraftServerExt;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerUtil;
import org.vivecraft.server.ServerVivePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerExt {

    @Unique
    Map<UUID, ServerVivePlayer> vivecraft$playersWithVivecraft = new HashMap<>();

    @Override
    @Unique
    public Map<UUID, ServerVivePlayer> vivecraft$getPlayersWithVivecraft() {
        return this.vivecraft$playersWithVivecraft;
    }

    @Inject(at = @At("HEAD"), method = "stopServer")
    private void vivecraft$stopExecutor(CallbackInfo ci) {
        if (Xplat.isDedicatedServer()) {
            ServerNetworking.LOGGER.info("shutting down vivecraft scheduler");
            ServerUtil.scheduler.shutdownNow();
        }
    }
}
