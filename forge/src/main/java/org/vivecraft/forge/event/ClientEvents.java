package org.vivecraft.forge.event;

import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static org.vivecraft.client_vr.VRState.dh;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(RegisterAdditional event) {
        event.register(TelescopeTracker.scopeModel);
        event.register(dh.thirdPersonCameraModel);
        event.register(dh.thirdPersonCameraDisplayModel);
        event.register(CameraTracker.cameraModel);
        event.register(CameraTracker.cameraDisplayModel);
    }
}
