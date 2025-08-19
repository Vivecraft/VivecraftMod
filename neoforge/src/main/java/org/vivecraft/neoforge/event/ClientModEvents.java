package org.vivecraft.neoforge.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.neoforge.Vivecraft;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerConfigScreen(FMLConstructModEvent constructModEvent) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
            () -> (mc, screen) -> new VivecraftMainSettings(screen));
    }

    @SubscribeEvent
    public static void registerReloadEvent(AddClientReloadListenersEvent addClientReloadListenersEvent) {
        addClientReloadListenersEvent.addListener(
            ResourceLocation.fromNamespaceAndPath("vivecraft", "reloadlistener"),
            new ReloadListener());
    }
}
