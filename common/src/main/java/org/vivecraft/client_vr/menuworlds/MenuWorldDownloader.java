package org.vivecraft.client_vr.menuworlds;

import net.minecraft.SharedConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.vivecraft.client.utils.Utils.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.common.utils.Utils.logger;

public class MenuWorldDownloader {
	private static final String baseUrl = "https://cache.techjargaming.com/vivecraft/115/";
	public static final String customWorldFolder = "menuworlds/custom_120";

	private static String lastWorld = "";
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
			String localSha1 = getFileChecksum(file, "SHA-1");
			String remoteSha1 = httpReadLine(baseUrl + "checksum.php?file=" + path);
			if (localSha1.equals(remoteSha1)) {
				logger.info("MenuWorlds: SHA-1 matches for {}", path);
				return;
			}
		}
		logger.info("MenuWorlds: Downloading world {}", path);
		httpReadToFile(baseUrl + path, file, true);
	}
	
	public static InputStream getRandomWorld() throws IOException, NoSuchAlgorithmException {
		init();
		try {
			List<MenuWorldItem> worldList = new ArrayList<>();

			if (
				switch(dh.vrSettings.menuWorldSelection)
				{
					case CUSTOM -> worldList.addAll(getCustomWorlds()) || worldList.addAll(getOfficialWorlds());
					case OFFICIAL -> worldList.addAll(getOfficialWorlds());
					case BOTH -> worldList.addAll(getCustomWorlds()) && worldList.addAll(getOfficialWorlds());
					default -> false;
				}
			)
			{
				// don't load the same world twice in a row
				worldList.removeIf(world -> lastWorld.equals(world.path) || lastWorld.equals(world.file.getPath()));
			}

			if (worldList.size() == 0)
				return getRandomWorldFallback();

			MenuWorldItem world = getRandomWorldFromList(worldList);
			if (world != null) {
				lastWorld = world.file != null ? world.file.getPath() : world.path;
			}
			return getStreamForWorld(world);
		} catch (IOException e) {
			e.printStackTrace();
			return getRandomWorldFallback();
		}
	}

	private static InputStream getStreamForWorld(MenuWorldItem world) throws IOException, NoSuchAlgorithmException {
		if (world.file != null) {
			logger.info("MenuWorlds: Using world {}", world.file.getName());
			return new FileInputStream(world.file);
		} else if (world.path != null) {
			downloadWorld(world.path);
			logger.info("MenuWorlds: Using official world {}", world.path);
			return new FileInputStream(world.path);
		} else {
			throw new IllegalArgumentException("File or path must be assigned");
		}
	}

	private static List<MenuWorldItem> getCustomWorlds() throws IOException {
		File dir = new File(customWorldFolder);
		if (dir.exists())
			return getWorldsInDirectory(dir);
		return new ArrayList<>();
	}

	private static List<MenuWorldItem> getOfficialWorlds() throws IOException {
		List<MenuWorldItem> list = new ArrayList<>();
		List<String> resultList = httpReadAllLines(baseUrl + "menuworlds_list.php?minver=" + MenuWorldExporter.MIN_VERSION + "&maxver=" + MenuWorldExporter.VERSION + "&mcver=" + SharedConstants.VERSION_STRING);
		for (String str : resultList)
			list.add(new MenuWorldItem("menuworlds/" + str, null));
		return list;
	}
	
	private static InputStream getRandomWorldFallback() throws IOException, NoSuchAlgorithmException {
		logger.info("MenuWorlds: Couldn't find a world, trying random file from directory");
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
		for (File file : dir.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".mmw"))) {
			int version = MenuWorldExporter.readVersion(file);
			if (version >= MenuWorldExporter.MIN_VERSION && version <= MenuWorldExporter.VERSION)
				worlds.add(new MenuWorldItem(null, file));
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
