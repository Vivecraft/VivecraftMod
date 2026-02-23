package org.vivecraft.client_vr.provider.openxr.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.control.Action;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public record XRBindingProfile(String name, String[] interactionProfiles, boolean custom, List<Action> actions) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static XRBindingProfile getCurrentProfile() throws FileNotFoundException {
        String currentProfile = ClientDataHolderVR.getInstance().vrSettings.currentBindingProfile;
        File profileFile = new File("openxr/input/profiles/" + currentProfile.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + ".json");
        if (profileFile.exists()) return GSON.fromJson(new FileReader(profileFile), XRBindingProfile.class);
        return null;
    }

    public static List<XRBindingProfile> getAllProfiles() {
        File profilesDir = new File("openxr/input/profiles/");
        if (!profilesDir.exists() || !profilesDir.isDirectory()) return List.of();

        File[] profileFiles = profilesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profileFiles == null) return List.of();

        List<XRBindingProfile> profiles = new java.util.ArrayList<>();
        for (File profileFile : profileFiles) {
            try (FileReader reader = new FileReader(profileFile)) {
                XRBindingProfile profile = GSON.fromJson(reader, XRBindingProfile.class);
                if (profile != null) profiles.add(profile);
            } catch (IOException e) {
                System.out.println("Failed to load binding profile from " + profileFile.getName() + ": " + e.getMessage());
            }
        }

        return profiles;
    }

    public boolean saveProfile() {
        File profileFile = new File("openxr/input/profiles/" + name.replaceAll("[^a-zA-Z0-9-_\\.]", "_") + ".json");
        if (!profileFile.exists()) profileFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(profileFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.out.println("Failed to save binding profile: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static String getPrettyName(String buttonName) {
        for (Map.Entry<String, String> entry : BUTTON_PRETTY_NAMES().entrySet()) {
            if (buttonName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return buttonName;
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
        return set;
    }

    public static XRBindingProfile getDefaultBinding(String Headset) throws FileNotFoundException {
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/cosmos_defaults.json"), XRBindingProfile.class);
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/vive_defaults.json"), XRBindingProfile.class);
            }
            case "/interaction_profiles/valve/index_controller" -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/index_defaults.json"), XRBindingProfile.class);
            }
            case "/interaction_profiles/oculus/touch_controller" -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/oculus_defaults.json"), XRBindingProfile.class);
            }
            case "/interaction_profiles/bytedance/pico4_controller",
                 "/interaction_profiles/bytedance/pico_neo3_controller" -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/pico_defaults.json"), XRBindingProfile.class);
            }
            default -> {
                return GSON.fromJson(new FileReader("openxr/input/profiles/simple_defaults.json"), XRBindingProfile.class);
            }
        }
    }

    public static Map<String, String> BUTTON_PRETTY_NAMES() {
        Map<String, String> names = new HashMap<>();

        // Left Hand
        names.put("/user/hand/left/input/y/click", "Left Y");
        names.put("/user/hand/left/input/y/touch", "Left Y Touch");
        names.put("/user/hand/left/input/x/click", "Left X");
        names.put("/user/hand/left/input/x/touch", "Left X Touch");
        names.put("/user/hand/left/input/menu/click", "Left Menu");
        names.put("/user/hand/left/input/system/click", "Left System");
        names.put("/user/hand/left/input/select/click", "Left Select");
        names.put("/user/hand/left/input/squeeze/click", "Left Grip");
        names.put("/user/hand/left/input/squeeze/value", "Left Grip Value");
        names.put("/user/hand/left/input/squeeze/force", "Left Grip Force");
        names.put("/user/hand/left/input/trigger/click", "Left Trigger Click");
        names.put("/user/hand/left/input/trigger/value", "Left Trigger");
        names.put("/user/hand/left/input/trigger/touch", "Left Trigger Touch");
        names.put("/user/hand/left/input/thumbstick", "Left Thumbstick");
        names.put("/user/hand/left/input/thumbstick/x", "Left Thumbstick X");
        names.put("/user/hand/left/input/thumbstick/y", "Left Thumbstick Y");
        names.put("/user/hand/left/input/thumbstick/click", "Left Thumbstick Click");
        names.put("/user/hand/left/input/thumbstick/touch", "Left Thumbstick Touch");
        names.put("/user/hand/left/input/thumbrest/touch", "Left Thumbrest Touch");
        names.put("/user/hand/left/input/trackpad", "Left Trackpad");
        names.put("/user/hand/left/input/trackpad/x", "Left Trackpad X");
        names.put("/user/hand/left/input/trackpad/y", "Left Trackpad Y");
        names.put("/user/hand/left/input/trackpad/force", "Left Trackpad Force");
        names.put("/user/hand/left/input/trackpad/touch", "Left Trackpad Touch");
        names.put("/user/hand/left/input/shoulder/click", "Left Shoulder");

        // Right Hand
        names.put("/user/hand/right/input/a/click", "Right A");
        names.put("/user/hand/right/input/a/touch", "Right A Touch");
        names.put("/user/hand/right/input/b/click", "Right B");
        names.put("/user/hand/right/input/b/touch", "Right B Touch");
        names.put("/user/hand/right/input/menu/click", "Right Menu");
        names.put("/user/hand/right/input/system/click", "Right System");
        names.put("/user/hand/right/input/select/click", "Right Select");
        names.put("/user/hand/right/input/squeeze/click", "Right Grip");
        names.put("/user/hand/right/input/squeeze/value", "Right Grip Value");
        names.put("/user/hand/right/input/squeeze/force", "Right Grip Force");
        names.put("/user/hand/right/input/trigger/click", "Right Trigger Click");
        names.put("/user/hand/right/input/trigger/value", "Right Trigger");
        names.put("/user/hand/right/input/trigger/touch", "Right Trigger Touch");
        names.put("/user/hand/right/input/thumbstick", "Right Thumbstick");
        names.put("/user/hand/right/input/thumbstick/x", "Right Thumbstick X");
        names.put("/user/hand/right/input/thumbstick/y", "Right Thumbstick Y");
        names.put("/user/hand/right/input/thumbstick/click", "Right Thumbstick Click");
        names.put("/user/hand/right/input/thumbstick/touch", "Right Thumbstick Touch");
        names.put("/user/hand/right/input/thumbrest/touch", "Right Thumbrest Touch");
        names.put("/user/hand/right/input/trackpad", "Right Trackpad");
        names.put("/user/hand/right/input/trackpad/x", "Right Trackpad X");
        names.put("/user/hand/right/input/trackpad/y", "Right Trackpad Y");
        names.put("/user/hand/right/input/trackpad/force", "Right Trackpad Force");
        names.put("/user/hand/right/input/trackpad/touch", "Right Trackpad Touch");
        names.put("/user/hand/right/input/shoulder/click", "Right Shoulder");

        return names;
    }
}
