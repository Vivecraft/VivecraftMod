package org.vivecraft.menuworlds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.Utils;

import net.minecraft.client.Minecraft;
import net.optifine.Config;

public class MenuWorldDownloader {
    private static final String baseUrl = "https://cache.techjargaming.com/vivecraft/115/";
    private static boolean init;
    private static Random rand;

    public static void init() {
        if (init) return;
        rand = new Random();
        rand.nextInt();
        init = true;
    }

    public static void downloadWorld(String path) throws IOException, NoSuchAlgorithmException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            String localSha1 = Utils.getFileChecksum(file, "SHA-1");
            String remoteSha1 = Utils.httpReadLine(baseUrl + "checksum.php?file=" + path);
            if (localSha1.equals(remoteSha1)) {
                System.out.println("SHA-1 matches for " + path);
                return;
            }
        }
        System.out.println("Downloading world " + path);
        Utils.httpReadToFile(baseUrl + path, file, true);
    }

    public static InputStream getRandomWorld() throws IOException, NoSuchAlgorithmException {
        init();
        VRSettings settings = Minecraft.getInstance().vrSettings;

        try {
            List<MenuWorldItem> worldList = new ArrayList<>();
            if (settings.menuWorldSelection == VRSettings.MenuWorld.BOTH || settings.menuWorldSelection == VRSettings.MenuWorld.CUSTOM)
                worldList.addAll(getCustomWorlds());
            if (settings.menuWorldSelection == VRSettings.MenuWorld.BOTH || settings.menuWorldSelection == VRSettings.MenuWorld.OFFICIAL || worldList.size() == 0)
                worldList.addAll(getOfficialWorlds());

            if (worldList.size() == 0)
                return getRandomWorldFallback();

            MenuWorldItem world = getRandomWorldFromList(worldList);
            return getStreamForWorld(world);
        } catch (IOException e) {
            e.printStackTrace();
            return getRandomWorldFallback();
        }
    }

    private static InputStream getStreamForWorld(MenuWorldItem world) throws IOException, NoSuchAlgorithmException {
        if (world.file != null) {
            System.out.println("Using world " + world.file.getName());
            return new FileInputStream(world.file);
        } else if (world.path != null) {
            downloadWorld(world.path);
            System.out.println("Using official world " + world.path);
            return new FileInputStream(world.path);
        } else {
            throw new IllegalArgumentException("File or path must be assigned");
        }
    }

    private static List<MenuWorldItem> getCustomWorlds() throws IOException {
        File dir = new File("menuworlds/custom_114");
        if (dir.exists())
            return getWorldsInDirectory(dir);
        return new ArrayList<>();
    }

    private static List<MenuWorldItem> getOfficialWorlds() throws IOException {
        List<MenuWorldItem> list = new ArrayList<>();
        List<String> resultList = Utils.httpReadAllLines(baseUrl + "menuworlds_list.php?minver=" + MenuWorldExporter.MIN_VERSION + "&maxver=" + MenuWorldExporter.VERSION + "&mcver=" + Config.MC_VERSION);
        for (String str : resultList)
            list.add(new MenuWorldItem("menuworlds/" + str, null));
        return list;
    }

    private static InputStream getRandomWorldFallback() throws IOException, NoSuchAlgorithmException {
        System.out.println("Couldn't find a world, trying random file from directory");
        File dir = new File("menuworlds");
        if (dir.exists()) {
            MenuWorldItem world = getRandomWorldFromList(getWorldsInDirectory(dir));
            if (world != null)
                return getStreamForWorld(world);
        }
        return null;
    }

    private static List<MenuWorldItem> getWorldsInDirectory(File dir) throws IOException {
        List<MenuWorldItem> worlds = new ArrayList<>();
        List<File> files = Arrays.asList(dir.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".mmw")));
        if (files.size() > 0) {
            for (File file : files) {
                int version = MenuWorldExporter.readVersion(file);
                if (version >= MenuWorldExporter.MIN_VERSION && version <= MenuWorldExporter.VERSION)
                    worlds.add(new MenuWorldItem(null, file));
            }
        }
        return worlds;
    }

    private static MenuWorldItem getRandomWorldFromList(List<MenuWorldItem> list) {
        if (list.size() > 0)
            return list.get(rand.nextInt(list.size()));
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
