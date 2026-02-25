package org.vivecraft.client_vr.provider.openxr.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.vivecraft.client.utils.FileUtils;
import org.vivecraft.client_vr.provider.control.ActionType;

import java.util.Map;

public class ControllerMapping {
    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static Map<String, ActionType> getMapping(String Headset) {
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/vive_cosmos_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/vive_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
            case "/interaction_profiles/valve/index_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/index_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
            case "/interaction_profiles/oculus/touch_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/touch_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
            case "/interaction_profiles/bytedance/pico4_controller",
                 "/interaction_profiles/bytedance/pico_neo3_controller" -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/pico_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
            default -> {
                return GSON.fromJson(FileUtils.loadAssetToString("input/mappings/simple_controller.json", true),
                    TypeToken.getParameterized(Map.class, String.class, ActionType.class).getType());
            }
        }
    }
}
