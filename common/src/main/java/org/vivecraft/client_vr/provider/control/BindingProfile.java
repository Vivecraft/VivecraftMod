package org.vivecraft.client_vr.provider.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vivecraft.client.utils.FileUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public record BindingProfile(
    String name,
    String description,
    String category,
    ControllerPaths controller_paths,
    Map<String, ActionSet> sets
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static BindingProfile getCurrentProfile() throws FileNotFoundException {
        String currentProfile = ClientDataHolderVR.getInstance().vrSettings.currentBindingProfile;
        File profileFile = new File("input/profiles/" + currentProfile.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + ".json");
        if (profileFile.exists()) return GSON.fromJson(new FileReader(profileFile), BindingProfile.class);
        return null;
    }

    public static List<BindingProfile> getAllProfiles() {
        File profilesDir = new File("input/profiles/");
        if (!profilesDir.exists() || !profilesDir.isDirectory()) return List.of();

        File[] profileFiles = profilesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profileFiles == null) return List.of();

        List<BindingProfile> profiles = new java.util.ArrayList<>();
        for (File profileFile : profileFiles) {
            try (FileReader reader = new FileReader(profileFile)) {
                BindingProfile profile = GSON.fromJson(reader, BindingProfile.class);
                if (profile != null) profiles.add(profile);
            } catch (IOException e) {
                VRSettings.LOGGER.error("Failed to load binding profile from {}: {}", profileFile.getName(),
                    e.getMessage());
            }
        }

        return profiles;
    }

    public boolean saveProfile() {
        File profileFile = new File("input/profiles/" + name.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + ".json");
        if (!profileFile.exists()) profileFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(profileFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            VRSettings.LOGGER.error("Failed to save binding profile: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public static HashSet<String> supportedHeadsets() {
        HashSet<String> set = new HashSet<>();
        if (MCOpenXR.get().session.getCapabilities().XR_HTC_vive_cosmos_controller_interaction) {
            set.add("/interaction_profiles/htc/vive_cosmos_controller");
        }

        if (MCOpenXR.get().session.getCapabilities().XR_BD_controller_interaction) {
            set.add("/interaction_profiles/bytedance/pico4_controller");
            set.add("/interaction_profiles/bytedance/pico_neo3_controller");
        }

        set.add("/interaction_profiles/khr/simple_controller");
        set.add("/interaction_profiles/oculus/touch_controller");
        set.add("/interaction_profiles/htc/vive_controller");
        set.add("/interaction_profiles/valve/index_controller");
        set.add("/interaction_profiles/microsoft/motion_controller");
        return set;
    }

    public static BindingProfile getDefaultBinding(String Headset) throws FileNotFoundException {
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/cosmos_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/vive_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/valve/index_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/knuckles_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/oculus/touch_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/oculus_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/bytedance/pico4_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/pico4_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/bytedance/pico_neo3_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/pico_neo3_defaults.json", true), BindingProfile.class);
            }
            case "/interaction_profiles/microsoft/motion_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/wmr_defaults.json", true), BindingProfile.class);
            }
            default -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/simple_defaults.json", true), BindingProfile.class);
            }
        }
    }
}
