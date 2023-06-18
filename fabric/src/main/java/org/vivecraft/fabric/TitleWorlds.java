package org.vivecraft.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.network.chat.TextComponent;
import org.vivecraft.titleworlds.Screenshot3D;
import org.vivecraft.titleworlds.TitleWorldsMod;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public class TitleWorlds implements ClientModInitializer {

    public void onInitializeClient() {
        TitleWorldsMod.onInitializeClient();

        ClientCommandManager.DISPATCHER.register(literal("titleworlds:3Dscreenshot")
                .executes(ctx -> {
                    String name = Screenshot3D.take3DScreenshot(ctx.getSource().getWorld(), null);
                    ctx.getSource().sendFeedback(new TextComponent("Saved 3D screenshot as " + name));
                    return 1;
                }).then(argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            String name = Screenshot3D.take3DScreenshot(ctx.getSource().getWorld(), StringArgumentType.getString(ctx, "name"));
                            ctx.getSource().sendFeedback(new TextComponent("Saved 3D screenshot as " + name));
                            return 1;
                        }))
        );
    }
}
