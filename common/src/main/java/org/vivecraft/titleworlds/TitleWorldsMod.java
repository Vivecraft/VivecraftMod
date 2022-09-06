package org.vivecraft.titleworlds;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public class TitleWorldsMod {
    public static final Logger LOGGER = LogManager.getLogger("Title World");

    private static KeyMapping keyBinding;

    public static State state = new State();

    public static LevelStorageSource levelSource;

    public static void onInitializeClient() {
        LOGGER.info("Opening level storage source");
        Minecraft minecraft = Minecraft.getInstance();
        Path titleWorldsPath = minecraft.gameDirectory.toPath().resolve("titleworlds");
        levelSource = new LevelStorageSource(titleWorldsPath, minecraft.gameDirectory.toPath().resolve("titleworldbackups"), minecraft.getFixerUpper());

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

    public static class State {
        public boolean isTitleWorld = false;

        public boolean pause = false;
        public boolean noSave = true;
    }
}
