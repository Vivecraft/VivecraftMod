package org.vivecraft.forge.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(TelescopeTracker.scopeModel);
        event.register(ClientDataHolder.thirdPersonCameraModel);
        event.register(ClientDataHolder.thirdPersonCameraDisplayModel);
        event.register(CameraTracker.cameraModel);
        event.register(CameraTracker.cameraDisplayModel);
    }
}
