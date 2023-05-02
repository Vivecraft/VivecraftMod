package org.vivecraft.mixin.server;

import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net/minecraft/server/level/ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {

    @Accessor("seenBy")
    Set<ServerPlayerConnection> getPlayersTracking();
}
