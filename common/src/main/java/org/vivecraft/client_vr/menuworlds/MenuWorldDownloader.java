package org.vivecraft.client_vr.menuworlds;

import net.minecraft.SharedConstants;
import org.vivecraft.client.utils.FileUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuWorldDownloader {
    private static final String baseUrl = "https://cache.techjargaming.com/vivecraft/115/";
    public static final String customWorldFolder = "menuworlds/custom_120";

    private static String lastWorld = "";
    private static boolean init;
    private static Random rand;

    public static void init() {
        if (init) {
            return;
        }
        rand = new Random();
        rand.nextInt();
        init = true;
    }

    public static void downloadWorld(String path) throws IOException, NoSuchAlgorithmException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            String localSha1 = FileUtils.getFileChecksum(file, "SHA-1");
            String remoteSha1 = FileUtils.httpReadLine(baseUrl + "checksum.php?file=" + path);
            if (localSha1.equals(remoteSha1)) {
                VRSettings.logger.info("Vivecraft: MenuWorlds: SHA-1 matches for {}", path);
                return;
            }
        }
        VRSettings.logger.info("Vivecraft: MenuWorlds: Downloading world {}", path);
        FileUtils.httpReadToFile(baseUrl + path, file);
    }

    public static InputStream getRandomWorld() {
        init();
        VRSettings settings = ClientDataHolderVR.getInstance().vrSettings;

        try {
            List<MenuWorldItem> worldList = new ArrayList<>();
            if (settings.menuWorldSelection == VRSettings.MenuWorld.BOTH || settings.menuWorldSelection == VRSettings.MenuWorld.CUSTOM) {
                worldList.addAll(getCustomWorlds());
            }
            if (settings.menuWorldSelection == VRSettings.MenuWorld.BOTH || settings.menuWorldSelection == VRSettings.MenuWorld.OFFICIAL || worldList.isEmpty()) {
                worldList.addAll(getOfficialWorlds());
            }

            // don't load the same world twice in a row
            if (worldList.size() > 1) {
                worldList.removeIf(world -> lastWorld.equals(world.path) || (world.file != null && lastWorld.equals(world.file.getPath())));
            }

            if (worldList.isEmpty()) {
                return getRandomWorldFallback();
            }

            MenuWorldItem world = getRandomWorldFromList(worldList);
            if (world != null) {
                lastWorld = world.file != null ? world.file.getPath() : world.path;
            }
            return getStreamForWorld(world);
        } catch (IOException | UncheckedIOException | NoSuchAlgorithmException e) {
            VRSettings.logger.error("Vivecraft: error getting random menuworld:", e);
            try {
                return getRandomWorldFallback();
            } catch (IOException | NoSuchAlgorithmException e2) {
                VRSettings.logger.error("Vivecraft: error getting random menuworld fallback:", e);
                return null;
            }
        }
    }

    private static InputStream getStreamForWorld(MenuWorldItem world) throws IOException, NoSuchAlgorithmException {
        if (world.file != null) {
            VRSettings.logger.info("Vivecraft: MenuWorlds: Using world {}", world.file.getName());
            return new FileInputStream(world.file);
        } else if (world.path != null) {
            downloadWorld(world.path);
            VRSettings.logger.info("Vivecraft: MenuWorlds: Using official world {}", world.path);
            return new FileInputStream(world.path);
        } else {
            throw new IllegalArgumentException("File or path must be assigned");
        }
    }

    private static List<MenuWorldItem> getCustomWorlds() throws IOException {
        File dir = new File(customWorldFolder);
        if (dir.exists()) {
            return getWorldsInDirectory(dir);
        }
        return new ArrayList<>();
    }

    private static List<MenuWorldItem> getOfficialWorlds() throws IOException, UncheckedIOException {
        List<MenuWorldItem> list = new ArrayList<>();
        List<String> resultList = FileUtils.httpReadAllLines(baseUrl + "menuworlds_list.php?minver=" + MenuWorldExporter.MIN_VERSION + "&maxver=" + MenuWorldExporter.VERSION + "&mcver=" + SharedConstants.VERSION_STRING);
        for (String str : resultList) {
            list.add(new MenuWorldItem("menuworlds/" + str, null));
        }
        return list;
    }

    private static InputStream getRandomWorldFallback() throws IOException, NoSuchAlgorithmException {
        VRSettings.logger.info("Vivecraft: MenuWorlds: Couldn't find a world, trying random file from directory");
        File dir = new File("menuworlds");
        if (dir.exists()) {
            MenuWorldItem world = getRandomWorldFromList(getWorldsInDirectory(dir));
            if (world != null) {
                return getStreamForWorld(world);
            }
        }
        return null;
    }

    private static List<MenuWorldItem> getWorldsInDirectory(File dir) throws IOException {
        List<MenuWorldItem> worlds = new ArrayList<>();
        for (File file : dir.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".mmw"))) {
            int version = MenuWorldExporter.readVersion(file);
            if (version >= MenuWorldExporter.MIN_VERSION && version <= MenuWorldExporter.VERSION) {
                worlds.add(new MenuWorldItem(null, file));
            }
        }
        return worlds;
    }

    private static MenuWorldItem getRandomWorldFromList(List<MenuWorldItem> list) {
        if (!list.isEmpty()) {
            return list.get(rand.nextInt(list.size()));
        }
        return null;
    }

    private static class MenuWorldItem {
        final File file;
        final String path;

        public MenuWorldItem(String path, File file) {
            this.file = file;
            this.path = path;
        }
    }
}
