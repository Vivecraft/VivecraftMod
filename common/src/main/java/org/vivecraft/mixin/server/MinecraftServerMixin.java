package org.vivecraft.mixin.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.server.MinecraftServerExt;
import org.vivecraft.server.ServerVivePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerExt {

    @Unique
    Map<UUID, ServerVivePlayer> playersWithVivecraft = new HashMap<>();

    @Override
    public Map<UUID, ServerVivePlayer> getPlayersWithVivecraft() {
        return this.playersWithVivecraft;
    }
}
