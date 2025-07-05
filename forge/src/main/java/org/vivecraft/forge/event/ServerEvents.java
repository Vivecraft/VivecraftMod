package org.vivecraft.forge.event;

import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.Xloader;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.config.ServerConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    @SubscribeEvent
    public static void crashWithForgeExtension(ServerAboutToStartEvent event) {
        if (Xloader.isModLoaded("vivecraftforgeextensions")) {
            throw new RuntimeException(
                "The vivecraft mod cannot be used together with the 'Vivecraft Forge Extension'.\nThe Vivecraft Mod implements all features the forge extension has.\nRemove the 'Vivecraft Forge Extension' to resolve this error");
        }
    }

    @SubscribeEvent
    public static void loadServerConfig(ServerStartingEvent serverStartingEvent) {
        // on the server reinit the ServerConfig here again, after the lang files got loaded, to have comments
        // some forge versions don't load the lang files correctly on the server, so manually load ours
        if (!Language.getInstance().has("vivecraft.serverSettings.general")) {
            // copied from neoforge
            String langFile = String.format(Locale.ROOT, "lang/%s.json", "en_us");
            ResourceManager resourceManager = serverStartingEvent.getServer().getServerResources().resourceManager();
            // cannot close this as it would close all packs
            ResourceManager clientResources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                resourceManager.listPacks().toList());
            Map<String, String> langMap = new HashMap<>();
            try {
                ResourceLocation langResource = ResourceLocation.fromNamespaceAndPath("vivecraft", langFile);
                for (Resource resource : clientResources.getResourceStack(langResource)) {
                    try (InputStream stream = resource.open()) {
                        Language.loadFromJson(stream, langMap::put);
                    }
                }
                Language.getInstance().getLanguageData().putAll(langMap);
                ForgeI18n.loadLanguageData(Language.getInstance().getLanguageData());
            } catch (Exception exception) {
                ServerNetworking.LOGGER.error("Vivecraft: failed to load vivecraft lang file", exception);
            }
        }
        ServerConfig.init(null);
    }
}
