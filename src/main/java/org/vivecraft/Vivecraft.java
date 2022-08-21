package org.vivecraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;

public class Vivecraft implements ModInitializer {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
    }
}
