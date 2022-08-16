package com.example.vivecraftfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;

public class VivecraftFabric implements ModInitializer {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> {
            out.accept(TelescopeTracker.scopeModel);
            out.accept(DataHolder.thirdPersonCameraModel);
            out.accept(DataHolder.thirdPersonCameraDisplayModel);
            out.accept(CameraTracker.cameraModel);
            out.accept(CameraTracker.cameraDisplayModel);
        }));
    }
}
