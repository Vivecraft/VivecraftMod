package org.vivecraft.titleworlds;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class TitleWorldsMod {

    public static final String MODID = "titleworlds";
    public static final Logger LOGGER = LogManager.getLogger("Title World");

    public static State state = new State();

    public static LevelStorageSource levelSource;

    public static void onInitializeClient() {
        LOGGER.info("Opening level storage source");
        Minecraft minecraft = Minecraft.getInstance();
        Path titleWorldsPath = minecraft.gameDirectory.toPath().resolve("titleworlds");
        levelSource = new LevelStorageSource(titleWorldsPath, minecraft.gameDirectory.toPath().resolve("titleworldbackups"), minecraft.getFixerUpper());
    }

    public static class State {
        public boolean isTitleWorld = false;

        public boolean pause = false;
        public boolean noSave = true;
    }
}
