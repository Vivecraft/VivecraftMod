package org.vivecraft.client_vr.provider.control;

public enum ActionType {
    BOOLEAN("boolean"),
    DOUBLE_PRESS("boolean"),
    LONG_PRESS("boolean"),
    HOLD("boolean"),
    TOGGLE("boolean"),
    VEC1("vector1"),
    VEC2("vector2"),
    POSE("pose"),
    HAPTIC("vibration");

    final String steamName;

    ActionType(String steamName) {
        this.steamName = steamName;
    }

    public String getSteamName() {
        return steamName;
    }

    public static ActionType convertFromSteam(String steamName, String mode) {
        return switch (steamName) {
            case "click" -> switch (mode) {
                case "button" -> BOOLEAN;
                case "toggle_button" -> TOGGLE;
                default -> BOOLEAN;
            };
            case "long" -> switch (mode) {
                case "button" -> LONG_PRESS;
                default -> LONG_PRESS;
            };
            case "scroll" -> switch (mode) {
                case "scroll" -> VEC2;
                default -> VEC2;
            };
            case "touch" -> switch (mode) {
                case "trackpad" -> BOOLEAN;
                default -> BOOLEAN;
            };
            case "position" -> switch (mode) {
                case "joystick" -> VEC2;
                default -> VEC2;
            };
            case "pull" -> switch (mode) {
                case "trigger" -> VEC1;
                default -> VEC1;
            };
            case "grab" -> switch (mode) {
                case "grab" -> BOOLEAN;
                default -> BOOLEAN;
            };
            case "north", "east", "south", "west" -> switch (mode) {
                case "dpad" -> VEC1;
                default -> VEC1;
            };

            default -> null;
        };
    }
}
