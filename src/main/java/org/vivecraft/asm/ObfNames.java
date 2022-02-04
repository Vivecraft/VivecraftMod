package org.vivecraft.asm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObfNames {
	public static boolean DEBUG = false;
	private static final Pattern descPattern = Pattern.compile("L(.+?);");
	private static final Map<String, String> classMappings = new HashMap<>();
	private static final Map<String, String> fieldMappings = new HashMap<>();
	private static final Map<String, String> methodMappings = new HashMap<>();
	private static final Map<String, String> devMappings = new HashMap<>();

	static {
		String prop = System.getProperty("vivecraft.mcpconfdir");
		File mcpDir = new File(prop == null ? "../conf" : prop);
		boolean inDev = new File(mcpDir, "joined.srg").exists();

		if (!inDev) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(ObfNames.class.getResourceAsStream("/mappings/vivecraft/joined.srg")))) {
				System.out.println("Loading obf mappings...");
				br.lines().forEach(line -> {
					String[] split = line.split(": ");
					String[] values = split[1].split(" ");
					switch (split[0]) {
						case "CL":
							classMappings.put(values[1], values[0]);
							break;
						case "FD":
							fieldMappings.put(values[1].substring(values[1].lastIndexOf('/') + 1), values[0].substring(values[0].lastIndexOf('/') + 1));
							break;
						case "MD":
							methodMappings.put(values[2].substring(values[2].lastIndexOf('/') + 1), values[0].substring(values[0].lastIndexOf('/') + 1));
							break;
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			System.out.println("MCP conf found! Loading dev mappings...");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mcpDir, "fields.csv"))))) {
				br.lines().forEach(line -> {
					String[] split = line.split(",");
					if (!split[0].equals("searge")) // Ignore header
						devMappings.put(split[0], split[1]);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mcpDir, "methods.csv"))))) {
				br.lines().forEach(line -> {
					String[] split = line.split(",");
					if (!split[0].equals("searge")) // Ignore header
						devMappings.put(split[0], split[1]);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returned class path is separated by slashes, not dots, if mapping is found.
	 */
	public static String resolveClass(String name, boolean obfuscated) {
		if (obfuscated) {
			String canon = name.replace('.', '/');
			if (classMappings.containsKey(canon))
				return classMappings.get(canon);
			else if (DEBUG)
				System.out.println("No obf mapping found for " + name);
		}
		return name;
	}

	public static String resolveField(String name, boolean obfuscated) {
		if (obfuscated) {
			if (fieldMappings.containsKey(name))
				return fieldMappings.get(name);
			else if (DEBUG)
				System.out.println("No obf mapping found for " + name);
		}
		return name;
	}

	public static String resolveMethod(String name, boolean obfuscated) {
		if (obfuscated) {
			if (methodMappings.containsKey(name))
				return methodMappings.get(name);
			else if (DEBUG)
				System.out.println("No obf mapping found for " + name);
		}
		return name;
	}

	public static String resolveDescriptor(String desc, boolean obfuscated) {
		if (obfuscated) {
			Matcher m = descPattern.matcher(desc);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(sb, "L" + ObfNames.resolveClass(m.group(1), true) + ";");
			}
			m.appendTail(sb);
			return sb.toString();
		}
		return desc;
	}

	public static String getDevMapping(String name) {
		if (devMappings.containsKey(name))
			return devMappings.get(name);
		else if (DEBUG)
			System.out.println("No dev mapping found for " + name);
		return name;
	}
}