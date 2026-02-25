package org.vivecraft.client_vr.provider.control;

import java.util.List;
import java.util.Map;

public record Source(List<Action> inputs, Map<String, String> parameters, String path) {
    //TODO: this is a bit of a hack, but it allows us to reuse the same input system for both OpenVR and OpenXR. We should probably refactor this at some point to be more elegant.
    //TODO: Finish changing to OpenVR
    public String getOpenVRPath(String type) {
        String xrPath = path;
        if (path.contains("thumbstick")) xrPath = path.replace("thumbstick", "joystick");
        if (path.contains("menu")) xrPath = path.replace("menu", "application_menu");
        if (path.contains("shoulder")) xrPath = path.replace("shoulder", "bumper");
        if (path.contains("squeeze")) xrPath = path.replace("squeeze", "grab");

        return switch (type) {
            case "click", "touch" -> xrPath + "/" + type;
            case "scroll", "long", "position", "pull" -> xrPath;
            case "grab" -> xrPath + "/" + "click";
            default -> xrPath;
        };
    }
}
