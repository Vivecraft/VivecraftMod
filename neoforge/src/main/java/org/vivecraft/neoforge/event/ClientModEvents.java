package org.vivecraft.neoforge.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.neoforge.Vivecraft;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(TelescopeTracker.SCOPE_MODEL);
        event.register(ClimbTracker.CLAWS_MODEL);
        event.register(ClientDataHolderVR.THIRD_PERSON_CAMERA_MODEL);
        event.register(ClientDataHolderVR.THIRD_PERSON_CAMERA_DISPLAY_MODEL);
        event.register(CameraTracker.CAMERA_MODEL);
        event.register(CameraTracker.CAMERA_DISPLAY_MODEL);
    }

    @SubscribeEvent
    public static void registerConfigScreen(FMLConstructModEvent constructModEvent) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
            () -> (mc, screen) -> new VivecraftMainSettings(screen));
    }

    @SubscribeEvent
    public static void registerReloadEvent(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
        registerClientReloadListenersEvent.registerReloadListener(new ReloadListener());
    }
}
