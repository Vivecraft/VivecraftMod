package org.vivecraft.client.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.utils.Utils;
import org.vivecraft.server.config.ServerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateChecker {

    public static boolean hasUpdate = false;

    public static String changelog = "";

    public static String newestVersion = "";

    public static boolean checkForUpdates() {
        System.out.println("Checking for Vivecraft Updates");

        char updateType;
        if (Xplat.isDedicatedServer()) {
            // server
            updateType = ServerConfig.checkForUpdateType.get().charAt(0);
        } else {
            // client
            updateType = switch (ClientDataHolderVR.getInstance().vrSettings.updateType) {
                case RELEASE -> 'r';
                case BETA -> 'b';
                case ALPHA -> 'a';
            };
        }

        try {
            String apiURL = "https://api.modrinth.com/v2/project/vivecraft/version?loaders=[%22" + Xplat.getModloader() + "%22]&game_versions=[%22" + SharedConstants.VERSION_STRING + "%22]";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            // 10 seconds read and connect timeout
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "application/json,*/*");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Utils.logger.error("Error " + conn.getResponseCode() + " fetching Vivecraft updates");
                return false;
            }

            JsonElement j = JsonParser.parseString(inputStreamToString(conn.getInputStream()));

            List<Version> versions = new LinkedList<>();

            if (j.isJsonArray()) {
                for (JsonElement element : j.getAsJsonArray()) {
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

            String currentVersionNumber = Xplat.getModVersion() + "-" + Xplat.getModloader();
            Version current = new Version(currentVersionNumber, currentVersionNumber, "");

            for (Version v : versions) {
                if (v.isVersionType(updateType) && current.compareTo(v) > 0) {
                    changelog += "§a" + v.fullVersion + "§r" + ": \n" + v.changelog + "\n\n";
                    if (newestVersion.isEmpty()) {
                        newestVersion = v.fullVersion;
                    }
                    hasUpdate = true;
                }
            }
            // no carriage returns please
            changelog = changelog.replaceAll("\\r", "");
            if (hasUpdate) {
                Utils.logger.info("Vivecraft update found: " + newestVersion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasUpdate;
    }

    private static String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.joining("\n"));
    }

    private static class Version implements Comparable<Version> {

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
            int viveVersionIndex = parts.length - 2;
            // parts should be [mc version]-(pre/rc)-[vive version]-(vive a/b/test)-[mod loader]
            if (!parts[viveVersionIndex].contains(".")) {
                viveVersionIndex = parts.length - 3;
                // prerelease
                if (parts[parts.length - 1].matches("a\\d+")) {
                    alpha = Integer.parseInt(parts[parts.length - 1].replaceAll("\\D+", ""));
                } else if (parts[parts.length - 1].matches("b\\d+")) {
                    beta = Integer.parseInt(parts[parts.length - 1].replaceAll("\\D+", ""));
                } else {
                    featureTest = true;
                }
            }
            String[] ints = parts[viveVersionIndex].split("\\.");
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

        public boolean isVersionType(char versionType) {
            return switch (versionType) {
                case 'r' -> beta == 0 && alpha == 0 && !featureTest;
                case 'b' -> beta >= 0 && alpha == 0 && !featureTest;
                case 'a' -> alpha >= 0 && !featureTest;
                default -> false;
            };
        }

        // two digits per segment, should be enough right?
        private long compareNumber() {
            return alpha + beta * 100L + (alpha + beta == 0 || featureTest ? 1000L : 0L) + patch * 100000L + minor * 10000000L + major * 1000000000L;
        }
    }
}
