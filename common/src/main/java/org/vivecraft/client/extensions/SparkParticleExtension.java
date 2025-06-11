package org.vivecraft.client.extensions;

import java.util.UUID;

public interface SparkParticleExtension {
    /**
     * sets the player uuid that this particle was created for
     *
     * @param playerUUID UUID of the owning player
     */
    void vivecraft$setPlayerUUID(UUID playerUUID);
}
