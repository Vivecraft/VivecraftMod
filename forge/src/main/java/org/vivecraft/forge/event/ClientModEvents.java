package org.vivecraft.forge.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.forge.Vivecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(TelescopeTracker.SCOPE_MODEL);
        ForgeModelBakery.addSpecialModel(ClimbTracker.CLAWS_MODEL);
        ForgeModelBakery.addSpecialModel(ClientDataHolderVR.THIRD_PERSON_CAMERA_MODEL);
        ForgeModelBakery.addSpecialModel(ClientDataHolderVR.THIRD_PERSON_CAMERA_DISPLAY_MODEL);
        ForgeModelBakery.addSpecialModel(CameraTracker.CAMERA_MODEL);
        ForgeModelBakery.addSpecialModel(CameraTracker.CAMERA_DISPLAY_MODEL);
    }

    @SubscribeEvent
    public static void registerConfigScreen(FMLConstructModEvent constructModEvent) {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
            () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new VivecraftMainSettings(screen)));
    }

    @SubscribeEvent
    public static void registerReloadEvent(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
        registerClientReloadListenersEvent.registerReloadListener(new ReloadListener());
    }
}
