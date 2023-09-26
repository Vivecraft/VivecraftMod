package org.vivecraft.server;

import java.util.Map;
import java.util.UUID;

public interface MinecraftServerExt {
    Map<UUID, ServerVivePlayer> vivecraft$getPlayersWithVivecraft();
}
