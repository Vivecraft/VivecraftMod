package org.vivecraft.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.vivecraft.titleworlds.TitleWorldsMod;

@Mod(TitleWorldsMod.MODID)
public class TitleWorlds {

    public TitleWorlds() {

    }

    @Mod.EventBusSubscriber(modid = TitleWorldsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        //@SubscribeEvent
        static void clientSetup(FMLConstructModEvent event) {
            TitleWorldsMod.onInitializeClient();
        }
    }
}
