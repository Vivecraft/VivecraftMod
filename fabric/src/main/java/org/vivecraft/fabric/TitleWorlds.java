package org.vivecraft.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import org.vivecraft.titleworlds.Screenshot3D;
import org.vivecraft.titleworlds.TitleWorldsMod;

public class TitleWorlds implements ClientModInitializer {

    public void onInitializeClient() {
        TitleWorldsMod.onInitializeClient();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("titleworlds:3Dscreenshot")
                .executes(ctx -> {
                    String name = Screenshot3D.saveTitleworld(ctx.getSource().getWorld(), null);
                    ctx.getSource().sendFeedback(Component.literal("Saved 3D screenshot as " + name));
                    return 1;
                }).then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .executes(ctx -> {
                        String name = Screenshot3D.saveTitleworld(ctx.getSource().getWorld(), StringArgumentType.getString(ctx, "name"));
                        ctx.getSource().sendFeedback(Component.literal("Saved 3D screenshot as " + name));
                        return 1;
                    })));
        });
    }
}
