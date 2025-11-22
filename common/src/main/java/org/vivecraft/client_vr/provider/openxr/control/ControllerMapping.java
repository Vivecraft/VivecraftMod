package org.vivecraft.client_vr.provider.openxr.control;

import org.vivecraft.client_vr.provider.control.ActionType;

import java.util.HashMap;
import java.util.Map;

public class ControllerMapping {

    public static Map<String, ActionType> quest2Bindings() {
        Map<String, ActionType> bindings = new HashMap<>();

        bindings.put("/user/hand/left/input/y/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/y/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/x/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/x/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trigger/touch", ActionType.BOOLEAN);
        //bindings.put("/user/hand/left/input/trigger/proximity", ActionType.BOOLEAN); openxr 1.1
        //bindings.put("/user/hand/left/input/thumb_resting_surfaces/proximity", ActionType.BOOLEAN); openxr 1.1
        bindings.put("/user/hand/left/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/left/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbrest/touch", ActionType.BOOLEAN);

        bindings.put("/user/hand/right/input/a/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/a/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trigger/touch", ActionType.BOOLEAN);
        //bindings.put("/user/hand/right/input/trigger/proximity", ActionType.BOOLEAN); openxr 1.1
        //bindings.put("/user/hand/right/input/thumb_resting_surfaces/proximity", ActionType.BOOLEAN); openxr 1.1
        bindings.put("/user/hand/right/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/right/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbrest/touch", ActionType.BOOLEAN);
        return bindings;
    }

    public static Map<String, ActionType> cosmosBindings() {
        Map<String, ActionType> bindings = new HashMap<>();
        bindings.put("/user/hand/left/input/y/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/x/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/left/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/shoulder/click", ActionType.BOOLEAN);

        bindings.put("/user/hand/right/input/a/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/right/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/shoulder/click", ActionType.BOOLEAN);
        return bindings;
    }

    public static Map<String, ActionType> viveBindings() {
        Map<String, ActionType> bindings = new HashMap<>();
        bindings.put("/user/hand/left/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trackpad", ActionType.VEC2);
        bindings.put("/user/hand/left/input/trackpad/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trackpad/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trackpad/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trackpad/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/click", ActionType.BOOLEAN);

        bindings.put("/user/hand/right/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trackpad", ActionType.VEC2);
        bindings.put("/user/hand/right/input/trackpad/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trackpad/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trackpad/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trackpad/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/click", ActionType.BOOLEAN);
        return bindings;
    }

    public static Map<String, ActionType> indexBindings() {
        Map<String, ActionType> bindings = new HashMap<>();
        bindings.put("/user/hand/left/input/a/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/a/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/b/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/b/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/system/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trigger/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/left/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trackpad", ActionType.VEC2);
        bindings.put("/user/hand/left/input/trackpad/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trackpad/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trackpad/force", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trackpad/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/force", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/value", ActionType.VEC1);

        bindings.put("/user/hand/right/input/a/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/a/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/system/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trigger/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/right/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trackpad", ActionType.VEC2);
        bindings.put("/user/hand/right/input/trackpad/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trackpad/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trackpad/force", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trackpad/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/force", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/value", ActionType.VEC1);
        return bindings;
    }

    public static Map<String, ActionType> picoBindings() {
        Map<String, ActionType> bindings = new HashMap<>();
        bindings.put("/user/hand/left/input/x/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/x/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/y/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/y/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/left/input/trigger/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/left/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/left/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/squeeze/value", ActionType.VEC1);

        bindings.put("/user/hand/right/input/a/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/a/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/b/touch", ActionType.BOOLEAN);
        //bindings.put("/user/hand/right/input/menu/click", ActionType.BOOLEAN); //not present on pico4, is present on neo3
        bindings.put("/user/hand/right/input/system/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/trigger/value", ActionType.VEC1);
        bindings.put("/user/hand/right/input/trigger/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick", ActionType.VEC2);
        bindings.put("/user/hand/right/input/thumbstick/x", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/y", ActionType.VEC1);
        bindings.put("/user/hand/right/input/thumbstick/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/thumbstick/touch", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/squeeze/value", ActionType.VEC1);
        return bindings;
    }

    public static Map<String, ActionType> defaultBindings() {
        Map<String, ActionType> bindings = new HashMap<>();
        bindings.put("/user/hand/left/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/left/input/select/click", ActionType.BOOLEAN);

        bindings.put("/user/hand/right/input/menu/click", ActionType.BOOLEAN);
        bindings.put("/user/hand/right/input/select/click", ActionType.BOOLEAN);
        return bindings;
    }

    public static Map<String, ActionType> getMapping(String Headset) {
        switch (Headset) {
            case "/interaction_profiles/htc/vive_cosmos_controller" -> {
                return cosmosBindings();
            }
            case "/interaction_profiles/htc/vive_controller" -> {
                return viveBindings();
            }
            case "/interaction_profiles/valve/index_controller" -> {
                return indexBindings();
            }
            case "/interaction_profiles/oculus/touch_controller" -> {
                return quest2Bindings();
            }
            case "/interaction_profiles/bytedance/pico4_controller",
                 "/interaction_profiles/bytedance/pico_neo3_controller" -> {
                return picoBindings();
            }
            default -> {
                return defaultBindings();
            }
        }
    }
}
