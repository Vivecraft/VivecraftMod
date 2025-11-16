package org.vivecraft.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.vivecraft.client.gui.pip.GuiFBTPlayerRenderer;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.forge.Vivecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Vivecraft.MODID)
public class ClientEvents {
    public static void registerConfigScreen(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new VivecraftMainSettings(screen)));
    }

    @SubscribeEvent
    public static void registerReloadEvent(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
        registerClientReloadListenersEvent.registerReloadListener(new ReloadListener());
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent registerClientCommandsEvent) {
        registerClientCommandsEvent.getDispatcher()
            .register(Commands.literal("vivecraft-client-config").executes(context -> {
                Minecraft mc = Minecraft.getInstance();
                mc.schedule(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                return 1;
            }));
    }

    @SubscribeEvent
    public static void registerPiPRenderers(RegisterPictureInPictureRendererEvent event) {
        event.register(new GuiFBTPlayerRenderer(event.getBufferSource()));
    }
}
