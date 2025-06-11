package org.vivecraft.client_vr.extensions;

public interface MinecraftExtension {

    /**
     * returns the partialTick when running and the pausePartialTick when paused
     */
    float vivecraft$getPartialTick();
}
