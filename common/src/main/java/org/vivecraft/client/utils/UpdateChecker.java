package org.vivecraft.client.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.vivecraft.Xloader;
import org.vivecraft.api.data.ViveVersion;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.server.ServerNetworking;
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

    public static boolean HAS_UPDATE = false;

    public static String CHANGELOG = "";

    public static String NEWEST_VERSION = "";

    public static boolean checkForUpdates() {
        VRSettings.LOGGER.info("Vivecraft: Checking for Updates");

        char updateType;
        if (Xloader.INSTANCE.isDedicatedServer()) {
            // server
            updateType = ServerConfig.CHECK_FOR_UPDATE_TYPE.get().charAt(0);
        } else {
            // client
            updateType = switch (ClientDataHolderVR.getInstance().vrSettings.updateType) {
                case RELEASE -> 'r';
                case BETA -> 'b';
                case ALPHA -> 'a';
            };
        }

        try {
            String apiURL =
                "https://api.modrinth.com/v2/project/vivecraft/version?loaders=[%22" +
                    Xloader.INSTANCE.getModloader().name + "%22]&game_versions=[%22" +
                    ClientUtils.currentMcVersion() + "%22]";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            // 10 seconds read and connect timeout
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "application/json,*/*");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                VRSettings.LOGGER.error("Vivecraft: Error '{}' fetching updates", conn.getResponseCode());
                return false;
            }

            JsonElement j = JsonParser.parseString(inputStreamToString(conn.getInputStream()));

            List<Version> versions = new LinkedList<>();

            if (j.isJsonArray()) {
                for (JsonElement element : j.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        versions.add(
                            Version.fromModrinth(obj.get("name").getAsString(),
                                obj.get("version_number").getAsString(),
                                obj.get("changelog").getAsString()));
                    }
                }
            }
            // sort the versions, modrinth doesn't guarantee them to be sorted.
            Collections.sort(versions);

            String currentVersionNumber = Xloader.INSTANCE.getModVersion() + "-" + Xloader.INSTANCE.getModloader().name;
            Version current = Version.fromModrinth(currentVersionNumber, currentVersionNumber, "");

            // enforce update notifications if using a non release
            if (current.alpha > 0 && updateType != 'a') {
                updateType = 'a';
            } else if (current.beta > 0 && updateType != 'a') {
                updateType = 'b';
            }

            for (Version v : versions) {
                if (v.isVersionType(updateType) && current.compareTo(v) > 0) {
                    CHANGELOG += "§a" + v.fullVersion + "§r" + ": \n" + v.changelog + "\n\n";
                    if (NEWEST_VERSION.isEmpty()) {
                        NEWEST_VERSION = v.fullVersion;
                    }
                    HAS_UPDATE = true;
                }
            }
            // no carriage returns please
            CHANGELOG = CHANGELOG.replaceAll("\\r", "");
            if (HAS_UPDATE) {
                VRSettings.LOGGER.info("Vivecraft update found: {}", NEWEST_VERSION);
            }
        } catch (IOException e) {
            VRSettings.LOGGER.error("Vivecraft: fetching available vivecraft updates: ", e);
        }
        return HAS_UPDATE;
    }

    private static String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.joining("\n"));
    }

    public static class Version implements Comparable<Version>, ViveVersion {

        public final static Version UNKNOWN = new Version();

        public final String fullVersion;

        public final String changelog;

        private int major;
        private int minor;
        private int patch;
        private int alpha = 0;
        private int beta = 0;
        private boolean featureTest = false;

        private boolean unknown = false;

        private Version() {
            this.fullVersion = "Unknown";
            this.changelog = "";
            this.unknown = true;
        }

        private Version(String version, String version_number, String changelog) {
            this.fullVersion = version;
            this.changelog = changelog;
            try {
                String[] parts = version_number.split("-");
                int viveVersionIndex = parts.length - 1;
                // parts should be [mc version]-(pre/rc)-[vive version]-(vive a/b/test)
                if (!parts[viveVersionIndex].contains(".")) {
                    viveVersionIndex = parts.length - 2;
                    String testString = parts[parts.length - 1];
                    // prerelease
                    if (testString.matches("a\\d+.*")) {
                        this.alpha = Integer.parseInt(testString.replaceAll("\\D+", ""));
                    } else if (testString.matches("b\\d+.*")) {
                        this.beta = Integer.parseInt(testString.replaceAll("\\D+", ""));
                    }
                    // if the prerelease string is not just aXX or bXX it's a feature test as well and ranked slightly higher
                    if (!testString.replaceAll("^[ab]\\d+", "").isEmpty()) {
                        this.featureTest = true;
                    }
                }
                String[] ints = parts[viveVersionIndex].split("\\.");
                // remove all letters, since stupid me put a letter in one version
                this.major = Integer.parseInt(ints[0].replaceAll("\\D+", ""));
                this.minor = Integer.parseInt(ints[1].replaceAll("\\D+", ""));
                this.patch = Integer.parseInt(ints[2].replaceAll("\\D+", ""));
            } catch (Exception e) {
                // couldn't parse the version, mark as unknown
                ServerNetworking.LOGGER.warn("Vivecraft: coudln't parse version: {}, Error: ", version, e);
                this.unknown = true;
                this.major = 0;
                this.minor = 0;
                this.patch = 0;
                this.alpha = 0;
                this.beta = 0;
                this.featureTest = false;
            }
        }

        public static Version fromModrinth(String modrinthVersionName, String modrinthVersionNumber, String changelog) {
            // parts should be [mc version]-(pre/rc)-[vive version]-(vive a/b/test)-[mod loader]
            // remove the mod loader
            return new Version(modrinthVersionName,
                modrinthVersionNumber.substring(0, modrinthVersionNumber.lastIndexOf("-")), changelog);
        }

        public static Version fromClient(String clientString) {
            String[] versionParts = clientString.split(" ");
            String vive;
            if (versionParts.length == 2) {
                // 1.0.0+ version scheme
                // versions sent look like "Vivecraft-[mc version]-[mod loader]-[vive version]-(a/b/test) VR/NONVR"
                vive = versionParts[0];
            } else {
                // versions sent look like "Vivecraft [mc version] (jrbudda)-VR/NONVR-[mod loader]-[vive version]"
                // or for the standalone, this is not parsable
                // versions sent look like "Vivecraft [mc version] (jrbudda)-VR/NONVR-[feature]-[releases]"
                vive = versionParts[versionParts.length - 1];
            }
            return new Version(clientString, vive, "");
        }

        public boolean isValid() {
            return !this.unknown;
        }

        @Override
        public int getMajor() {
            return this.major;
        }

        @Override
        public int getMinor() {
            return this.minor;
        }

        @Override
        public int getPatch() {
            return this.patch;
        }

        @Override
        public ReleaseType getReleaseType() {
            if (this.alpha > 0) {
                return ReleaseType.ALPHA;
            } else if (this.beta > 0) {
                return ReleaseType.BETA;
            } else {
                return ReleaseType.RELEASE;
            }
        }

        /**
         * returns 1 if the other version is newer, -1 if the other version is older. 0 if they are equal
         */
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
                case 'r' -> this.beta == 0 && this.alpha == 0 && !this.featureTest;
                case 'b' -> this.beta >= 0 && this.alpha == 0 && !this.featureTest;
                case 'a' -> this.alpha >= 0 && !this.featureTest;
                default -> false;
            };
        }

        // two digits per segment, should be enough right?
        private long compareNumber() {
            // digit flag
            // major minor patch full release beta alpha feature test
            // 00    00    00    0            00   00    0
            return (this.featureTest ? 1L : 0L) +
                this.alpha * 10L +
                this.beta * 1000L +
                (this.alpha + this.beta == 0 ? 10000L : 0L) +
                this.patch * 1000000L +
                this.minor * 100000000L +
                this.major * 10000000000L;
        }

        public String versionString() {
            String version = this.major + "." + this.minor + "." + this.patch;
            if (this.alpha > 0) {
                version += "-a" + this.alpha;
            }
            if (this.beta > 0) {
                version += "-b" + this.beta;
            }
            if (this.featureTest) {
                version += ((this.alpha > 0 || this.beta > 0) ? "_" : "-") + "featuretest";
            }
            return version;
        }

        @Override
        public String toString() {
            if (this.unknown) {
                return this.fullVersion + "(unknown format)";
            }

            return this.fullVersion + "(" + versionString() + ")";
        }
    }
}
