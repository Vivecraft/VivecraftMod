package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.api.client.Tracker;

/**
 * Allows a Tracker to render visual debug information
 */
public interface DebugRenderTracker extends Tracker {
    /**
     * Renders debug elements to visualize the tracker's state.
     * Only called when the tracker is active and set to render debug elements in the debug settings.
     * <br>
     * This is called every frame, so {@link ProcessType#PER_TICK} trackers should make sure that stuff is not {@code null}, since this could be called before {@link Tracker#activeProcess(LocalPlayer)} is called.
     */
    void renderDebug();
}
