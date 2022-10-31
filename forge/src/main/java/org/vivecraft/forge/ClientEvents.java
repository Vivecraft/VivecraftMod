package org.vivecraft.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.vivecraft.titleworlds.Screenshot3D;
import org.vivecraft.titleworlds.TitleWorldsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = TitleWorldsMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    static void registerCommand(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("titleworlds:3Dscreenshot");
        RequiredArgumentBuilder<CommandSourceStack, String> argument = RequiredArgumentBuilder.argument("name", StringArgumentType.string());
        builder.executes(ctx -> {
            String name = Screenshot3D.take3DScreenshot((ClientLevel) ctx.getSource().getUnsidedLevel(), null);
            ctx.getSource().sendSuccess(new TextComponent("Saved 3D screenshot as " + name), false);
            return 1;
        }).then(argument
                .executes(ctx -> {
                    String name = Screenshot3D.take3DScreenshot((ClientLevel) ctx.getSource().getUnsidedLevel(), StringArgumentType.getString(ctx, "name"));
                    ctx.getSource().sendSuccess(new TextComponent("Saved 3D screenshot as " + name), false);
                    return 1;
                })
        );

        event.getDispatcher().register(builder);
    }
}
