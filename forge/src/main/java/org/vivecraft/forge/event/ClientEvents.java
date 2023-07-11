package org.vivecraft.forge.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(TelescopeTracker.scopeModel);
        ForgeModelBakery.addSpecialModel(ClientDataHolderVR.thirdPersonCameraModel);
        ForgeModelBakery.addSpecialModel(ClientDataHolderVR.thirdPersonCameraDisplayModel);
        ForgeModelBakery.addSpecialModel(CameraTracker.cameraModel);
        ForgeModelBakery.addSpecialModel(CameraTracker.cameraDisplayModel);
    }
}
