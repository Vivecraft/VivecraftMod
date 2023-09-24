package org.vivecraft.mixin.server;

import org.vivecraft.server.MinecraftServerExt;
import org.vivecraft.server.ServerVivePlayer;

import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerExt {

    @Unique
    Map<UUID, ServerVivePlayer> playersWithVivecraft = new HashMap<>();

    @Override
    public Map<UUID, ServerVivePlayer> getPlayersWithVivecraft() {
        return this.playersWithVivecraft;
    }
}
