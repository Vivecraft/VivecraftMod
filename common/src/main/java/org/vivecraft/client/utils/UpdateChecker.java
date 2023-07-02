package org.vivecraft.client.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import org.vivecraft.client.Xplat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class UpdateChecker {

    public static boolean hasUpdate = false;

    public static String changelog = "";

    public static String newestVersion = "";

    public static void checkForUpdates() {
        System.out.println("Checking for Vivecraft Updates");
        try {
            String apiURL = "https://api.modrinth.com/v2/project/vivecraft/version?loaders=[%22" +  Xplat.getModloader() + "%22]&game_versions=[%22" + SharedConstants.VERSION_STRING + "%22]";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            // 10 seconds read and connect timeout
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "application/json,*/*");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LogUtils.getLogger().error("Error " + conn.getResponseCode() + " fetching Vivecraft updates");
                return;
            }

            JsonElement j = JsonParser.parseString(inputStreamToString(conn.getInputStream()));

            List<Version> versions = new LinkedList<>();

            if (j.isJsonArray()) {
                for(JsonElement element : j.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        versions.add(
                                new Version(obj.get("name").getAsString(),
                                        obj.get("version_number").getAsString(),
                                        obj.get("changelog").getAsString()));
                    }
                }
            }
            // sort the versions, modrinth doesn't guarantee them to be sorted.
            Collections.sort(versions);

            String currentVersionNumber = Xplat.getModVersion() + "-" +Xplat.getModloader();
            Version current = new Version(currentVersionNumber,currentVersionNumber,"");

            for (Version v : versions) {
                if (current.compareTo(v) > 0) {
                    changelog += "§a"+v.fullVersion+"§r" + ": \n" + v.changelog + "\n\n";
                    if (newestVersion.isEmpty()) {
                        newestVersion = v.fullVersion;
                    }
                    hasUpdate = true;
                }
            }
            // no carriage returns please
            changelog = changelog.replaceAll("\\r", "");
            if (hasUpdate) {
                LogUtils.getLogger().info("Vivecraft update found: " + newestVersion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    private static class Version implements Comparable<Version>{

        public String fullVersion;

        public String changelog;

        public int major;
        public int minor;
        public int patch;
        int alpha = 0;
        int beta = 0;
        boolean featureTest = false;

        public Version(String version, String version_number, String changelog) {
            this.fullVersion = version;
            this.changelog = changelog;
            String[] parts = version_number.split("-");
            if (parts.length > 3) {
                // prerelease
                if (parts[2].matches("a\\d+")) {
                    alpha = Integer.parseInt(parts[2].replaceAll("\\D+",""));
                } else if (parts[2].matches("b\\d+\"")) {
                    beta = Integer.parseInt(parts[2].replaceAll("\\D+", ""));
                } else {
                    featureTest = true;
                }
            }
            // account for old version, without MC version prefix
            int index = parts.length > 1 ? 1 : 0;
            String[] ints = parts[index].split("\\.");
            // remove all letters, since stupid me put a letter in one version
            major = Integer.parseInt(ints[0].replaceAll("\\D+", ""));
            minor = Integer.parseInt(ints[1].replaceAll("\\D+", ""));
            patch = Integer.parseInt(ints[2].replaceAll("\\D+", ""));
        }

        @Override
        public int compareTo(UpdateChecker.Version o) {
            long result = this.compareNumber() - o.compareNumber();
            if (result < 0) {
                return 1;
            } else if (result == 0L) {
                return 0;
            }
            return -1;
        }

        // two digits per segment, should be enough right?
        private long compareNumber() {
            return alpha + beta * 100L + (alpha + beta == 0 || featureTest ? 1000L : 0L) + patch * 100000L + minor * 10000000L + major * 1000000000L;
        }
    }

}
