package org.vivecraft.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vivecraft.titleworlds.Screenshot3D;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    static void registerCommand(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("titleworlds:3Dscreenshot");
        RequiredArgumentBuilder<CommandSourceStack, String> argument = RequiredArgumentBuilder.argument("name", StringArgumentType.string());
        builder.executes(ctx -> {
            String name = Screenshot3D.saveTitleworld((ClientLevel) ctx.getSource().getUnsidedLevel(), null);
            ctx.getSource().sendSuccess(Component.literal("Saved 3D screenshot as " + name), false);
            return 1;
        }).then(argument
                .executes(ctx -> {
                    String name = Screenshot3D.saveTitleworld((ClientLevel) ctx.getSource().getUnsidedLevel(), StringArgumentType.getString(ctx, "name"));
                    ctx.getSource().sendSuccess(Component.literal("Saved 3D screenshot as " + name), false);
                    return 1;
                })
        );

        event.getDispatcher().register(builder);
    }
}
